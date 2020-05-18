package com.offcn.search.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.pojo.TbItem;
import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.*;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private  RedisTemplate redisTemplate;


    //在商品设定好状态之后更新到索引库
    @Override
    public void importDataList(List<TbItem> list) {
        for (TbItem item : list) {
            Map map = JSON.parseObject(item.getSpec(), Map.class);
        Map map1 = new HashMap();

            for (Object key : map.keySet()) {
                map1.put("item_spec_"+Pinyin.toPinyin((String) key,""),map.get(key));
            }
            item.setSpecMap(map1);
        
        }

        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    @Override
    public Map<String, Object> search(Map searchMap) {
        
        //关键字的空格处理

        String keywords = (String) searchMap.get("keywords");

        String keywords1 = keywords.replace(" ", "");

         searchMap.put("keywords",keywords1);


        Map<String, Object> map = new HashMap<>();


     map.putAll(searchList(searchMap));


               List categoryList =  searchCategoryList(searchMap);

        String category = (String) searchMap.get("category");
if(!"".equals(category)){
    map.putAll(searchBrandAndSpecList(category));
}else {
    if(categoryList.size()>0){
        map.putAll(searchBrandAndSpecList((String)categoryList.get(0)));
    }
}


        return map;
    }

    @Override
    public void deleteGoodsByIds(Long[] ids) {
        List<Long> id = Arrays.asList(ids);
                         Query query = new SimpleQuery();
        Criteria item_goods_id = new Criteria("item_goodsid").in(id);
        query.addCriteria(item_goods_id);
        solrTemplate.delete(query);
        solrTemplate.commit();


    }


    //解决高亮问题，关键字搜索的时候使title文字显示高亮

    private Map searchList(Map searchMap){
          //返回结果
        Map map = new HashMap();
//设置高亮查询对象
        Query query = new SimpleQuery();


         Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));

//根据各种条件的改变进行查询操作
        //按照分类进行筛选

        if(!"".equals(searchMap.get("category"))){
            System.out.println("category="+searchMap.get("category"));
            Criteria criteria1 = new Criteria("item_category").is(searchMap.get("category"));

         FilterQuery simpleFilterQuery = new SimpleFilterQuery(criteria1);

            query.addFilterQuery(simpleFilterQuery);
        }
//2按照品牌进行筛选
        if(!"".equals(searchMap.get("brand"))){

            Criteria criteria1 = new Criteria("item_brand").is(searchMap.get("brand"));
           FilterQuery simpleFilterQuery = new SimpleFilterQuery(criteria1);

            query.addFilterQuery(simpleFilterQuery);
        }
    //按照规格进行筛选
        if(null != searchMap.get("spec")){

            Map<String,String> spec = (Map) searchMap.get("spec");
            for (String s : spec.keySet()) {

                Criteria criteria1 = new Criteria("item_spec_" + Pinyin.toPinyin(s, "").toLowerCase()).is(spec.get(s));
                FilterQuery simpleFilterQuery = new SimpleFilterQuery(criteria1);
                query.addFilterQuery(simpleFilterQuery);
            }

        }

        //按照价格筛选

        if(!"".equals(searchMap.get("price"))){
            String[] prices = ((String) searchMap.get("price")).split("-");


            //判断区间的起点不为0
                  if(!prices[0].equals("0")){
                      Criteria criteria1 = new Criteria("item_price").greaterThanEqual(prices[0]);
                      FilterQuery simpleFilterQuery = new SimpleFilterQuery(criteria1);

                      query.addFilterQuery(simpleFilterQuery);
                  }
                  //如果区间的终点不等于~
                  if(!prices[1].equals("~")){
                      Criteria criteria1 = new Criteria("item_price").lessThanEqual(prices[1]);
                      FilterQuery simpleFilterQuery = new SimpleFilterQuery(criteria1);

                      query.addFilterQuery(simpleFilterQuery);

                  }



        }
        //手写分页查询
        //1.6 分页查询
        Integer pageNo = (Integer) searchMap.get("pageNo");//提取页码
        if (pageNo == null) {
            pageNo = 1;//默认第一页
        }
        Integer pageSize = (Integer) searchMap.get("pageSize");//每页记录数
        if (pageSize == null) {
            pageSize = 10;//默认20
        }
        query.setOffset((pageNo - 1) * pageSize);//从第几条记录查询
        query.setRows(pageSize);

        //1.7排序
        String sortField = (String) searchMap.get("sortField");
        String sort = (String) searchMap.get("sort");

        if(!"".equals(sort) && null!=sort){
            if(sort.equals("ASC")){
                Sort sort1 = new Sort(Sort.Direction.ASC,"item_"+sortField);
                query.addSort(sort1);
            }else {
                Sort sort1 = new Sort(Sort.Direction.DESC,"item_"+sortField);
                query.addSort(sort1);
            }
        }
        query.addCriteria(criteria);


        ScoredPage<TbItem> tbItems = solrTemplate.queryForPage(query, TbItem.class);
        ;
        System.out.println("从solr中获取的每页的信息数量"+tbItems.getContent().size());

                    map.put("rows",tbItems.getContent());
        map.put("totalPages", tbItems.getTotalPages());//返回总页数
        map.put("pageSize", tbItems.getTotalElements());//返回总记录数
                              return map;

    }

    private List searchCategoryList(Map searchMap){
              List<String> list = new ArrayList<>();

        Query simpleQuery = new SimpleQuery();

        //按照关键字进行查询

        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
          simpleQuery.addCriteria(criteria);
          //设置分组选项

        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
       simpleQuery.setGroupOptions(groupOptions);

       //得到分组页
        GroupPage<TbItem> tbItems = solrTemplate.queryForGroupPage(simpleQuery, TbItem.class);

        //根据列得到分组结果集
        GroupResult<TbItem> groupResult = tbItems.getGroupResult("item_category");

        //得到分组结果页入口

        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
//得到分组入口集合
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        for (GroupEntry<TbItem> tbItemGroupEntry : content) {
            list.add(tbItemGroupEntry.getGroupValue());
        }

                   return list;

    }
//根据typeid查询出brandList和specList
    private Map searchBrandAndSpecList(String category){

         Map map = new HashMap();

        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);

        if(typeId != null){
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
              map.put("brandList",brandList);

            List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
            map.put("specList",specList);

        }
                     return map;


    }


}

