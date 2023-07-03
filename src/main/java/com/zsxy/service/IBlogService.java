package com.zsxy.service;

import com.zsxy.dto.Result;
import com.zsxy.entity.Blog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xwc
 * @since 2023-6
 */
public interface IBlogService extends IService<Blog> {

    Result queryBlog(Long id);

    Result queryHotBlog(Integer current);

    Result like(Long id);

    Result queryRankById(Long id);

    Result saveAndFeedBlog(Blog blog);

    Result queryBlogByFollow(Long max, Integer offset);

    Result deleteById(Long blog_id);
}
