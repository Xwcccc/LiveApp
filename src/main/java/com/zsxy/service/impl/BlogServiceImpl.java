package com.zsxy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsxy.dto.Result;
import com.zsxy.dto.UserDTO;
import com.zsxy.entity.Blog;
import com.zsxy.entity.Follow;
import com.zsxy.entity.FollowResult;
import com.zsxy.entity.User;
import com.zsxy.mapper.BlogMapper;
import com.zsxy.service.IBlogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsxy.service.IUserService;
import com.zsxy.utils.SystemConstants;
import com.zsxy.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.zsxy.utils.RedisConstants.BLOG_LIKED_KEY;
import static com.zsxy.utils.RedisConstants.FEED_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xwc
 * @since 2023-6
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

    @Resource
    private IUserService userService;
    @Resource
    private BlogServiceImpl blogService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private FollowServiceImpl followService;

    @Override
    public Result queryBlog(Long id) {
        Blog blog = getById(id);
        this.queryBlogUser(blog);
        this.queryLike(blog);
        return Result.ok(blog);
    }

    @Override
    public Result queryHotBlog(Integer current) {
        Page<Blog> page = query()
                .orderByDesc("liked")
                .page(new Page<>(current, 10));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        // 查询用户
        records.forEach(blog ->{
            this.queryBlogUser(blog);
            this.queryLike(blog);
        });
        return Result.ok(records);
    }

    public void queryBlogUser(Blog blog){
        Long userId = blog.getUserId();
        User user = userService.getById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
    }

    public void queryLike(Blog blog){
        UserDTO user = UserHolder.getUser();
        if(user == null){
            return;
        }
        Long userId = user.getId();
        String key = BLOG_LIKED_KEY+blog.getId();
        Double score = stringRedisTemplate.opsForZSet().score(key,userId.toString());
        if((score != null)){
            blog.setIsLike(true);
        }
    }

    @Override
    public Result like(Long id) {
        // 修改点赞数量
        String key = BLOG_LIKED_KEY+id;
        Long userId = UserHolder.getUser().getId();
        Double score = stringRedisTemplate.opsForZSet().score(key,userId.toString());
        if(score == null){
            boolean success = update().setSql("liked = liked + 1").eq("id", id).update();
            if(success) {
                stringRedisTemplate.opsForZSet().add(key,userId.toString(),System.currentTimeMillis());
            }
        }else{
            boolean success = update().setSql("liked = liked - 1").eq("id", id).update();
            if(success) {
                stringRedisTemplate.opsForZSet().remove(key,userId.toString());
            }
        }
        return Result.ok();
    }

    @Override
    public Result queryRankById(Long id) {
        String key = BLOG_LIKED_KEY + id;
        Set<String> set = stringRedisTemplate.opsForZSet().range(key,0,4);
        if(set == null || set.isEmpty()){
            return Result.ok(Collections.emptyList());
        }
        List<Long> list = set.stream().map(Long::valueOf).collect(Collectors.toList());
        String idStr = StrUtil.join(",", list);
        // 3.根据用户id查询用户 WHERE id IN ( 5 , 1 ) ORDER BY FIELD(id, 5, 1)
        List<UserDTO> userDTOS = userService.query()
                .in("id", list).last("ORDER BY FIELD(id," + idStr + ")").list()
                .stream().map(user ->  BeanUtil.copyProperties(user,UserDTO.class)).collect(Collectors.toList());
        return Result.ok(userDTOS);
    }

    @Override
    public Result saveAndFeedBlog(Blog blog) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        blog.setUserId(user.getId());
        // 保存探店博文
        boolean isSuccess = save(blog);
        if(!isSuccess){
            return Result.fail("数据库存取失败！");
        }
        //获取所有关注博主的人，即在数据库表中查询follow_id为博主的人的id
        List<Follow> follow_list = followService.query().eq("follow_user_id",blog.getUserId()).list();
        for(Follow f:follow_list){
            Long followId = f.getUserId();
            String key = FEED_KEY+followId;
            stringRedisTemplate.opsForZSet().add(key,blog.getId().toString(),System.currentTimeMillis());
        }
        // 返回id
        return Result.ok(blog.getId());
    }

    @Override
    public Result queryBlogByFollow(Long max, Integer offset) {
        Long userId = UserHolder.getUser().getId();
        String key = FEED_KEY+userId;
        Set<ZSetOperations.TypedTuple<String>> blogSet = stringRedisTemplate.opsForZSet().reverseRangeByScoreWithScores(key,0,max,offset,3);
        if(blogSet == null || blogSet.isEmpty()){
            return Result.ok();
        }
        List<String> ids = new ArrayList<>();
        Long min = null;
        int count = 1;
        for(ZSetOperations.TypedTuple<String> blog : blogSet){
            ids.add(blog.getValue());
            Long tmp = blog.getScore().longValue();;
            if(tmp.equals(min)){
                count++;
            }else{
                count = 1;
                min = tmp;
            }
        }
        String idStr = StrUtil.join(",",ids);
        List<Blog> blogs = blogService.query()
                .in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list();
        for (Blog blog: blogs){
            this.queryBlogUser(blog);
            this.queryLike(blog);
        }
        FollowResult followResult = new FollowResult();
        followResult.setList(blogs);
        followResult.setMinTime(min);
        followResult.setOffset(count);
        return Result.ok(followResult);
    }

    @Override
    public Result deleteById(Long blog_id) {
        boolean isSuccess = removeById(blog_id);
        return Result.ok(isSuccess);
    }

}
