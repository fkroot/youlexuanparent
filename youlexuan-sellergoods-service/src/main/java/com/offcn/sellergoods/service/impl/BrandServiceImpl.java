package com.offcn.sellergoods.service.impl;

import java.util.List;
import java.util.Map;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.pojo.TbBrandExample;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.offcn.mapper.TbBrandMapper;
import com.offcn.pojo.TbBrand;
import com.offcn.sellergoods.service.BrandService;
@Service
public class BrandServiceImpl implements BrandService {
    @Autowired
    private TbBrandMapper brandMapper;

    @Override
    public List<TbBrand> findAll() {
        return brandMapper.selectByExample(null);
    }

    @Override
    public PageResult findPage(int page, int rows) {

        PageHelper.startPage(page,rows);

        Page<TbBrand> pageHelper = (Page<TbBrand>) brandMapper.selectByExample(null);

        return new PageResult(pageHelper.getTotal(),pageHelper.getResult());
    }

    @Override
    public void add(TbBrand tbBrand) {

        brandMapper.insert(tbBrand);
    }

    @Override
    public void update(TbBrand tbBrand) {

        brandMapper.updateByPrimaryKey(tbBrand);


    }

    @Override
    public TbBrand findOne(Long id) {
        return brandMapper.selectByPrimaryKey(id);
    }

    @Override
    public void dele(Long[] arr) {

        for (Long aLong : arr) {

            brandMapper.deleteByPrimaryKey(aLong);

        }

    }

    @Override
    public PageResult search(TbBrand tbBrand, Integer page, Integer rows) {
        PageHelper.startPage(page,rows);

        TbBrandExample example = new TbBrandExample();

        TbBrandExample.Criteria criteria = example.createCriteria();

          if(tbBrand.getName() != null && tbBrand.getName().length()>0){
              criteria.andNameLike("%"+tbBrand.getName()+"%");
          }
          if(tbBrand.getFirstChar() != null && tbBrand.getFirstChar().length()>0){
              criteria.andFirstCharEqualTo(tbBrand.getFirstChar());
          }

                          Page<TbBrand> page1  = (Page<TbBrand>) brandMapper.selectByExample(example);

        return new PageResult(page1.getTotal(),page1.getResult());
    }

    @Override
    public List<Map> selectOptionList() {
        return brandMapper.selectOptionList();
    }

}
