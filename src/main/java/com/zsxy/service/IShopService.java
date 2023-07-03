package com.zsxy.service;

import com.zsxy.dto.Result;
import com.zsxy.entity.BlogComments;
import com.zsxy.entity.Shop;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xwc
 * @since 2023-6
 */
public interface IShopService extends IService<Shop> {

    Result queryById(Long id);


    Result update(Shop shop);

    Result queryShopByType(Integer typeId, Integer current);

    Result addXY(Double lot, Double lat);
}
