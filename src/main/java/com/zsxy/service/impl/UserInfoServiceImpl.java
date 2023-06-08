package com.zsxy.service.impl;

import com.zsxy.entity.UserInfo;
import com.zsxy.mapper.UserInfoMapper;
import com.zsxy.service.IUserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

}
