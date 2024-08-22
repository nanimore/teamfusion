package com.zoro.teamfusion.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zoro.teamfusion.common.ErrorCode;
import com.zoro.teamfusion.exception.BusinessException;
import com.zoro.teamfusion.mapper.TeamMapper;
import com.zoro.teamfusion.moder.VO.TeamUserVO;
import com.zoro.teamfusion.moder.VO.UserVO;
import com.zoro.teamfusion.moder.domain.Team;
import com.zoro.teamfusion.moder.domain.User;
import com.zoro.teamfusion.moder.domain.UserTeam;
import com.zoro.teamfusion.moder.dto.TeamQuery;
import com.zoro.teamfusion.moder.enmus.TeamStatusEnum;
import com.zoro.teamfusion.moder.request.TeamJoinRequest;
import com.zoro.teamfusion.moder.request.TeamQuitRequest;
import com.zoro.teamfusion.moder.request.TeamUpdateRequest;
import com.zoro.teamfusion.service.TeamService;
import com.zoro.teamfusion.service.UserService;
import com.zoro.teamfusion.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@Service
@Slf4j
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService {

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {
        //1. 请求参数是否为空？
        if (team == null ) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2. 是否登录，未登录不允许创建
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        //3. 校验信息
        //   1. 队伍人数 > 1 且 <= 20
        Integer maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum <=1 || maxNum >20) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "队伍人数不满足要求");
        }
        //   2. 队伍标题 <= 20
        String name = team.getName();
        if (StringUtils.isBlank(name) || name.length() > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍标题不满足要求");
        }
        //   3. 描述 <= 512
        String description = team.getDescription();
        if (StringUtils.isNotBlank(description) && description.length() > 512){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述过长");
        }

        //   4. status 是否公开（int）不传默认为 0（公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
        }
        //   5. 如果 status 是加密状态，一定要有密码，且密码 <= 32
        String password = team.getPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum)){
            if(StringUtils.isBlank(password) || password.length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码设置不正确");
            }
        }
        //   6. 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if (new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "超时时间 > 当前时间");
        }

        //   7. 校验用户最多创建 5 个队伍
        // ToDo 可能同时插入多个队伍
        final long userId = loginUser.getId();

        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        long hasTeamNum = this.count(queryWrapper);
        if (hasTeamNum > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多创建 5 个队伍");
        }

        //4. 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getId();
        if (!result || teamId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        //5. 插入用户  => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }

        return teamId;
    }

    @Override
    public List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin) {

        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        //组合查询条件
        if (teamQuery != null) {
            Long id = teamQuery.getId();
            if (id != null && id > 0) {
                queryWrapper.eq("id", id);
            }

            List<Long> idList = teamQuery.getIdList();
            if (CollectionUtils.isNotEmpty(idList)) {
                queryWrapper.in("id", idList);
            }

            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like("name", name);
            }

            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.like("description", description);
            }

            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(qw -> qw.like("name", name).or().like("description", description));
            }

            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum != null && maxNum > 0) {
                queryWrapper.lt("maxNum", maxNum);
            }
            // 根据状态来查询
            Integer status = teamQuery.getStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
            statusEnum = Optional.ofNullable(statusEnum).orElse(TeamStatusEnum.PUBLIC);

