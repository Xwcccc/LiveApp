package com.zsxy.service;

import com.zsxy.dto.Result;
import com.zsxy.entity.VoucherOrder;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xwc
 * @since 2023-6
 */
public interface IVoucherOrderService extends IService<VoucherOrder> {

    Result seckillVoucherOrder(Long voucherId);

    void createOneService(VoucherOrder voucherOrder);

    Result killVoucherOrder(Long voucherId);

    Result createNormalOrder(Long voucherId);

    Result checkAll(Integer current);
}
