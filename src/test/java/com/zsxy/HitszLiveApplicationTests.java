package com.zsxy;

import com.zsxy.entity.Shop;
import com.zsxy.service.impl.ShopServiceImpl;
import com.zsxy.utils.RedisIdWorker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@SpringBootTest
class HitszLiveApplicationTests {

    @Resource
    private RedisIdWorker redisIdWorker;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ShopServiceImpl shopService;

    private ThreadPoolExecutor executorService = new ThreadPoolExecutor(
            50,
            400,
            1, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(100),
            new ThreadPoolExecutor.CallerRunsPolicy());

     @Test
     void testIdWorker() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(300);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < 300; i++){
                    long result = redisIdWorker.nextID("Coupons");
                    System.out.println(result);
                }
            }
         };
        long begin = System.currentTimeMillis();
        for (int j = 0; j < 300; j++){
            executorService.submit(r);
        }
        //使得300个分线程执行完之后再执行main线程
        latch.await();
        long end = System.currentTimeMillis();
        System.out.println("time = " + (end - begin));
     }

     @Test
    void loadShopData(){
         List<Shop> shopList = shopService.list();
         Map<Long,List<Shop>> shopTypeMap = shopList.stream().collect(Collectors.groupingBy(Shop::getTypeId));
         for (Map.Entry<Long,List<Shop>> entry : shopTypeMap.entrySet()){
             Long typeId = entry.getKey();
             String key = "shop:geo:" + typeId;
             List<Shop> values = entry.getValue();
             List<RedisGeoCommands.GeoLocation<String>> locationList = new ArrayList<>();
             for (Shop shop: values){
                locationList.add(new RedisGeoCommands.GeoLocation<>(shop.getId().toString(),new Point(shop.getX(),shop.getY())));
             }
             stringRedisTemplate.opsForGeo().add(key,locationList);
         }
     }
}
