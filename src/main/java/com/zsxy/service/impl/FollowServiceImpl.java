package com.zsxy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsxy.dto.Result;
import com.zsxy.dto.UserDTO;
import com.zsxy.entity.Follow;
import com.zsxy.entity.User;
import com.zsxy.mapper.FollowMapper;
import com.zsxy.service.IFollowService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsxy.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.zsxy.utils.RedisConstants.FOLLOW_KEY;
import static com.zsxy.utils.SystemConstants.DEFAULT_PAGE_SIZE;
import static com.zsxy.utils.SystemConstants.MAX_PAGE_SIZE;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xwc
 * @since 2023-6
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private UserServiceImpl userService;

    @Override
    public Result follow(Long blogUserId, Boolean isFollow) {
        Long userId = UserHolder.getUser().getId();
        String key = FOLLOW_KEY + userId;
        if(isFollow){
            Follow followD = new Follow();
            followD.setUserId(userId);
            followD.setFollowUserId(blogUserId);
            boolean isSuccess = save(followD);
            if(isSuccess){
                stringRedisTemplate.opsForSet().add(key,blogUserId.toString());
            }
        }else{
            boolean isSuccess = remove(new QueryWrapper<Follow>().eq("user_id",userId).eq("follow_user_id",blogUserId));
            if(isSuccess){
                stringRedisTemplate.opsForSet().remove(key,blogUserId.toString());
            }
        }
        return Result.ok();
    }

    @Override
    public Result checkIsFollow(Long id) {
        Long userId = UserHolder.getUser().getId();
        int result = query().eq("user_id",userId).eq("follow_user_id",id).count();
        return Result.ok(result > 0);
    }

    @Override
    public Result getCommonFollow(Long id,Integer current) {
        Long userId = UserHolder.getUser().getId();
        String userKey = FOLLOW_KEY + userId;
        String blogerKey = FOLLOW_KEY + id;
        Set<String> inter = stringRedisTemplate.opsForSet().intersect(userKey,blogerKey);
        if(inter == null || inter.isEmpty()){
            return Result.ok(Collections.emptyList());
        }
        List<Long> list = inter.stream().map(Long::valueOf).collect(Collectors.toList());
        List<UserDTO> userDTOList  = userService.listByIds(list).
                stream().map(user -> BeanUtil.copyProperties(user,UserDTO.class))
                        .collect(Collectors.toList());
        Integer fromIndex = DEFAULT_PAGE_SIZE*(current-1);
        Integer endIndex = userDTOList.size()-fromIndex;
        if(endIndex > current){
            endIndex = DEFAULT_PAGE_SIZE*current;
        }
        List<UserDTO> page = userDTOList.subList(fromIndex,endIndex);
        return Result.ok(page);
    }

    @Override
    public Result checkAllFollow(Integer current) {
        Long userId = UserHolder.getUser().getId();
        Page<Follow> followPage = query().eq("follow_user_id",userId).page(new Page<Follow>(current,10));
        List<Follow> followList = followPage.getRecords();
        List<UserDTO> fans = new ArrayList<>();
        for (Follow f: followList){
            List<User> u = userService.query().eq("id",f.getUserId()).list();
            if(u.isEmpty()){
                return Result.fail("不存在此用户");
            }
            fans.add(BeanUtil.copyProperties(u.get(0),UserDTO.class));
        }
        return Result.ok(fans);
    }
}
