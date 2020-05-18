package com.offcn.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/search")
public class ItemSearchController {

  @Reference
  private ItemSearchService itemSearchService;

    @RequestMapping("/find")
public Map<String,Object> find(@RequestBody Map searchMap) {


        Map<String, Object> search = itemSearchService.search(searchMap);



        return search;

    }





}
