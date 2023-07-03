package com.zsxy.controller;


import com.zsxy.dto.Result;
import com.zsxy.service.impl.FollowServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/follow")
public class FollowController {
    @Resource
    private FollowServiceImpl followService;

    @GetMapping("/check-fans")
    public Result checkFollow(@RequestParam(value = "current",defaultValue = "1")Integer current){
        return followService.checkAllFollow(current);
    }

    @PutMapping("/{id}/{isFollow}")
    public Result follow(@PathVariable(value = "id") Long blogUserId, @PathVariable(value = "isFollow")Boolean isFollow){
        return followService.follow(blogUserId,isFollow);
    }

    @GetMapping("/or/not/{id}")
    public Result checkIsFollow(@PathVariable(value = "id") Long id){
        return followService.checkIsFollow(id);
    }

    @GetMapping("/common/{blogerId}/{f_current}")
    public Result getCommonFollow(@PathVariable("blogerId") Long id,@PathVariable("f_current") Integer f_current){
        return followService.getCommonFollow(id,f_current);
    }
}
