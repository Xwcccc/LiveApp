package com.zsxy.service;

import com.zsxy.dto.Result;
import com.zsxy.entity.VoucherOrder;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IVoucherOrderService extends IService<VoucherOrder> {

    Result seckillVoucherOrder(Long voucherId);

    void createOneService(VoucherOrder voucherOrder);

}
