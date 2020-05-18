package com.offcn.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.entity.Spec;
import com.offcn.mapper.TbSpecificationMapper;
import com.offcn.mapper.TbSpecificationOptionMapper;
import com.offcn.pojo.TbSpecification;
import com.offcn.pojo.TbSpecificationExample;
import com.offcn.pojo.TbSpecificationExample.Criteria;
import com.offcn.pojo.TbSpecificationOption;
import com.offcn.pojo.TbSpecificationOptionExample;
import com.offcn.sellergoods.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SpecificationServiceImpl implements SpecificationService {

	@Autowired
	private TbSpecificationMapper specificationMapper;


	@Autowired
	private TbSpecificationOptionMapper tbSpecificationOptionMapper;

	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSpecification> findAll() {
		return specificationMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSpecification> page=   (Page<TbSpecification>) specificationMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Spec spec) {

		specificationMapper.insert(spec.getSpecification());//插入规格
		//循环插入规格选项
		for(TbSpecificationOption specificationOption:spec.getSpecificationOptionList()){
			specificationOption.setSpecId(spec.getSpecification().getId());//设置规格ID
			tbSpecificationOptionMapper.insert(specificationOption);
		}





	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(Spec specification){
		specificationMapper.updateByPrimaryKey(specification.getSpecification());

		List<TbSpecificationOption> list = specification.getSpecificationOptionList();

		TbSpecificationOptionExample example = new TbSpecificationOptionExample();
		TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
		criteria.andSpecIdEqualTo(specification.getSpecification().getId());
		 tbSpecificationOptionMapper.deleteByExample(example);


		for (TbSpecificationOption option : list) {

			tbSpecificationOptionMapper.insert(option);

		}


	}
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Spec findOne(Long id){

		Spec spec = new Spec();

		TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey(id);
		spec.setSpecification(tbSpecification);

		TbSpecificationOptionExample example = new TbSpecificationOptionExample();
		TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
		criteria.andSpecIdEqualTo(tbSpecification.getId());
		List<TbSpecificationOption> tbSpecificationOptions = tbSpecificationOptionMapper.selectByExample(example);
		spec.setSpecificationOptionList(tbSpecificationOptions);

		return spec;

	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {

		for (Long id : ids) {
			TbSpecificationOptionExample example = new TbSpecificationOptionExample();
			TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
			criteria.andSpecIdEqualTo(id);
			tbSpecificationOptionMapper.deleteByExample(example);

		}

		for (Long id : ids) {
			specificationMapper.deleteByPrimaryKey(id);
		}


	}
	
	
		@Override
	public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSpecificationExample example=new TbSpecificationExample();
		Criteria criteria = example.createCriteria();
		
		if(specification!=null){			
						if(specification.getSpecName()!=null && specification.getSpecName().length()>0){
                                      				criteria.andSpecNameLike("%"+specification.getSpecName()+"%");
                                      			}	
		}
		
		Page<TbSpecification> page= (Page<TbSpecification>)specificationMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<Map> findSpecList() {
		return specificationMapper.selectOptionList();
	}

}
