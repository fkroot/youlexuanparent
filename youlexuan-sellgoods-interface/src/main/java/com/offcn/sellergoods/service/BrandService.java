package com.offcn.sellergoods.service;

import com.offcn.entity.PageResult;
import com.offcn.pojo.TbBrand;

import java.util.List;
import java.util.Map;

public interface BrandService {

           public List<TbBrand> findAll();


    PageResult findPage(int page, int rows);

    void add(TbBrand tbBrand);

    void update(TbBrand tbBrand);

    TbBrand findOne(Long id);

    void dele(Long[] arr);

    PageResult search(TbBrand tbBrand, Integer page, Integer rows);

    List<Map> selectOptionList();
}
