package com.zsxy.service;

import com.zsxy.dto.Result;
import com.zsxy.entity.Follow;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xwc
 * @since 2023-6
 */
public interface IFollowService extends IService<Follow> {

    Result follow(Long blogUserId, Boolean isFollow);

    Result checkIsFollow(Long id);

    Result getCommonFollow(Long id);
}
