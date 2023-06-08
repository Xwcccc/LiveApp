package com.zsxy.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static java.time.LocalDate.now;

/**
 * 全局ID生成器
 * 问题：用数据库表自增ID记录增长的秒杀优惠券容易在返回订单信息时泄露销售数据，并且若要分表会产生重复的秒杀订单ID，这是不可接受的
 * 解决办法：用全局ID来标识秒杀订单
 * 具体描述：
 * ID需求：体现自增长方便后期统计数据同时又无法被轻易反推
 * 全局ID结构： 1位符号位+31位时间戳+32位自增序列号
 */
@Component
public class RedisIdWorker {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    private final static long BEIGN_TIME = 1664420760;

    public long nextID(String prefixKey){
        //1.生成时间戳
        LocalDateTime time = LocalDateTime.now();
        long seconds = time.toEpochSecond(ZoneOffset.UTC) - BEIGN_TIME;
        //2.用redis生成自增序列号，其实感觉也可以用一张专门产生自增ID的数据库表来解决
        String date = now().format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        long number = stringRedisTemplate.opsForValue().increment("icr"+prefixKey+":"+date);
        //3.拼接
        return seconds << 32 | number;
    }

}
