package com.zsxy.service.impl;

import cn.hutool.json.JSONUtil;
import com.zsxy.entity.ShopType;
import com.zsxy.mapper.ShopTypeMapper;
import com.zsxy.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static com.zsxy.utils.RedisConstants.CACHE_SHOP_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xwc
 * @since 2023-6
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Override
    public List<ShopType> queryList() {
        List<String> list = new ArrayList<>();
        list = stringRedisTemplate.opsForList().range(CACHE_SHOP_KEY+"tb_shop_type",0,-1);
        List<ShopType> result = new ArrayList<>();

        //这里一定要用isEmpty，==是比较地址啊
        if(!list.isEmpty()){
            for(String x: list){
                result.add(JSONUtil.toBean(x,ShopType.class));
            }
            return result;
        }
        List<ShopType> tempResult = new ArrayList<>();
        tempResult = query().orderByAsc("sort").list();
        if(tempResult.isEmpty()){
            return null;
        }
        List<String> listResult = new ArrayList<>();
        for (ShopType y: tempResult){
            listResult.add(JSONUtil.toJsonStr(y));
        }
        stringRedisTemplate.opsForList().rightPushAll(CACHE_SHOP_KEY+"tb_shop_type",listResult);
        return tempResult;
    }

}
