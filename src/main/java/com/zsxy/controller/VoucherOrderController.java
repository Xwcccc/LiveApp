package com.zsxy.controller;


import com.zsxy.dto.Result;
import com.zsxy.service.impl.VoucherOrderServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("kill/{id}")
    public Result killVoucher(@PathVariable("id") Long voucherId) {
        return voucherOrderService.killVoucherOrder(voucherId);
    }

    @GetMapping("/check-orders")
    public Result checkVoucherOrder(@RequestParam(value = "current",defaultValue = "1")Integer current){
        return voucherOrderService.checkAll(current);
    }
}
