package com.zsxy.controller;


import com.zsxy.dto.Result;
import com.zsxy.entity.ShopType;
import com.zsxy.service.IShopTypeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@RequestMapping("/shop-type")
public class ShopTypeController {
    @Resource
    private IShopTypeService typeService;

    @GetMapping("list")
    public Result queryTypeList() {
        List<ShopType> typeList = typeService
                .queryList();
        if(typeList == null){
            return Result.fail("已浏览到底部!");
        }
        return Result.ok(typeList);
    }
}
