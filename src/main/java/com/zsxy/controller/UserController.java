package com.zsxy.controller;


import cn.hutool.core.bean.BeanUtil;
import com.zsxy.dto.LoginFormDTO;
import com.zsxy.dto.Result;
import com.zsxy.dto.UserDTO;
import com.zsxy.entity.Blog;
import com.zsxy.entity.User;
import com.zsxy.entity.UserInfo;
import com.zsxy.service.IUserInfoService;
import com.zsxy.service.IUserService;
import com.zsxy.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.Date;

import static com.zsxy.utils.RedisConstants.LOGIN_USER_KEY;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author xwc
 * @since 2023-6
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    @Resource
    private IUserInfoService userInfoService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private String token;
    /**
     * 发送手机验证码
     */
    @PostMapping("code")
    public Result sendCode(@RequestParam("phone") String phone, HttpSession session) {
        //发送短信验证码并保存验证码
        return userService.sendCode(phone, session);
    }

    /**
     * 登录功能
     * @param loginForm 登录参数，包含手机号、验证码
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginForm, HttpSession session){
        // 实现登录功能
        Result r = userService.login(loginForm, session);
        token = (String) r.getData();
        return r;
    }

    /**
     * 登录功能
     * @param loginForm 登录参数，包含账号、密码
     */
    @PostMapping("/loginByPwd")
    public Result login2(@RequestBody LoginFormDTO loginForm){
        // 实现登录功能
        Result r = userService.loginByPwd(loginForm);
        token = (String) r.getData();
        return r;
    }
    /**
     * 登出功能
     * @return 无
     */
    @PostMapping("/logout")
    public Result logout(){
        // 实现登出功能
        stringRedisTemplate.delete(LOGIN_USER_KEY+token);
        UserHolder.removeUser();
        return Result.ok();
    }

    @GetMapping("/me")
    public Result me(){
        // 获取当前登录的用户并返回
        Long uId = UserHolder.getUser().getId();
        UserDTO user = (UserDTO) queryUserById(uId).getData();
        return Result.ok(user);
    }

    @GetMapping("/info/{id}")
    public Result info(@PathVariable("id") Long userId){
        // 查询详情
        UserInfo info = userInfoService.getById(userId);
        if (info == null) {
            // 没有详情，应该是第一次查看详情
            return Result.ok();
        }
        info.setCreateTime(null);
        info.setUpdateTime(null);
        info.setPassword(null);
        // 返回
        return Result.ok(info);
    }

    @GetMapping("/{id}")
    public Result queryUserById(@PathVariable(value = "id") Long id){
        User user = userService.getById(id);
        if(user == null){
            return Result.ok();
        }
        UserDTO userDTO = BeanUtil.copyProperties(user,UserDTO.class);
        return Result.ok(userDTO);
    }

    @PostMapping("/sign")
    public Result sign(){
        return userService.sign();
    }

    @GetMapping("/sign/count")
    public Result signCount(){
        return userService.signCount();
    }

    @PostMapping("/edit")
    public Result updateUserInfo(@RequestBody UserInfo userInfo){
        return userInfoService.updateUserInfo(userInfo);
    }

    @PostMapping("/setIcon")
    public Result updateIcon(@RequestBody String icon){
        return userService.setIcon(icon);
    }

}
