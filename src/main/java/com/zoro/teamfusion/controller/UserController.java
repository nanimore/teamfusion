package com.zoro.teamfusion.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zoro.teamfusion.common.BaseResponse;
import com.zoro.teamfusion.common.ErrorCode;
import com.zoro.teamfusion.common.ResultUtils;
import com.zoro.teamfusion.exception.BusinessException;
import com.zoro.teamfusion.mapper.UserMapper;
import com.zoro.teamfusion.moder.domain.User;
import com.zoro.teamfusion.moder.request.UserLoginRequest;
import com.zoro.teamfusion.moder.request.UserRegisterRequest;
import com.zoro.teamfusion.moder.request.UserSearchRequest;
import com.zoro.teamfusion.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.zoro.teamfusion.constant.UserConstant.ADMIN_ROLE;
import static com.zoro.teamfusion.constant.UserConstant.USER_LOGIN_STATE;


/**
 * 用户接口
 *
 * @author ZORO
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:3000"})
@Slf4j
public class UserController {
    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();

        long l = userService.userRegister(userAccount, userPassword, checkPassword);

        return ResultUtils.success(l);

    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        System.out.println(request.getParameter("userAccount"));
        request.getParameter("userAccount");
        request.getRemoteAddr();
        System.out.println(request.getRemoteAddr());
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);

    }

    @GetMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        int result = userService.userLogout(request);
        return ResultUtils.success(result);

    }

    @Resource
    private UserMapper userMapper;

    @PostMapping("/searchDome")
    public User seachUser(@RequestBody UserSearchRequest userSearchRequest) {
//        Userdemo userdemo1 = userdomeMapper.selectOne(new QueryWrapper<Userdemo>().eq("age",28));
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("id", userSearchRequest.getUserId()));
        return user;
    }

    @PostMapping("/searchDome1")
    public User seachUser1(@RequestParam(value = "userId") long userId) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("id", userId));
        return user;
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String userAccount, HttpServletRequest request) {
        //判断是否为管理员
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(userAccount)) {
            wrapper.like("userAccount", userAccount);
        }
        List<User> userList = userService.list(wrapper);
        List<User> collect = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(collect);
    }

    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(userList);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deletUser(@RequestParam(value = "id") long id, HttpServletRequest request) {
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }

    @PostMapping("/delete1")
    public boolean deletUser1(@RequestBody Map<String, Long> params) {
        Long id = params.get("id");
        if (id <= 0) {
            return false;
        }
        return userService.removeById(id);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        User user = userService.getById(currentUser.getId());
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    private boolean isAdmin(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;

        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    // todo 推荐多个，未实现
    /**
     * 推荐页面
     * @param request
     * @return
     */
    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageSize, long pageNum, HttpServletRequest request){

//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        Page<User> userList =  userService.page(new Page<>(pageNum, pageSize), queryWrapper);
//        return ResultUtils.success(userList);

        User loginUser = userService.getLoginUser(request);
        String redisKey = String.format("teamfusion:user:recommend:%s", loginUser.getId());
        ValueOperations<String,Object> valueOperations = redisTemplate.opsForValue();
        //如果有缓存，直接读缓存
        Page<User> userPage= (Page<User>)valueOperations.get(redisKey);
        if (userPage!=null){
            return ResultUtils.success(userPage);
        }
        //无缓存，查数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        userPage = userService.page(new Page<>(pageNum, pageSize), queryWrapper);
        //写缓存
        try {
            valueOperations.set(redisKey,userPage,30000, TimeUnit.MILLISECONDS);
        }catch (Exception e){
            log.error("redis set key error",e);
        }
        return ResultUtils.success(userPage);

    }

    /**
     * 获取最匹配的用户
     *
     * @param num
     * @param request
     * @return
     */
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(long num, HttpServletRequest request) {
        if (num <= 0 || num > 500) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.matchUsers(num, user));
    }

    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        // 校验参数是否为空
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        int result = userService.updateUser(user, loginUser);
        return ResultUtils.success(result);
    }


}
