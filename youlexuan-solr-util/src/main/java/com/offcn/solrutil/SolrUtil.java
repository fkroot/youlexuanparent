package com.offcn.solrutil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.mapper.TbItemMapper;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.domain.Sort;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.stereotype.Component;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {


    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private SolrTemplate solrTemplate;


    /**
     * 导入商品数据
     */
    public void importItemData() {
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");// 已审核
        List<TbItem> itemList = itemMapper.selectByExample(example);
        System.out.println("===商品列表===");

        for (TbItem tbItem : itemList) {
            Map<String,String> specMap = JSON.parseObject(tbItem.getSpec(),Map.class);
            Map<String,String> mapPinyin=new HashMap<>();


            for (String key : specMap.keySet()) {
                mapPinyin.put(Pinyin.toPinyin(key,"").toLowerCase(),specMap.get(key));

            }
            tbItem.setSpecMap(mapPinyin);



        }


//保存集合数据到solr
        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
        System.out.println("保存商品数据到solr成功");


        

    }
/*    public void testDeleteAll(){
        Query query=new SimpleQuery("*:*");
        solrTemplate.delete(query);
        solrTemplate.commit();
    }*/
public void testSelect(){
               /*    new Cre*/
    TbItem item = solrTemplate.getById(536563, TbItem.class);
    System.out.println(item.getImage());

}
    public void testPageQueryMutil() {
        Query query = new SimpleQuery("*:*");

   /*     Criteria criteria = new Criteria("item_title").contains("2");
        criteria = criteria.and("item_price").greaterThan(2020);*/
        Criteria criteria = new Criteria("item_keywords").is("手机");

        query.addCriteria(criteria);

        Sort s = new Sort(Sort.Direction.DESC, "item_price");
        query.addSort(s);

        // query.setOffset(10); //开始索引（默认0）
        // query.setRows(100); //每页记录数(默认10)
        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);

        System.out.println("总记录数：" + page.getTotalElements());
        List<TbItem> list = page.getContent();
        System.out.println(page.getContent().size());

        for (TbItem item : list) {
            System.out.println(item.getImage() + "，" + item.getPrice());
        }
    }




    public static void main(String[] args) {
        ApplicationContext context=new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
        SolrUtil solrUtil = (SolrUtil) context.getBean("solrUtil");
/*   solrUtil.importItemData();*/
        /*solrUtil.testSelect();*/
      solrUtil.testPageQueryMutil();

        }


}
