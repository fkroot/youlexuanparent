package com.offcn.pay.service;

import java.util.Map;

public interface AliPayService {
    /**
     * 生成支付宝支付二维码
     * @param out_trade_no 订单号
     * @param total_fee 金额(分)
     * @return
     */

    public Map createNative(String out_trade_no, String total_fee);

    //根据订单号查询当前订单状态
    public Map queryPayStatus(String out_trade_no);
//当支付超时之后，关闭支付宝支付窗口
    public Map closePayWindow(String out_trade_no);

}
