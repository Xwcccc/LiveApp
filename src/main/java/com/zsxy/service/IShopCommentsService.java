package com.zsxy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zsxy.dto.Result;
import com.zsxy.entity.ShopComments;

public interface IShopCommentsService extends IService<ShopComments> {

    Result checkAll(Long shop_id, Integer current);

    Result like(Long id);

    Result addCommentsById(ShopComments shopComments);

    Result queryComment(Long id);
}
