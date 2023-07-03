package com.zsxy.controller;

import com.zsxy.dto.Result;
import com.zsxy.utils.MD5Util;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

import static com.zsxy.utils.RedisConstants.LOGIN_SALT_KEY;


@RestController
@RequestMapping("/encrypt")
public class EncryptController {

    @Resource
    private RedisTemplate redisTemplate;

    @PostMapping("/getSalt")
    public Result getSalt(){
        String salt = new MD5Util().getSalt();
        redisTemplate.opsForValue().setIfAbsent(LOGIN_SALT_KEY,salt,3, TimeUnit.MINUTES);
        return Result.ok(salt);
    }
}
