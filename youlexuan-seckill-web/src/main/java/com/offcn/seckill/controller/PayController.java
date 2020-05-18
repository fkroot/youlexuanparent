package com.offcn.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.entity.Result;
import com.offcn.pay.service.AliPayService;
import com.offcn.pojo.TbSeckillOrder;
import com.offcn.seckill.service.SeckillOrderService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

@Reference
    private AliPayService aliPayService;
@Reference
    private SeckillOrderService seckillOrderService;


//时刻查询二维码的支付状态

    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {

        String userName = SecurityContextHolder.getContext().getAuthentication().getName();

        Result result = new Result();

        while (true) {
            int x = 0;
            Map<String, String> map = aliPayService.queryPayStatus(out_trade_no);

            if (null == map) {
                result = new Result(false, "支付时出现错误");
                break;
            }
            if (map.get("tradestatus") != null && map.get("tradestatus").equals("TRADE_SUCCESS")) {
                result = new Result(true, "支付成功");
                seckillOrderService.saveOrderFromRedisToTable(userName, Long.parseLong(out_trade_no), map.get("trade_no"));
                break;
            }

            if (map.get("tradestatus") != null && map.get("tradestatus").equals("TRADE_CLOSED")) {
                result = new Result(true, "未付款交易超时关闭，或支付完成后全额退款");
                break;

            }
            if (map.get("tradestatus") != null && map.get("tradestatus").equals("TRADE_FINISHED")) {
                result = new Result(true, "交易结束，不可退款");
                break;

            }
            try {
                Thread.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            }
                     x++;
            if (x>100){
                result = new Result(false,"支付超时");
                Map<String,String> map1 = aliPayService.closePayWindow(out_trade_no);
                if("10000".equals(map1.get("code"))){
                    System.out.println("用户支付超时，已经取消订单");
                    seckillOrderService.deleteFromRedis(userName,Long.valueOf(out_trade_no));
                }



                break;
            }

        }
        return result;

    }
//生成二维码
    @RequestMapping("/createNative")
     public Map createNative(){
       String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        TbSeckillOrder tbSeckillOrder = seckillOrderService.searchOrderFromRedisByUserId(userName);
        //如果订单存在
        if(null != tbSeckillOrder){
         long monney = (long)(tbSeckillOrder.getMoney().doubleValue()*100);

            Map aNative = aliPayService.createNative(tbSeckillOrder.getId() + "", monney + "");
            return aNative;
        }else {
            System.out.println("该用户没有订单，不能进行秒杀");
            return new HashMap();
        }
    }
     







}
