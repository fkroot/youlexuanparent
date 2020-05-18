package com.offcn.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeCancelRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeCancelResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.offcn.pay.service.AliPayService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
@Service
public class AliPayServiceImpl implements AliPayService {

    @Autowired
    private AlipayClient alipayClient;

    @Override
    public Map createNative(String out_trade_no, String total_fee) {

        Map<String,String> map = new HashMap<>();
        //创建预下单请求对象

        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();

        //转换下单金额
        long total = Long.parseLong(total_fee);
        BigDecimal bigDecimal =  BigDecimal.valueOf(total);
        BigDecimal devide = BigDecimal.valueOf(100d);
        /*分转换为元*/
        BigDecimal Yuan = bigDecimal.divide(devide);
        System.out.println("用户下订单的预算金额"+devide);

        request.setBizContent("{" +
                "    \"out_trade_no\":\""+out_trade_no+"\"," +
                "    \"total_amount\":\""+Yuan.doubleValue()+"\"," +
                "    \"subject\":\"测试购买商品001\"," +
                "    \"store_id\":\"xa_001\"," +
                "    \"timeout_express\":\"90m\"}");//设置业务参数
        //发出预下单业务请求

        try {
            AlipayTradePrecreateResponse response = alipayClient.execute(request);
            //获得响应结果
            System.out.println("响应的状态码"+response.getCode());
            //响应体：全部响应结果
            String body = response.getBody();
            System.out.println("响应体中的结果"+body);

            if(response.getCode().equals("10000")){
                map.put("qrcode", response.getQrCode());
                map.put("out_trade_no", response.getOutTradeNo());
                map.put("total_fee",total_fee);
                System.out.println("qrcode:"+response.getQrCode());
                System.out.println("out_trade_no:"+response.getOutTradeNo());
                System.out.println("total_fee:"+total_fee);

            }else {
                System.out.println("预下单失败"+body);
            }


        } catch (AlipayApiException e) {
            e.printStackTrace();
        }


        return map;
    }

    @Override
    public Map<String,String> queryPayStatus(String out_trade_no) {
Map<String,String> map = new HashMap<>();

        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent("{" +
                "    \"out_trade_no\":\""+out_trade_no+"\"," +
                "    \"trade_no\":\"\"}"); //设置业务参数

        //发出请求
        try {
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            String code = response.getCode();
            System.out.println("验证查询订单支付状态的响应码"+code);
            if(code.equals("10000")){
                //System.out.println("返回值2:"+response.getBody());
                map.put("out_trade_no", out_trade_no);
                map.put("tradestatus", response.getTradeStatus());
                map.put("trade_no",response.getTradeNo());

            }

        } catch (AlipayApiException e) {
            e.printStackTrace();
        }


        return map;
    }

    @Override
    public Map closePayWindow(String out_trade_no) {
        Map<String,String> map=new HashMap<String, String>();
        //撤销交易请求对象
        AlipayTradeCancelRequest request = new AlipayTradeCancelRequest();
        request.setBizContent("{" +
                "    \"out_trade_no\":\""+out_trade_no+"\"," +
                "    \"trade_no\":\"\"}"); //设置业务参数

        try {
            AlipayTradeCancelResponse response = alipayClient.execute(request);
            String code=response.getCode();

            if(code.equals("10000")){

                System.out.println("返回值:"+response.getBody());
                map.put("code", code);
                map.put("out_trade_no", out_trade_no);
                return map;
            }
        } catch (AlipayApiException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

        return null;


    }
}
