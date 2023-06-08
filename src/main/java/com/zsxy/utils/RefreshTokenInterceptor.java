package com.zsxy.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.zsxy.dto.UserDTO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.zsxy.utils.RedisConstants.LOGIN_USER_KEY;

public class RefreshTokenInterceptor implements HandlerInterceptor {
    private RedisTemplate<String, Map<Object,Object>> redisTemplate;

    public RefreshTokenInterceptor(RedisTemplate<String, Map<Object,Object>> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        response.addHeader("Access-Control-Expose-Headers","Authorization");
        String token = request.getHeader("authorization");
        if(StrUtil.isBlank(token)){
            return true;
        }
        String key = LOGIN_USER_KEY+token;
        Map<Object,Object> user = redisTemplate.opsForHash().entries(key);
        if(user.isEmpty()){
            return true;
        }
        UserDTO result = BeanUtil.fillBeanWithMap(user,new UserDTO(),false);
        UserHolder.saveUser(result);
        redisTemplate.expire(key,30, TimeUnit.MINUTES);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
