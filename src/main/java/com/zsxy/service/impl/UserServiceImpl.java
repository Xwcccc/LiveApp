package com.zsxy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsxy.dto.LoginFormDTO;
import com.zsxy.dto.Result;
import com.zsxy.dto.UserDTO;
import com.zsxy.entity.User;
import com.zsxy.mapper.UserMapper;
import com.zsxy.service.IUserService;
import com.zsxy.utils.RegexUtils;
import com.zsxy.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.zsxy.utils.RedisConstants.*;
import static com.zsxy.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author xwc
 * @since 2023-6
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public Result sendCode(String phone, HttpSession session) {
        if(RegexUtils.isPhoneInvalid(phone)){
            return Result.fail("手机号无效");
        }
        String code = RandomUtil.randomNumbers(6);
        redisTemplate.opsForValue().set(LOGIN_CODE_KEY+phone,code,LOGIN_CODE_TTL, TimeUnit.MINUTES);
        log.debug("生成验证码成功，验证码为{}",code);
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        //校验手机号和验证码
        String phone = loginForm.getPhone();
        String code = loginForm.getCode();
        String s = redisTemplate.opsForValue().get(LOGIN_CODE_KEY+phone);
        if(s == null || !s.equals(code)){
            return Result.fail("验证码错误");
        }
        //查询用户
        User user = query().eq("phone",phone).one();
        //用户不存在，注册
        if(user == null){
            log.debug("用户不存在");
            user = createNewUser(loginForm);
        }
        UserDTO u = BeanUtil.copyProperties(user, UserDTO.class);

        String token = UUID.randomUUID().toString();
        Map<String,Object> map = BeanUtil.beanToMap(u,new HashMap<>(), CopyOptions.create().ignoreNullValue().setFieldValueEditor(
                (fieldName,fieldValue)-> fieldValue.toString()));
        redisTemplate.opsForHash().putAll(LOGIN_USER_KEY+token,map);
        //只要用户在访问，那么有效期就不断刷新
        redisTemplate.expire(LOGIN_USER_KEY+token,30,TimeUnit.MINUTES);
        return Result.ok(token);
    }

    @Override
    public Result sign() {
        Long userId = UserHolder.getUser().getId();
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern(":yyyyMM"));
        int day = LocalDateTime.now().getDayOfMonth();
        String key = USER_SIGN_KEY + userId + time;
        redisTemplate.opsForValue().setBit(key,day-1,true);
        return Result.ok();
    }

    @Override
    public Result signCount() {
        Long userId = UserHolder.getUser().getId();
        LocalDateTime now = LocalDateTime.now();
        String time = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        int day = now.getDayOfMonth();
        String key = USER_SIGN_KEY + userId + time;
        List<Long> result = redisTemplate.opsForValue().bitField(key,
                BitFieldSubCommands.create()
                        .get(BitFieldSubCommands.BitFieldType.unsigned(day)).valueAt(0));
        if(result == null || result.isEmpty()){
            return Result.ok(0);
        }
        Long num = result.get(0);
        if(num == null || num == 0){
            return Result.ok(0);
        }
        int count = 0;
        while (true){
            long tmp = num & 1;
            if(tmp == 0){
                break;
            }else {
                count++;
            }
            num = num >> 1;
        }
        return Result.ok(count);
    }

    private User createNewUser(LoginFormDTO loginForm) {
        User user = new User();
        user.setPhone(loginForm.getPhone());
        user.setNickName(USER_NICK_NAME_PREFIX +RandomUtil.randomNumbers(6));
        save(user);
        return user;
    }

}
