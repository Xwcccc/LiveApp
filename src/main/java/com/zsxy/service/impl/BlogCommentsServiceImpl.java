package com.zsxy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsxy.dto.Result;
import com.zsxy.dto.UserDTO;
import com.zsxy.entity.*;
import com.zsxy.mapper.BlogCommentsMapper;
import com.zsxy.service.IBlogCommentsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsxy.utils.SystemConstants;
import com.zsxy.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.zsxy.utils.RedisConstants.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xwc
 * @since 2023-6
 */
@Service
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments> implements IBlogCommentsService {

    @Resource
    private UserServiceImpl userService;
    @Resource
    private BlogServiceImpl blogService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result checkAll(Long blog_id, Integer current) {
        Page<BlogComments> page = query()
                .eq("blog_id",blog_id)
                .orderByDesc("liked")
                .page(new Page<>(current, 3));
        // 获取当前页数据
        List<BlogComments> records = page.getRecords();
        // 查询用户
        records.forEach(blogComments ->{
            this.queryBlogCommentsUser(blogComments);
            this.queryLike(blogComments);
        });
        return Result.ok(records);
    }

    @Override
    public Result like(Long id) {
        // 修改点赞数量
        String key = Comment_LIKED_KEY+id;
        Long userId = UserHolder.getUser().getId();
        Double score = stringRedisTemplate.opsForZSet().score(key,userId.toString());
        if(score == null){
            boolean success = this.update().setSql("liked = liked + 1").eq("id", id).update();
            if(success) {
                stringRedisTemplate.opsForZSet().add(key,userId.toString(),System.currentTimeMillis());
            }
        }else{
            boolean success = this.update().setSql("liked = liked - 1").eq("id", id).update();
            if(success) {
                stringRedisTemplate.opsForZSet().remove(key,userId.toString());
            }
        }
        BlogComments blogComments = getById(id);
        blogComments.setIsLike(true);
        return Result.ok(blogComments);
    }

    @Override
    public Result addCommentsById(BlogComments blogComments) {
        UserDTO user = UserHolder.getUser();
        blogComments.setUserId(user.getId());
        Long blogId = blogComments.getBlogId();
        blogService.update().setSql("comments = comments + 1").eq("id", blogId).update();
        // 保存探店博文
        boolean isSuccess = save(blogComments);
        if(!isSuccess){
            return Result.fail("数据库存取失败！");
        }
        // 返回id
        return Result.ok(blogComments.getId());
    }

    @Override
    public Result queryComment(Long id) {
        BlogComments blogComments = getById(id);
        this.queryBlogCommentsUser(blogComments);
        this.queryLike(blogComments);
        return Result.ok(blogComments);
    }

    @Override
    public Result queryRankById(Long id) {
        BlogComments blogComments = getById(id);
        int likes = blogComments.getLiked();
        return Result.ok(likes);
    }

    public void queryLike(BlogComments blogComments){
        UserDTO user = UserHolder.getUser();
        if(user == null){
            return;
        }
        Long userId = user.getId();
        String key = Comment_LIKED_KEY+blogComments.getId();
        Double score = stringRedisTemplate.opsForZSet().score(key,userId.toString());
        if((score != null)){
            blogComments.setIsLike(true);
        }
    }

    public void queryBlogCommentsUser(BlogComments blogComments){
        Long userId = blogComments.getUserId();
        User user = userService.getById(userId);
        blogComments.setName(user.getNickName());
        blogComments.setIcon(user.getIcon());
    }
}
