package com.zsxy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.zsxy.dto.Result;
import com.zsxy.entity.VoucherOrder;
import com.zsxy.mapper.VoucherOrderMapper;
import com.zsxy.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsxy.utils.RedisIdWorker;
import com.zsxy.utils.UserHolder;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xwc
 * @since 2023-6
 */
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {
    @Resource
    SeckillVoucherServiceImpl seckillVoucherService;
    @Resource
    RedisIdWorker redisIdWorker;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedissonClient redissonClient;
    private boolean isBegin = false;
    private static DefaultRedisScript<Long> SECKILL_SCRIPT;
    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }
    private IVoucherOrderService proxy;
    ThreadPoolExecutor seckillOrderExecutor = new ThreadPoolExecutor(
            10,
            500,
            30,
            TimeUnit.MINUTES,new SynchronousQueue<Runnable>(),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.AbortPolicy()
    );
    public class HandleSekill implements Runnable{
        String queueName = "stream.order";
        @Override
        public void run() {
           // while (!isBegin){
             //   System.out.println("关闭！！！！！！！");
           // }
            System.out.println("开始啦！！！！！！！");
            while (true){
                try {
                    //ID field value
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1","c1"),
                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                            StreamOffset.create(queueName, ReadOffset.lastConsumed())
                    );
                    if(list == null || list.isEmpty()){
                        continue;
                    }
                    Map<Object,Object> map = list.get(0).getValue();
                    VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(map,new VoucherOrder(),true);
                    voucherOrderHandler(voucherOrder);
                    stringRedisTemplate.opsForStream().acknowledge(queueName,"g1",list.get(0).getId());
                } catch (Exception e) {
                    log.error("订单处理异常1",e);
                    pendingListHandler();
                }
            }
        }
    }
    private void pendingListHandler(){
        String queueName = "stream.order";
        while (true){
            try {
                //ID field value
                List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                        Consumer.from("g1","c1"),
                        StreamReadOptions.empty().count(1),
                        StreamOffset.create(queueName, ReadOffset.from("0"))
                );
                if(list == null || list.isEmpty()){
                    break;
                }
                Map<Object,Object> map = list.get(0).getValue();
                VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(map,new VoucherOrder(),true);
                voucherOrderHandler(voucherOrder);
                stringRedisTemplate.opsForStream().acknowledge(queueName,"g1",list.get(0).getId());
            } catch (Exception e) {
                try{
                    //沉睡一会自动循环
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    @PostConstruct
    private void init(){
        seckillOrderExecutor.submit(new HandleSekill());
    }

    private void voucherOrderHandler(VoucherOrder voucherOrder) {
        Long userID = voucherOrder.getUserId();
        RLock rLock = redissonClient.getLock("order:"+userID);
        boolean lock = false;
        try {
            lock = rLock.tryLock(1,10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(!lock){
            return;
        }
        try{
            proxy.createOneService(voucherOrder);
        }finally {
           // isBegin = true;
            rLock.unlock();
        }
    }

    @Override
    public void createOneService(VoucherOrder voucherOrder) {
        Long userID = voucherOrder.getUserId();
        int count = query().eq("user_id", userID).eq("voucher_id", voucherOrder.getVoucherId()).count();
        // 5.2.判断是否存在
        if (count > 0) {
            // 用户已经购买过了
            return;
        }
        //4.有库存则扣除库存-1
        boolean success = seckillVoucherService.update().setSql("stock = stock - 1").eq("voucher_id",voucherOrder.getVoucherId()).gt("stock",0).update();
        if(!success){
            return;
        }
        //5.添加订单信息
        save(voucherOrder);
    }

    @Override
    public Result seckillVoucherOrder(Long voucherId)  {
        Long userId = UserHolder.getUser().getId();
        Long orderId = redisIdWorker.nextID("Coupons");
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(),userId.toString(),orderId.toString()
        );
        int r = result.intValue();
        if(r != 0){
            return Result.fail(r == 1 ? "抢购失败，库存不足" : "抢购失败，一个用户只能抢购一单");
        }
        proxy = (IVoucherOrderService) AopContext.currentProxy();
        //返回订单ID
        return Result.ok(orderId);
    }
}
/**
 *
 * 同步下单，串行执行，全程数据库查询更新
 @Override
 public Result seckillVoucherOrder(Long voucherId)  {
 //1.根据ID获得优惠券信息
 SeckillVoucher seckillVoucher = seckillVoucherService.getById(voucherId);
 //2.判断优惠券是否在有效期内
 if(seckillVoucher.getBeginTime().isAfter(LocalDateTime.now())){
 return Result.fail("秒杀券抢购尚未开始");
 }
 if(seckillVoucher.getEndTime().isBefore(LocalDateTime.now())){
 return Result.fail("秒杀券抢购已经结束");
 }
 //3.判断优惠券是否有库存
 if(seckillVoucher.getStock() < 1){
 return Result.fail("秒杀券抢购一空啦");
 }
 //最后实现一人一单
 Long userID = UserHolder.getUser().getId();
 RLock rLock = redissonClient.getLock("order:"+userID);
 boolean lock = false;
 try {
 lock = rLock.tryLock(1,10, TimeUnit.SECONDS);
 } catch (InterruptedException e) {
 e.printStackTrace();
 }

 //RedisLockImpl redisLock = new RedisLockImpl("order:"+userID,stringRedisTemplate);
 //boolean lock = redisLock.getLock(30L);

 if(!lock){
 return Result.fail("一次只允许下一单");
 }
 try{
 IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
 return proxy.createOneService(voucherId);
 }finally {
 rLock.unlock();
 }
 }

 @Override
 @Transactional
 public Result createOneService(Long voucherId){
 Long userID = UserHolder.getUser().getId();
 int count = query().eq("user_id", userID).eq("voucher_id", voucherId).count();
 // 5.2.判断是否存在
 if (count > 0) {
 // 用户已经购买过了
 return Result.fail("用户已经购买过一次！");
 }
 //4.有库存则扣除库存-1
 boolean success = seckillVoucherService.update().setSql("stock = stock - 1").eq("voucher_id",voucherId).gt("stock",0).update();
 if(!success){
 return Result.fail("秒杀券抢购一空啦");
 }
 //5.添加订单信息
 VoucherOrder voucherOrder = new VoucherOrder();
 long orderId = redisIdWorker.nextID("Coupons");
 voucherOrder.setId(orderId);
 voucherOrder.setUserId(UserHolder.getUser().getId());
 voucherOrder.setVoucherId(voucherId);
 save(voucherOrder);
 return Result.ok(orderId);
 }
 */



/**
 //内存限制问题：这个是JDK自带的阻塞队列，用的是JVM的内存，当订单量很大的时候，JVM搞不定
 //数据安全问题：宕机，数据丢失
 private BlockingQueue<VoucherOrder> blockingQueue = new ArrayBlockingQueue<>(1024*1024);
 public class HandleSekill implements Runnable{

@Override
public void run() {
while (true){
try {
VoucherOrder voucherOrder = blockingQueue.take();
voucherOrderHandler(voucherOrder);
} catch (InterruptedException e) {
e.printStackTrace();
}
}
}
}
 */
