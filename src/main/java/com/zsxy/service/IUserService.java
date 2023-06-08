package com.zsxy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsxy.dto.LoginFormDTO;
import com.zsxy.dto.Result;
import com.zsxy.entity.User;

import javax.servlet.http.HttpSession;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xwc
 * @since 2023-6
 */
public interface IUserService extends IService<User> {

    Result sendCode(String phone, HttpSession session);

    Result login(LoginFormDTO loginForm, HttpSession session);

    Result sign();

    Result signCount();

}
