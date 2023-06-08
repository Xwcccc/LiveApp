package com.zsxy.config;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
    @Bean
    public RedissonClient RedissonCreate(){
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.204.136:6379").setPassword("123321");
        return Redisson.create(config);
    }
}