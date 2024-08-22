package com.zoro.teamfusion;
import java.util.Date;

import com.zoro.teamfusion.moder.domain.User;
import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class RedissonTest {
    @Resource
    private RedissonClient redissonClient;

    @Test
    void test(){
        //List。数据存在本地JVM内存中
//        ArrayList<Object> list = new ArrayList<>();
//        list.add("zoro");
//        System.out.println("list: "+list.get(0));
//        list.remove(0);

        //数据存在redis的内存中
        RList<User> rList = redissonClient.getList("rList");

        User user = new User();
        user.setId(0L);
        user.setUsername("");
        user.setUserAccount("");
        user.setAvatarUrl("");
        user.setGender(0);
        user.setUserPassword("");
        user.setEmail("");
        user.setUserStatus(0);
        user.setPhone("");
        user.setTags("");
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user.setIsDelete((byte)0);
        user.setUserRole(0);


        rList.add(user);
        System.out.println("rList: "+rList.get(1));

//        rList.remove(0);
    }

}