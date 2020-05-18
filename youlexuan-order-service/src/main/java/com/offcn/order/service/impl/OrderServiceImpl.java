package com.offcn.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.Cart;
import com.offcn.entity.PageResult;
import com.offcn.mapper.TbOrderItemMapper;
import com.offcn.mapper.TbOrderMapper;
import com.offcn.mapper.TbPayLogMapper;
import com.offcn.order.service.OrderService;
import com.offcn.pojo.TbOrder;
import com.offcn.pojo.TbOrderExample;
import com.offcn.pojo.TbOrderExample.Criteria;
import com.offcn.pojo.TbOrderItem;
import com.offcn.pojo.TbPayLog;
import com.offcn.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;
	@Autowired
	private TbOrderItemMapper tbOrderItemMapper;
	@Autowired
	private IdWorker idWorker;
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private TbPayLogMapper tbPayLogMapper;

	
	/**
	 * 查询全部
	 */

	//在执行添加操作时，将获得的数据插入两张表中




	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbOrder order) {
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
		//订单列表
		List<String> orderList = new ArrayList<>();
		double total_money = 0;


		for (Cart cart : cartList) {
			long orderId = idWorker.nextId();
            TbOrder tbOrder = new TbOrder();
             tbOrder.setOrderId(orderId);
             tbOrder.setUserId(order.getUserId());
             tbOrder.setPaymentType(order.getPaymentType());
             tbOrder.setStatus("1");//未付款
			tbOrder.setCreateTime(new Date());
			tbOrder.setUpdateTime(new Date());
			tbOrder.setReceiverAreaName(order.getReceiverAreaName());
			tbOrder.setReceiverMobile(order.getReceiverMobile());
			tbOrder.setReceiver(order.getReceiver());
			tbOrder.setSourceType(order.getSourceType());
			tbOrder.setSellerId(cart.getSellerId());

			double money = 0;
			for (TbOrderItem tbOrderItem : cart.getTbOrderItems()) {
                          tbOrderItem.setId(idWorker.nextId());
                          tbOrderItem.setOrderId(orderId);
				tbOrderItem.setSellerId(cart.getSellerId());

				money += money + tbOrderItem.getTotalFee().doubleValue();
				tbOrderItemMapper.insert(tbOrderItem);
				orderList.add(orderId+"");//添加到订单列表
				total_money+=money;//累加到总金额


			}

			if("1".equals(tbOrder.getPaymentType())){//支付宝支付
				TbPayLog tbPayLog = new TbPayLog();
				String outOrderId = idWorker.nextId()+"";
				tbPayLog.setCreateTime(new Date());
				tbPayLog.setOutTradeNo(outOrderId);
				String orderListString = orderList.toString().replace("[", "").replace("]", "").replace(" ", "");
				tbPayLog.setOrderList(orderListString);
				tbPayLog.setTotalFee(Long.parseLong(total_money*100+""));
				tbPayLog.setPayType("1");
				tbPayLog.setTradeState("0");
				tbPayLog.setUserId(order.getUserId());
				tbPayLogMapper.insert(tbPayLog);
				redisTemplate.boundHashOps("payLog").put(order.getUserId(),tbPayLog);//放入缓存


			}

			tbOrder.setPayment(new BigDecimal(money));
			orderMapper.insert(tbOrder);
     redisTemplate.boundHashOps("cartList").delete(order.getUserId());


		}


	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param
	 * @return
	 */
	@Override
	public TbOrder findOne(Long orderId){
		return orderMapper.selectByPrimaryKey(orderId);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] orderIds) {
		for(Long orderId:orderIds){
			orderMapper.deleteByPrimaryKey(orderId);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
                                      				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
                                      			}			if(order.getPostFee()!=null && order.getPostFee().length()>0){
                                      				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
                                      			}			if(order.getStatus()!=null && order.getStatus().length()>0){
                                      				criteria.andStatusLike("%"+order.getStatus()+"%");
                                      			}			if(order.getShippingName()!=null && order.getShippingName().length()>0){
                                      				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
                                      			}			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
                                      				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
                                      			}			if(order.getUserId()!=null && order.getUserId().length()>0){
                                      				criteria.andUserIdLike("%"+order.getUserId()+"%");
                                      			}			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
                                      				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
                                      			}			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
                                      				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
                                      			}			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
                                      				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
                                      			}			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
                                      				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
                                      			}			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
                                      				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
                                      			}			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
                                      				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
                                      			}			if(order.getReceiver()!=null && order.getReceiver().length()>0){
                                      				criteria.andReceiverLike("%"+order.getReceiver()+"%");
                                      			}			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
                                      				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
                                      			}			if(order.getSourceType()!=null && order.getSourceType().length()>0){
                                      				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
                                      			}			if(order.getSellerId()!=null && order.getSellerId().length()>0){
                                      				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
                                      			}	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public TbPayLog searchPayLogFromRedis(String userName) {
		return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userName);
	}

	@Override
	public void updateOrderStatus(String out_trade_no, Object trade_no) {
		TbPayLog tbPayLog = tbPayLogMapper.selectByPrimaryKey(out_trade_no);
		tbPayLog.setPayTime(new Date());
		tbPayLog.setTradeState("1");
		tbPayLog.setTransactionId(trade_no+"");
		tbPayLogMapper.updateByPrimaryKey(tbPayLog);
		//修改order表的订单状态
		String orderList = tbPayLog.getOrderList();
		String[] OrderIdList = orderList.split(",");

		for (String s : OrderIdList) {
			TbOrder tbOrder = orderMapper.selectByPrimaryKey(Long.parseLong(s));
			if(null != tbOrder){
				tbOrder.setStatus("2");//已经付款
				orderMapper.updateByPrimaryKey(tbOrder);

			}

		}


       redisTemplate.boundHashOps("payLog").delete(tbPayLog.getUserId());

	}

}
