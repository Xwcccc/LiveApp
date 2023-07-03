package com.zsxy.service.impl;

import com.zsxy.dto.Result;
import com.zsxy.entity.UserInfo;
import com.zsxy.mapper.UserInfoMapper;
import com.zsxy.service.IUserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xwc
 * @since 2023-6
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

    @Resource
    private UserServiceImpl userService;

    @Override
    public Result updateUserInfo(UserInfo userInfo) {
        boolean isSuccess = saveOrUpdate(userInfo);
        if(!userInfo.getImages().isEmpty() && userInfo.getImages() != null){
            userService.update().set("icon",userInfo.getImages()).eq("id",userInfo.getUserId()).update();
        }
        userService.update().set("account",userInfo.getAccount()).eq("id",userInfo.getUserId()).update();
        userService.update().set("nick_name",userInfo.getAccount()).eq("id",userInfo.getUserId()).update();
        userService.update().set("password",userInfo.getPassword()).eq("id",userInfo.getUserId()).update();
        return Result.ok(isSuccess);
    }

}
