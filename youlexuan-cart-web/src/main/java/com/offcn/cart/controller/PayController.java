package com.offcn.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.entity.Result;
import com.offcn.order.service.OrderService;
import com.offcn.pay.service.AliPayService;
import com.offcn.pojo.TbPayLog;
import com.offcn.util.IdWorker;

import org.springframework.beans.factory.annotation.Autowired;
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
private OrderService orderService;


    /**
     * 生成二维码
     * @return
     */
// 实现思路：调用获取支付日志对象的方法，得到订单号和金额
    //获取当前用户
    @RequestMapping("/createNative")
    public Map createNative(){

        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
                TbPayLog payLog = orderService.searchPayLogFromRedis(userName);

                if(null != payLog){
                    return aliPayService.createNative(payLog.getOutTradeNo()+"",payLog.getTotalFee()+"");
                }else {
                    System.out.println("没有从redis中搜索数据");
                    return new HashMap();
                }






    }
    //查询订单状态
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
       Result result = new Result();

       //死循环一直等待用户扫描支付订单
       while (true){
           Map map = new HashMap();
           int i = 0;

           map = aliPayService.queryPayStatus(out_trade_no);

           if(null == map){
               result = new Result(false,"用户支付时出错");
               break;
           }
           if(null != map.get("tradestatus") && map.get("tradestatus").equals("TRADE_SUCCESS")){
               result = new Result(true,"用户支付成功");
               orderService.updateOrderStatus(out_trade_no,map.get("trade_no"));
               break;
           }
 if(null != map.get("tradestatus") && map.get("tradestatus").equals("TRADE_CLOSED")){
     result = new Result(true,"用户交易完成关闭，或者未支付超时关闭");
     break;
 }
 if(null != map.get("tradestatus") && map.get("tradestatus").equals("TRADE_FINISHED")){
     result = new Result(true,"交易结束，你没有权利选择退款，请联系商家自行退款");
     break;
 }

  i++;
if(i >= 100){

    result = new Result(false,"二维码已经失效啦");

    break;

}


       }
return result;

    }


}
