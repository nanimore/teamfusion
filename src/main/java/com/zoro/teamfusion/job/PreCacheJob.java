package com.zoro.teamfusion.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zoro.teamfusion.moder.domain.User;
import com.zoro.teamfusion.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class PreCacheJob {
    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    //设置重点用户
    private List<Long> mainUserList = Arrays.asList(1L,2L,3L,4L,5L);

    private Random random = new Random();

    //每天执行，预热推荐用户
    // 秒，分，时，日，月，周
    @Scheduled(cron = "0 42,44 19 * * ?")
    public void doCacheRecommendUser(){

        RLock lock = redissonClient.getLock("teamfusion:precachejob:docache:lock");
        lock.lock();
        try {
            if(lock.tryLock(0, 30000, TimeUnit.MILLISECONDS)){
                log.info("getLock: "+Thread.currentThread().getId());

                for (Long userId: mainUserList){
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> userPage = userService.page(new Page<>(1, 20), queryWrapper);
                    String redisKey = String.format("teamfusion:user:recommend:%s", userId);
                    ValueOperations<String,Object> valueOperations = redisTemplate.opsForValue();
                    //写缓存
                    try {
                        valueOperations.set(redisKey,userPage,30000+ random.nextInt(1000), TimeUnit.MILLISECONDS);
                        log.info("redis set preCache key success");
                    }catch (Exception e){
                        log.error("redis set key error",e);
                    }
                }

            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error ",e);
        } finally {
            if (lock.isHeldByCurrentThread()){
                log.info("unlock: "+Thread.currentThread().getId());
                lock.unlock();
            } else {
                log.info("get lock failed……");
            }


        }

    }

    public void doCacheRecommendUser1() {
        RLock lock = redissonClient.getLock("teamfusion:job:precache:lock");

        try {
            if (lock.tryLock(0, 30, TimeUnit.SECONDS)){
                log.info("get lock");
                // do something
            }
        } catch (InterruptedException e) {
            log.error("do something error");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                log.info("unlock");
                lock.unlock();
            } else {
                log.info("get lock failed");
            }
        }

    }

}