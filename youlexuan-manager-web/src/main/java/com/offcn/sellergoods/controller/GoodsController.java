package com.offcn.sellergoods.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.offcn.entity.Goods;
import com.offcn.entity.PageResult;
import com.offcn.entity.Result;

import com.offcn.pojo.TbGoods;
import com.offcn.pojo.TbItem;

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

	@Reference
	private GoodsService goodsService;

	@Autowired
	private Destination queueSolrDestination;

	@Autowired
	private JmsTemplate jsmTemplate;

	@Autowired
    private Destination queueSolrDeleteDestination;
    @Autowired
    private Destination topicPageDestination;


	
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

		String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();

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
		/*	itemSearchService.deleteGoodsByIds(ids);*/
            jsmTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    //此处ids为一个数组，传递一个Object过去
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

		System.out.println(goods.getAuditStatus()+"2");



		PageResult page1 = goodsService.findPage(goods, page, rows);

		return page1;
	}
@RequestMapping("updateStatus")
	public Result updateStatus(Long[] ids,String status){

	try {
		goodsService.updateStatus(ids,status);
		if(status.equals("1")){
			List<TbItem> item = goodsService.findItemByGoodsIdAndStatus(ids, status);
			if(item.size()>0){
			/*	itemSearchService.importDataList(item);*/
				String itemList = JSON.toJSONString(item);
                System.out.println("开始传递审核通过的数据");
				jsmTemplate.send(queueSolrDestination, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        return session.createTextMessage(itemList);
                    }
                });





			}else {
				System.out.println("导入数据进入solr失败");
			}
			//静态页生成
			/*for(Long goodsId:ids){
				itemPageService.genItemHtml(goodsId);
			}*/

            for(Long goodsId:ids){
               jsmTemplate.send(topicPageDestination, new MessageCreator() {
                   @Override
                   public Message createMessage(Session session) throws JMSException {
                       return session.createTextMessage(goodsId+"");
                   }
               });
            }



		}
		return new Result(true, "修改成功");
	} catch (Exception e) {
		e.printStackTrace();
		return new Result(false, "修改失败");
	}

}
/*	@Reference(timeout=40000)
	private ItemPageService itemPageService;
	*//**
	 * 生成静态页（测试）
	 * @param goodsId
	 *//*
	@RequestMapping("/genHtml")
	public void genHtml(Long goodsId){
		itemPageService.genItemHtml(goodsId);
	}*/
	
}
