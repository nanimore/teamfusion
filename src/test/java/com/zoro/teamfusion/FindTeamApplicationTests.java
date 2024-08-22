package com.zoro.teamfusion;
import java.util.Date;

import com.zoro.teamfusion.moder.VO.UserVO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TeamFusionApplicationTests {



    @Test
    void contextLoads() {
    }

    @Test
    void testToString() {
        UserVO userVO = new UserVO();

        userVO.setId(0L);
        userVO.setUsername("");
        userVO.setUserAccount("");
        userVO.setAvatarUrl("");
        userVO.setGender(0);
        userVO.setPhone("");
        userVO.setEmail("");
        userVO.setTags("");
        userVO.setUserStatus(0);
        userVO.setCreateTime(new Date());
        userVO.setUpdateTime(new Date());
        userVO.setUserRole(0);
        userVO.setPlanetCode("");

        System.out.println(userVO);

    }

}
