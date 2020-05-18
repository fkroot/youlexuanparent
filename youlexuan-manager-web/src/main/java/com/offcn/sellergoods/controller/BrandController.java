package com.offcn.sellergoods.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.entity.PageResult;
import com.offcn.entity.Result;
import com.offcn.pojo.TbBrand;
import com.offcn.sellergoods.service.BrandService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
public class BrandController {

    @Reference
    private BrandService brandService;

    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList() {
        return brandService.selectOptionList();
    }


    @RequestMapping("/findAll")
    public List<TbBrand> findAll() {
        return brandService.findAll();
    }

    //模糊查询加分页
    @RequestMapping("/search")
    public PageResult search(@RequestBody TbBrand tbBrand,Integer page, Integer rows) {
        /*System.out.println(page);*/
        PageResult pageResult = brandService.search(tbBrand,page, rows);

        /*System.out.println(pageResult);*/

        return pageResult;
    }


    @RequestMapping("/findPage")
    public PageResult findPage(Integer page, Integer rows) {
        /*System.out.println(page);*/
        PageResult pageResult = brandService.findPage(page, rows);

        /*System.out.println(pageResult);*/

        return pageResult;
    }

    @RequestMapping("/add")
    public Result add(@RequestBody TbBrand tbBrand) {

        try {
            brandService.add(tbBrand);
            return new Result(true, "增加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "增加失败");
        }


    }

    @RequestMapping("/findOne")
    public TbBrand findOne(Long id){
      /*  System.out.println(id);*/
    return   brandService.findOne(id);

    }


    @RequestMapping("/update")
    public Result update(@RequestBody TbBrand tbBrand){
        System.out.println(tbBrand);
        try {
            brandService.update(tbBrand);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }


    }
    @RequestMapping("/dele")
    public Result dele(Long[] selectIds){
        try {
            brandService.dele(selectIds);
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }


    }




}