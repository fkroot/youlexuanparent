package com.offcn.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.mapper.TbItemCatMapper;
import com.offcn.pojo.TbItemCat;
import com.offcn.pojo.TbItemCatExample;
import com.offcn.pojo.TbItemCatExample.Criteria;
import com.offcn.sellergoods.service.ItemCatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * 商品类目服务实现层
 * @author Administrator
 *
 */
@Service
public class ItemCatServiceImpl implements ItemCatService {

	@Autowired
	private TbItemCatMapper item_catMapper;

	@Autowired
	private RedisTemplate redisTemplate;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbItemCat> findAll() {
		return item_catMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbItemCat> page=   (Page<TbItemCat>) item_catMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbItemCat item_cat) {
		item_catMapper.insert(item_cat);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbItemCat item_cat){
		item_catMapper.updateByPrimaryKey(item_cat);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbItemCat findOne(Long id){
		return item_catMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			item_catMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbItemCat item_cat, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbItemCatExample example=new TbItemCatExample();
		Criteria criteria = example.createCriteria();
		
		if(item_cat!=null){			
						if(!("null".equals(String.valueOf(item_cat.getParentId())))){
				criteria.andParentIdEqualTo(item_cat.getParentId());
			}	
		}
		
		Page<TbItemCat> page= (Page<TbItemCat>)item_catMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<TbItemCat> findByParentId(Long parentId) {
		TbItemCatExample example = new TbItemCatExample();
		Criteria criteria = example.createCriteria();
		criteria.andParentIdEqualTo(parentId);
//每次进行读取的时候，一次性读取缓存进行存储

		List<TbItemCat> all = findAll();

		for (TbItemCat tbItemCat : all) {
			redisTemplate.boundHashOps("itemCat").put(tbItemCat.getName(),tbItemCat.getTypeId());
			System.out.println("更新缓存，商品名称和模板id");

		}


		return item_catMapper.selectByExample(example);
	}

	/**
	 * 根据上级ID查询列表
	 */

}
