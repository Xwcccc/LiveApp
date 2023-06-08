package com.zsxy.controller;


import com.zsxy.dto.Result;
import com.zsxy.service.impl.FollowServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author xwc
 * @since 2023-6
 */
@RestController
@RequestMapping("/follow")
public class FollowController {
    @Autowired
    private FollowServiceImpl followService;

    @PutMapping("/{id}/{isFollow}")
    public Result follow(@PathVariable(value = "id") Long blogUserId, @PathVariable(value = "isFollow")Boolean isFollow){
        return followService.follow(blogUserId,isFollow);
    }

    @GetMapping("/or/not/{id}")
    public Result checkIsFollow(@PathVariable(value = "id") Long id){
        return followService.checkIsFollow(id);
    }

    @GetMapping("/common/{blogerId}")
    public Result getCommonFollow(@PathVariable("blogerId") Long id){
        return followService.getCommonFollow(id);
    }
}
