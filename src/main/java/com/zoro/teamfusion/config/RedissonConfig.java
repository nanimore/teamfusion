package com.zoro.teamfusion.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 配置
 */
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {

    private String host;
    private String port;
    private String password;
    private int database;

    @Bean
    public RedissonClient redissonClient(){
        //1、创建配置
        Config config = new Config();
        String redisAddress = String.format("redis://%s:%s", host, port);
        config.useSingleServer().setAddress(redisAddress).setPassword(password).setDatabase(database);

        //使用json序列化方式
        Codec codec = new JsonJacksonCodec();
        config.setCodec(codec);

        //2.创建实例
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}