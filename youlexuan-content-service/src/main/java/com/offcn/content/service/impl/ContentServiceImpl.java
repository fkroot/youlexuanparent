package com.offcn.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.content.service.ContentService;
import com.offcn.entity.PageResult;
import com.offcn.mapper.TbContentMapper;
import com.offcn.pojo.TbContent;
import com.offcn.pojo.TbContentExample;
import com.offcn.pojo.TbContentExample.Criteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;

	@Autowired
	private RedisTemplate redisTemplate;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbContent> page=   (Page<TbContent>) contentMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContent content) {
		contentMapper.insert(content);

		redisTemplate.boundHashOps("content").delete(content.getCategoryId());
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content){

//先删除修改之前的缓存

		TbContent tbContent = contentMapper.selectByPrimaryKey(content.getId());

		redisTemplate.boundHashOps("content").delete(tbContent.getCategoryId());
		contentMapper.updateByPrimaryKey(content);
		//如果分类id发生了修改删除修改之后的缓存

		if(tbContent.getCategoryId() != content.getCategoryId()){
			redisTemplate.boundHashOps("content").delete(content.getCategoryId());
		}

	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			contentMapper.deleteByPrimaryKey(id);
			TbContent tbContent = contentMapper.selectByPrimaryKey(id);
			Long categoryId = tbContent.getCategoryId();
			redisTemplate.boundHashOps("content").delete(categoryId);

		}		
	}
	
	
		@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbContentExample example=new TbContentExample();
		Criteria criteria = example.createCriteria();
		
		if(content!=null){			
						if(content.getTitle()!=null && content.getTitle().length()>0){
                                      				criteria.andTitleLike("%"+content.getTitle()+"%");
                                      			}			if(content.getUrl()!=null && content.getUrl().length()>0){
                                      				criteria.andUrlLike("%"+content.getUrl()+"%");
                                      			}			if(content.getPic()!=null && content.getPic().length()>0){
                                      				criteria.andPicLike("%"+content.getPic()+"%");
                                      			}			if(content.getStatus()!=null && content.getStatus().length()>0){
                                      				criteria.andStatusLike("%"+content.getStatus()+"%");
                                      			}	
		}

		criteria.andStatusEqualTo("1");
		example.setOrderByClause("sort_order");

		Page<TbContent> page= (Page<TbContent>)contentMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<TbContent> findByCategoryId(Long categoryId) {

		List<TbContent> content = (List<TbContent>) redisTemplate.boundHashOps("content").get(categoryId);

if(content == null){
	TbContentExample example = new TbContentExample();
	Criteria criteria = example.createCriteria();
	criteria.andStatusEqualTo("1");
	criteria.andCategoryIdEqualTo(categoryId);
	example.setOrderByClause("sort_order");
	List<TbContent> tbContents = contentMapper.selectByExample(example);

	redisTemplate.boundHashOps("content").put(categoryId,tbContents);
	return tbContents;

       }else {
	System.out.println("从缓存中获取的数据");
       }
	return content;

	}

}
