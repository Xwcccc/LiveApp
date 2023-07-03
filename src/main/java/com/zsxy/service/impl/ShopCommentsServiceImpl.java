package com.zsxy.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsxy.dto.Result;
import com.zsxy.dto.UserDTO;
import com.zsxy.entity.BlogComments;
import com.zsxy.entity.ShopComments;
import com.zsxy.entity.User;
import com.zsxy.mapper.ShopCommentsMapper;
import com.zsxy.service.IShopCommentsService;
import com.zsxy.utils.SystemConstants;
import com.zsxy.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

import static com.zsxy.utils.RedisConstants.*;

@Service
public class ShopCommentsServiceImpl extends ServiceImpl<ShopCommentsMapper, ShopComments> implements IShopCommentsService {
    @Resource
    private UserServiceImpl userService;
    @Resource
    private ShopServiceImpl shopService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result checkAll(Long shop_id, Integer current) {
        Page<ShopComments> page = query()
                .eq("shop_id",shop_id)
                .orderByDesc("liked")
                .page(new Page<>(current, 6));
        // 获取当前页数据
        List<ShopComments> records = page.getRecords();
        // 查询用户
        records.forEach(shopComments ->{
            this.queryShopCommentsUser(shopComments);
            this.queryLike(shopComments);
        });
        return Result.ok(records);
    }

    @Override
    public Result like(Long id) {
        // 修改点赞数量
        String key = Shop_Comment_LIKED_KEY+id;
        Long userId = UserHolder.getUser().getId();
        Double score = stringRedisTemplate.opsForZSet().score(key,userId.toString());
        if(score == null){
            boolean success = update().setSql("liked = liked + 1").eq("id", id).update();
            if(success) {
                stringRedisTemplate.opsForZSet().add(key,userId.toString(),System.currentTimeMillis());
            }
        }else{
            boolean success = update().setSql("liked = liked - 1").eq("id", id).update();
            if(success) {
                stringRedisTemplate.opsForZSet().remove(key,userId.toString());
            }
        }
        ShopComments shopComments = getById(id);
        shopComments.setIsLike(true);
        return Result.ok(shopComments);
    }

    @Override
    public Result addCommentsById(ShopComments blogComments) {
        UserDTO user = UserHolder.getUser();
        blogComments.setUserId(user.getId());
        Long blogId = blogComments.getShopId();
        shopService.update().setSql("comments = comments + 1").eq("id", blogId).update();
        // 保存探店博文
        boolean isSuccess = save(blogComments);
        if(!isSuccess){
            return Result.fail("数据库存取失败！");
        }
        // 返回id
        return Result.ok(blogComments.getId());
    }

    @Override
        public Result queryComment(Long id) {
        ShopComments shopComments = getById(id);
        this.queryShopCommentsUser(shopComments);
        this.queryLike(shopComments);
        return Result.ok(shopComments);
    }

    public void queryLike(ShopComments shopComments){
        UserDTO user = UserHolder.getUser();
        if(user == null){
            return;
        }
        Long userId = user.getId();
        String key = Shop_Comment_LIKED_KEY+shopComments.getId();
        Double score = stringRedisTemplate.opsForZSet().score(key,userId.toString());
        if((score != null)){
            shopComments.setIsLike(true);
        }
    }

    public void queryShopCommentsUser(ShopComments shopComments){
        Long userId = shopComments.getUserId();
        User user = userService.getById(userId);
        shopComments.setName(user.getNickName());
        shopComments.setIcon(user.getIcon());
    }
}
