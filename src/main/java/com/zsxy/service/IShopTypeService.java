package com.zsxy.service;

import com.zsxy.entity.ShopType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xwc
 * @since 2023-6
 */
public interface IShopTypeService extends IService<ShopType> {

    List<ShopType> queryList();
}
