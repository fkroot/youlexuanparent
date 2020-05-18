package com.offcn.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.mapper.TbSeckillGoodsMapper;
import com.offcn.mapper.TbSeckillOrderMapper;
import com.offcn.pojo.TbSeckillGoods;
import com.offcn.pojo.TbSeckillOrder;
import com.offcn.pojo.TbSeckillOrderExample;
import com.offcn.pojo.TbSeckillOrderExample.Criteria;
import com.offcn.seckill.service.SeckillOrderService;
import com.offcn.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;

import java.util.Date;
import java.util.List;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private IdWorker idWorker;
	@Autowired
	private TbSeckillGoodsMapper tbSeckillGoodsMapper;

	@Override
	public void deleteFromRedis(String userId, Long sellerId) {

		TbSeckillOrder o = (TbSeckillOrder) redisTemplate.boundHashOps(userId).get(sellerId);
		if(null != o){
			//删除订单
			redisTemplate.boundHashOps(userId).delete(sellerId);
			//将缓存中 的商品的库存数量恢复
			TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(o.getSeckillId());
			if(null != seckillGoods){
				seckillGoods.setStockCount(seckillGoods.getStockCount()+1);

				redisTemplate.boundHashOps("seckillGoods").put(o.getSeckillId(),seckillGoods);
			}
		}

	}

	@Override
	public void saveOrderFromRedisToTable(String userId, Long orderId, String transactionId) {

		TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
                  if(null == seckillOrder){
                  	throw new RuntimeException("订单不存在");
				  }
                  if(orderId.longValue() != seckillOrder.getId().longValue()){
                  	throw new RuntimeException("订单号与支付订单号不匹配");
				  }
				  seckillOrder.setStatus("1");
                  seckillOrder.setPayTime(new Date());
                  seckillOrder.setTransactionId(transactionId);
		seckillOrderMapper.insert(seckillOrder);
		redisTemplate.boundHashOps("seckillOrder").delete(userId);

	}

	@Override
	public TbSeckillOrder searchOrderFromRedisByUserId(String userId) {
		TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
		return seckillOrder;
	}

	//使用redis管理事务，当有人下订单的时候，消减redis中的库存
	@Override
	public void submitOrder(Long seckillId,String userId) {
		//允许redis使用事务
            redisTemplate.setEnableTransactionSupport(true);
		Object Result = redisTemplate.execute(new SessionCallback<List<Object>>() {
			@Override
			public List<Object> execute(RedisOperations redisOperations) throws DataAccessException {
				redisTemplate.watch("seckillGoods");
				//在开启事务前执行查询，获取秒杀商品对象
				TbSeckillGoods seckillGoods = (TbSeckillGoods) redisOperations.boundHashOps("seckillGoods").get(seckillId);
            //开启事务
				redisOperations.multi();
               if(null == seckillGoods){
               	redisOperations.exec();
				   throw new RuntimeException("你秒杀的商品不存在");
			   }
			   if(seckillGoods.getStockCount() <= 0){
				   throw new RuntimeException("你秒杀的商品已经卖完了");
			   }
			   seckillGoods.setStockCount(seckillGoods.getStockCount()-1);
			   //将变化后的商品更新到redis
		redisOperations.boundHashOps("seckillGoods").put(seckillId,seckillGoods);
if(seckillGoods.getStockCount() == 0){
	tbSeckillGoodsMapper.updateByPrimaryKey(seckillGoods);
	redisTemplate.boundHashOps("seckillGoods").delete(seckillId);
}

//保存这份订单到redis

				TbSeckillOrder tbSeckillOrder = new TbSeckillOrder();
				tbSeckillOrder.setId(idWorker.nextId());
tbSeckillOrder.setCreateTime(new Date());
tbSeckillOrder.setStatus("0");
tbSeckillOrder.setMoney(seckillGoods.getCostPrice());
				tbSeckillOrder.setSellerId(seckillGoods.getSellerId());
				tbSeckillOrder.setUserId(userId);

				redisOperations.boundHashOps("seckillOrder").put(userId,tbSeckillOrder);

				//提交事务
				return redisOperations.exec();
			}
		});

//被 WATCH 的键会被监视，并会发觉这些键是否被改动过了。 如果有至少一个被监视的键在 EXEC 执行之前被修改了， 那么整个事务都会被取消，
// EXEC 返回空多条批量回复（null multi-bulk reply）来表示事务已经失败。
	}

	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillOrder> page=   (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder){
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id){
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillOrderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillOrderExample example=new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillOrder!=null){			
						if(seckillOrder.getUserId()!=null && seckillOrder.getUserId().length()>0){
                                      				criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
                                      			}			if(seckillOrder.getSellerId()!=null && seckillOrder.getSellerId().length()>0){
                                      				criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
                                      			}			if(seckillOrder.getStatus()!=null && seckillOrder.getStatus().length()>0){
                                      				criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
                                      			}			if(seckillOrder.getReceiverAddress()!=null && seckillOrder.getReceiverAddress().length()>0){
                                      				criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
                                      			}			if(seckillOrder.getReceiverMobile()!=null && seckillOrder.getReceiverMobile().length()>0){
                                      				criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
                                      			}			if(seckillOrder.getReceiver()!=null && seckillOrder.getReceiver().length()>0){
                                      				criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
                                      			}			if(seckillOrder.getTransactionId()!=null && seckillOrder.getTransactionId().length()>0){
                                      				criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
                                      			}	
		}
		
		Page<TbSeckillOrder> page= (Page<TbSeckillOrder>)seckillOrderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
	
}
