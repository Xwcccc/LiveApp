package com.zsxy.service.impl;

import com.zsxy.entity.SeckillVoucher;
import com.zsxy.mapper.SeckillVoucherMapper;
import com.zsxy.service.ISeckillVoucherService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 秒杀优惠券表，与优惠券是一对一关系 服务实现类
 * </p>
 *
 * @author xwc
 * @since 2023-6
 */
@Service
public class SeckillVoucherServiceImpl extends ServiceImpl<SeckillVoucherMapper, SeckillVoucher> implements ISeckillVoucherService {

}
