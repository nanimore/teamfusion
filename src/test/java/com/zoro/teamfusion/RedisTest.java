package com.zoro.teamfusion;

import com.zoro.teamfusion.moder.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

@SpringBootTest
public class RedisTest {

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    @Test
    void test(){
        ValueOperations valueOperations = redisTemplate.opsForValue();

        //增
        valueOperations.set("zoroString","dog");
        valueOperations.set("zoroInt",1);
        valueOperations.set("zoroDouble",2.0);
        User user = new User();
        user.setId(1L);
        user.setUsername("zoro");
        valueOperations.set("zoroUser",user);
        //查
        Object zoro = valueOperations.get("zoroString");
        Assertions.assertTrue("dog".equals((String)zoro));
        zoro = valueOperations.get("zoroInt");
        Assertions.assertTrue(1==((Integer)zoro));
        zoro = valueOperations.get("zoroDouble");
        Assertions.assertTrue(2.0==((Double)zoro));
        System.out.println(valueOperations.get("zoroUser"));
//        valueOperations.set("zoroString","dog");
//        redisTemplate.delete("zoroString");
    }

}