package com.zoro.teamfusion;
import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zoro.teamfusion.moder.domain.UserTeam;
import com.zoro.teamfusion.service.UserTeamService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class InsertTest {

    @Resource
    private UserTeamService userTeamService;

    @Test
    void insertUserTeam(){
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(1L);
        userTeam.setTeamId(2L);
        userTeam.setJoinTime(new Date());
        userTeam.setCreateTime(new Date());
        userTeam.setUpdateTime(new Date());
        userTeam.setIsDelete((byte)0);

        userTeamService.save(userTeam);

    }

    @Test
    void getTeamNum(){

        UserTeam userTeam = new UserTeam();
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>(userTeam);
        long count = userTeamService.count(userTeamQueryWrapper);
        System.out.println(count);
    }


}