//            if (statusEnum.equals(TeamStatusEnum.PUBLIC) || isAdmin) {
//                queryWrapper.eq("status", statusEnum.getValue());
//            }
//            else {
//                throw new BusinessException(ErrorCode.NO_AUTH);
//            }

            if (!isAdmin && statusEnum.equals(TeamStatusEnum.PRIVATE)) {
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            queryWrapper.eq("status", statusEnum.getValue());

            Long userId = teamQuery.getUserId();
            // 根据创建人来查询
            if (userId != null && userId > 0) {
                queryWrapper.eq("userId", userId);
            }

        }
        // 不展示已过期的队伍
        // expireTime is null or expireTime > now()
        queryWrapper.and(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));

        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }

        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        // 关联查询创建人的用户信息
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }

            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);

            User user = userService.getById(userId);

            if (user != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                teamUserVO.setCreateUser(userVO);
            }

            long teamHasJoinNum = this.countTeamUserByTeamId(teamUserVO.getId());
            if (teamHasJoinNum >= team.getMaxNum()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已满");
            }
            teamUserVO.setHasJoinNum((int) teamHasJoinNum);
            teamUserVOList.add(teamUserVO);
        }

        return teamUserVOList;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {

        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long id = teamUpdateRequest.getId();

        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team oldTeam = this.getById(id);


        if (oldTeam == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        if (!loginUser.getId().equals(oldTeam.getUserId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }


        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
        //TODO 本来就是加密的，不修改密码
        if (statusEnum.equals(TeamStatusEnum.SECRET)) {
            if (StringUtils.isBlank(teamUpdateRequest.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密房间必须要设置密码");
            }
        }
        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, updateTeam);

        return this.updateById(updateTeam);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {

        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        Team team = this.getById(teamJoinRequest.getTeamId());

        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }

        Date expireTime = team.getExpireTime();
        if (expireTime != null && new Date().before(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }

        if (team.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能加入自己的队伍");
        }

        Integer teamStatus = team.getStatus();
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(teamStatus);
        if (statusEnum.equals(TeamStatusEnum.PRIVATE)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "禁止加入私有的队伍");
        }

        if (statusEnum.equals(TeamStatusEnum.SECRET) && !teamJoinRequest.getPassword().equals(team.getPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }

        RLock lock = redissonClient.getLock("teamfusion:teamServce:joinTeam:lock");

        try {
            while (true) {
                if (lock.tryLock(0, 30, TimeUnit.SECONDS)) {
                    log.info("get lock");

                    QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("userId", loginUser.getId());
                    long countJoinAll = userTeamService.count(queryWrapper);
                    if (countJoinAll > 5) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多创建或加入 5 个队伍");
                    }
                    queryWrapper.eq("teamId", teamJoinRequest.getTeamId());
                    long countJoin = userTeamService.count(queryWrapper);

                    if (countJoin > 0) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已加入该队伍");
                    }

                    // 已加入队伍的人数
                    long teamHasJoinNum = this.countTeamUserByTeamId(teamJoinRequest.getTeamId());
                    if (teamHasJoinNum >= team.getMaxNum()) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已满");
                    }

                    // 修改队伍信息
                    UserTeam userTeam = new UserTeam();
                    userTeam.setUserId(loginUser.getId());
                    userTeam.setJoinTime(new Date());
                    userTeam.setTeamId(teamJoinRequest.getTeamId());

                    return userTeamService.save(userTeam);
                }
            }

        } catch (InterruptedException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "加入失败");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                log.info("unlock");
                lock.unlock();

            } else {
                log.info("get lock failed");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(long id, User loginUser) {
        // 校验队伍是否存在
        Team team = getTeamById(id);
        long teamId = team.getId();
        // 校验是不是队伍的队长
        if (!team.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无访问权限");
        }
        // 移除所有加入队伍的关联信息
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        boolean result = userTeamService.remove(userTeamQueryWrapper);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍关联信息失败");
        }
        // 删除队伍
        return this.removeById(teamId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamQuitRequest.getTeamId();
        Team team = getTeamById(teamId);
        long userId = loginUser.getId();
        UserTeam queryUserTeam = new UserTeam();
        queryUserTeam.setTeamId(teamId);
        queryUserTeam.setUserId(userId);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>(queryUserTeam);
        long count = userTeamService.count(queryWrapper);
        if (count == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未加入队伍");
        }
        long teamHasJoinNum = this.countTeamUserByTeamId(teamId);
        // 队伍只剩一人，解散
        if (teamHasJoinNum == 1) {
            // 删除队伍
            this.removeById(teamId);
        } else {
            // 队伍还剩至少两人
            // 是队长
            if (team.getUserId() == userId) {
                // 把队伍转移给最早加入的用户
                // 1. 查询已加入队伍的所有用户和加入时间
                QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("teamId", teamId);
                userTeamQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                if (CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                UserTeam nextUserTeam = userTeamList.get(1);
                Long nextTeamLeaderId = nextUserTeam.getUserId();
                // 更新当前队伍的队长
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextTeamLeaderId);
                boolean result = this.updateById(updateTeam);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队伍队长失败");
                }
            }
        }
        // 移除关系
        return userTeamService.remove(queryWrapper);
    }

    /**
     * 获取某队伍当前人数
     *
     * @param teamId
     * @return
     */
    public long countTeamUserByTeamId(long teamId) {
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        return userTeamService.count(userTeamQueryWrapper);
    }

    /**
     * 根据 id 获取队伍信息
     *
     * @param teamId
     * @return
     */
    private Team getTeamById(Long teamId) {
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        return team;
    }



}




