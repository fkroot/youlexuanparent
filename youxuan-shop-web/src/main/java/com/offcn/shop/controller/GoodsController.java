package com.offcn.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.entity.Goods;
import com.offcn.entity.PageResult;
import com.offcn.entity.Result;
import com.offcn.pojo.TbGoods;
import com.offcn.search.service.ItemSearchService;
import com.offcn.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.List;

/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {
/*	@Autowired*/


	@Reference
	private GoodsService goodsService;
	@Reference
	private ItemSearchService itemSearchService;

	@Autowired
	private Destination topicDeletePageDestination;
	@Autowired
	private JmsTemplate jmsTemplate;



	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return goodsService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods){

		System.out.println("进入了添加方法");
		String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
		System.out.println(sellerId);
		System.out.println(goods);
		goods.getGoods().setSellerId(sellerId);


		try {
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		Goods one = goodsService.findOne(goods.getGoods().getId());

		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		String sellerId = goods.getGoods().getSellerId();
		if(!one.getGoods().getSellerId().equals(name) || !sellerId.equals(name)){
                 return new Result(false,"你的操作不合法");
		}


		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			goodsService.delete(ids);

			itemSearchService.deleteGoodsByIds(ids);
			jmsTemplate.send(topicDeletePageDestination, new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});


			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){



		String name = SecurityContextHolder.getContext().getAuthentication().getName();

		goods.setSellerId(name);

		return goodsService.findPage(goods, page, rows);		
	}

	
}
