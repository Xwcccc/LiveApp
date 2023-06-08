package com.zsxy.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsxy.dto.Result;
import com.zsxy.dto.UserDTO;
import com.zsxy.entity.Blog;
import com.zsxy.service.IBlogService;
import com.zsxy.utils.SystemConstants;
import com.zsxy.utils.UserHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author xwc
 * @since 2023-6
 */
@RestController
@RequestMapping("/blog")
public class BlogController {

    @Resource
    private IBlogService blogService;

    @PostMapping
    public Result saveBlog(@RequestBody Blog blog) {
        return blogService.saveAndFeedBlog(blog);
    }

    @PutMapping("/like/{id}")
    public Result likeBlog(@PathVariable("id") Long id) {
        return blogService.like(id);
    }

    @GetMapping("/of/me")
    public Result queryMyBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        // 根据用户查询
        Page<Blog> page = blogService.query()
                .eq("user_id", user.getId()).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        return Result.ok(records);
    }

    @GetMapping("/hot")
    public Result queryHotBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        // 根据用户查询
        return blogService.queryHotBlog(current);
    }

    @GetMapping("/{id}")
    public Result queryBlogById(@PathVariable(value = "id") Long id){
        return blogService.queryBlog(id);
    }

    @GetMapping("/likes/{id}")
    public Result queryBlogLikes(@PathVariable(value = "id") Long id){
        return blogService.queryRankById(id);
    }

    @GetMapping("/of/user")
    public Result queryBlogByUserId(@RequestParam(value = "id") Long id, @RequestParam(value = "current",defaultValue = "1") Integer current){
        Page<Blog> page = blogService.query().eq("user_id",id).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        List<Blog> list = page.getRecords();
        return Result.ok(list);
    }

    @GetMapping("/of/follow")
    public Result queryBlogByFollow(@RequestParam(value = "lastId") Long max,
                                    @RequestParam(value = "offset",defaultValue = "0")Integer offset){
        return blogService.queryBlogByFollow(max,offset);
    }
}
