package com.zsxy.service;

import com.zsxy.dto.Result;
import com.zsxy.entity.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xwc
 * @since 2023-6
 */
public interface IUserInfoService extends IService<UserInfo> {

    Result updateUserInfo(UserInfo userInfo);

}
