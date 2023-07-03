package com.zsxy.controller;


import com.zsxy.dto.Result;
import com.zsxy.entity.ShopComments;
import com.zsxy.service.impl.ShopCommentsServiceImpl;
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
@RequestMapping("/shop-comments")
public class ShopCommentsController {

    @Resource
    private ShopCommentsServiceImpl shopCommentsService;

    @GetMapping("/check-list/{id}/{current}")
    public Result checkAllComments(@PathVariable("id") Long shop_id, @PathVariable Integer current ){
        return shopCommentsService.checkAll(shop_id,current);
    }

    @PutMapping("/like-comment/{id}")
    public Result likeBlogComment(@PathVariable("id") Long id) {
        return shopCommentsService.like(id);
    }

    @PostMapping("/add")
    public Result addComments(@RequestBody ShopComments shopComments){
        return shopCommentsService.addCommentsById(shopComments);
    }

    @GetMapping("/{id}")
    public Result queryCommentById(@PathVariable(value = "id") Long id){
        return shopCommentsService.queryComment(id);
    }
}
