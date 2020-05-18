package com.offcn.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.Goods;
import com.offcn.entity.PageResult;
import com.offcn.mapper.*;
import com.offcn.pojo.*;
import com.offcn.pojo.TbGoodsExample.Criteria;
import com.offcn.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
/*@Transactional*/
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;

	@Autowired
	private TbGoodsDescMapper tbGoodsDescMapper;

	@Autowired
	private TbItemMapper itemMapper;

	@Autowired
	private TbBrandMapper brandMapper;

	@Autowired
	private TbItemCatMapper itemCatMapper;

	@Autowired
	private TbSellerMapper sellerMapper;


	@Override
	public List<TbItem> findItemByGoodsIdAndStatus(Long [] ids,String status) {
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdIn(Arrays.asList(ids));
		criteria.andStatusEqualTo(status);

		List<TbItem> tbItems = itemMapper.selectByExample(example);


		return tbItems;
	}

	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {


		goods.getGoods().setAuditStatus("0");
		TbGoods tbGoods = goods.getGoods();

		goodsMapper.insert(tbGoods);

		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());


		tbGoodsDescMapper.insert(goods.getGoodsDesc());

		if ("1".equals(goods.getGoods().getIsEnableSpec())) {
			for (TbItem tbItem : goods.getItemList()) {
				String title = goods.getGoods().getGoodsName();
				Map<String, Object> map = JSON.parseObject(tbItem.getSpec(), Map.class);

				for (String s : map.keySet()) {

					title += " " + map.get(s);


				}
				tbItem.setTitle(title);
				setItemValus(goods, tbItem);
				itemMapper.insert(tbItem);
			}
		} else {
			TbItem item = new TbItem();
			item.setTitle(goods.getGoods().getGoodsName());//商品KPU+规格描述串作为SKU名称
			item.setPrice(goods.getGoods().getPrice());//价格
			item.setStatus("1");//状态
			item.setIsDefault("1");//是否默认
			item.setNum(99999);//库存数量
			item.setSpec("{}");
			setItemValus(goods, item);
			itemMapper.insert(item);
		}


	}
	private void setItemValus (Goods goods, TbItem item){
		item.setGoodsId(goods.getGoods().getId());//商品SPU编号
		item.setSellerId(goods.getGoods().getSellerId());//商家编号
		item.setCategoryid(goods.getGoods().getCategory3Id());//商品分类编号（3级）
		item.setCreateTime(new Date());//创建日期
		item.setUpdateTime(new Date());//修改日期

		//品牌名称
		TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
		item.setBrand(brand.getName());
		//分类名称
		TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
		item.setCategory(itemCat.getName());

		//商家名称
		TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
		item.setSeller(seller.getNickName());

		//图片地址（取spu的第一个图片）
		List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
		if (imageList.size() > 0) {
			item.setImage((String) imageList.get(0).get("url"));
		}
	}



	
	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
//此处为修改后的商品，重新设置状态，未审核
goods.getGoods().setAuditStatus("0");

		TbGoods goods1 = goods.getGoods();
		goodsMapper.updateByPrimaryKey(goods1);

		tbGoodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());
		//第三张表先删除原来的数据，再把新的数据添加进去

		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(goods.getGoods().getId());
		itemMapper.deleteByExample(example);

		if ("1".equals(goods.getGoods().getIsEnableSpec())) {
			for (TbItem tbItem : goods.getItemList()) {
				String title = goods.getGoods().getGoodsName();
				Map<String, Object> map = JSON.parseObject(tbItem.getSpec(), Map.class);

				for (String s : map.keySet()) {

					title += " " + map.get(s);


				}
				tbItem.setTitle(title);
				setItemValus(goods, tbItem);
				itemMapper.insert(tbItem);
			}
		} else {
			TbItem item = new TbItem();
			item.setTitle(goods.getGoods().getGoodsName());//商品KPU+规格描述串作为SKU名称
			item.setPrice(goods.getGoods().getPrice());//价格
			item.setStatus("1");//状态
			item.setIsDefault("1");//是否默认
			item.setNum(99999);//库存数量
			item.setSpec("{}");
			setItemValus(goods, item);
			itemMapper.insert(item);
		}




	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){

		Goods goods = new Goods();
		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
		       goods.setGoods(tbGoods);

		TbGoodsDesc tbGoodsDesc = tbGoodsDescMapper.selectByPrimaryKey(id);
		goods.setGoodsDesc(tbGoodsDesc);


		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(id);
		List<TbItem> tbItems = itemMapper.selectByExample(example);
		/*	itemMapper.selectByPrimaryKey()*/
		goods.setItemList(tbItems);
		return goods;


	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setIsDelete("1");
			goodsMapper.updateByPrimaryKey(goods);
	}
	//删除之后修改item表中的状态字段
		List<TbItem> itemByGoodsIdAndStatus = findItemByGoodsIdAndStatus(ids, "1");
		for (TbItem byGoodsIdAndStatus : itemByGoodsIdAndStatus) {
			byGoodsIdAndStatus.setStatus("0");
			itemMapper.updateByPrimaryKey(byGoodsIdAndStatus);
		}



	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();

		if(goods!=null){			
						if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
                                      				criteria.andSellerIdEqualTo(goods.getSellerId());
                                      			}			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
                                      				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
                                      			}			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
                                      				criteria.andAuditStatusEqualTo(goods.getAuditStatus());
                                      			}			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
                                      				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
                                      			}			if(goods.getCaption()!=null && goods.getCaption().length()>0){
                                      				criteria.andCaptionLike("%"+goods.getCaption()+"%");
                                      			}			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
                                      				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
                                      			}			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
                                      				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
                                      			}			if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				                                                    criteria.andIsDeleteNotEqualTo("1");
                                      			}	
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void updateStatus(Long[] ids, String status) {
		for(Long id:ids){
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setAuditStatus(status);
			goodsMapper.updateByPrimaryKey(goods);
		}

	}

}
