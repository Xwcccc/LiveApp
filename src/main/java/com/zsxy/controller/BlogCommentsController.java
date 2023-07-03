package com.zsxy.controller;


import com.zsxy.dto.Result;
import com.zsxy.entity.BlogComments;
import com.zsxy.service.impl.BlogCommentsServiceImpl;
import com.zsxy.service.impl.BlogServiceImpl;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author xwc
 * @since 2023-6
 */
@RestController
@RequestMapping("/blog-comments")
public class BlogCommentsController {

    @Resource
    private BlogCommentsServiceImpl blogCommentsService;

    @GetMapping("/check-list/{id}/{current}")
    public Result checkAllComments(@PathVariable("id") Long blog_id, @PathVariable Integer current ){
        return blogCommentsService.checkAll(blog_id,current);
    }

    @PutMapping("/like-comment/{id}")
    public Result likeBlogComment(@PathVariable("id") Long id) {
        return blogCommentsService.like(id);
    }

    @PostMapping("/add")
    public Result addComments(@RequestBody BlogComments blogComments){
        return blogCommentsService.addCommentsById(blogComments);
    }

    @GetMapping("/{id}")
    public Result queryCommentById(@PathVariable(value = "id") Long id){
        return blogCommentsService.queryComment(id);
    }

    @GetMapping("/likes/{id}")
    public Result queryCommentLikes(@PathVariable(value = "id") Long id){
        return blogCommentsService.queryRankById(id);
    }

}
