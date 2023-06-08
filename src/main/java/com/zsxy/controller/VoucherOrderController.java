package com.zsxy.controller;


import com.zsxy.dto.Result;
import com.zsxy.service.impl.VoucherOrderServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author xwc
 * @since 2023-6-3
 */
@RestController
@RequestMapping("/voucher-order")
public class VoucherOrderController {
    @Autowired
    VoucherOrderServiceImpl voucherOrderService;

    @PostMapping("seckill/{id}")
    public Result seckillVoucher(@PathVariable("id") Long voucherId) {
        return voucherOrderService.seckillVoucherOrder(voucherId);
    }
}
