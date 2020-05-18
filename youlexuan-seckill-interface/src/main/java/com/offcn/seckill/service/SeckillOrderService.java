package com.offcn.seckill.service;

import com.offcn.entity.PageResult;
import com.offcn.pojo.TbSeckillOrder;

import java.util.List;

/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface SeckillOrderService {

	/**
	 * 返回全部列表
	 * @return
	 */
	//当用户超时5分钟之后，将redis中的订单删除，恢复库存

	public void deleteFromRedis(String userId,Long sellerId);


	//用户支付完成之后，删除redis中的订单，将订单保存到数据库中

	public void saveOrderFromRedisToTable(String userId,Long orderId,String transactionId);


	//在用户点击下单之后c从redis中拿到订单生成二维码

	public TbSeckillOrder searchOrderFromRedisByUserId(String userId);

	public void submitOrder(Long seckillId,String userId);

	public List<TbSeckillOrder> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);


	/**
	 * 增加
	*/
	public void add(TbSeckillOrder seckill_order);


	/**
	 * 修改
	 */
	public void update(TbSeckillOrder seckill_order);


	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbSeckillOrder findOne(Long id);


	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long[] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbSeckillOrder seckill_order, int pageNum, int pageSize);
	
}
