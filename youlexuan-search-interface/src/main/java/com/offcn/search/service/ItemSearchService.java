package com.offcn.search.service;

import com.offcn.pojo.TbItem;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {


 public void importDataList(List<TbItem> list);

    public Map<String,Object> search(Map searchMap);


    void deleteGoodsByIds(Long[] ids);
}
