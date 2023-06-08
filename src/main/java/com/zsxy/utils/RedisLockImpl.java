package com.zsxy.utils;

import cn.hutool.core.lang.UUID;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class RedisLockImpl implements IRedisLock{
    private String name;
    private static final String PREFIX_NAME = "redis_lock:";
    private static final String uuid = UUID.randomUUID()+"-";
    private StringRedisTemplate stringRedisTemplate;
    private static DefaultRedisScript<Long> lua;
    static {
        lua = new DefaultRedisScript<>();
        lua.setLocation(new ClassPathResource("unlock.lua"));
        lua.setResultType(Long.class);
    }

    public RedisLockImpl(String name,StringRedisTemplate stringRedisTemplate) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public Boolean getLock(long timeout) {
        String value = uuid+Thread.currentThread().getId();
        Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(PREFIX_NAME+name,value,timeout, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(result);
    }

    @Override
    public void unlock() {
        stringRedisTemplate.execute(lua,
                Collections.singletonList(PREFIX_NAME+name),
                uuid+Thread.currentThread().getId());
    }

}
