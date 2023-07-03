package com.zsxy.service;

import com.zsxy.dto.Result;
import com.zsxy.entity.BlogComments;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xwc
 * @since 2023-6
 */
public interface IBlogCommentsService extends IService<BlogComments> {

    Result checkAll(Long blog_id, Integer current);

    Result like(Long id);

    Result addCommentsById(BlogComments blogComments);

    Result queryComment(Long id);

    Result queryRankById(Long id);
}
