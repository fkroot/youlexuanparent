package com.offcn.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.pojo.TbItem;
import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
class ItemSearchListener implements MessageListener{
    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        System.out.println("监听类接受到数据，开始监听");
        if(message instanceof TextMessage){
            TextMessage message1 = (TextMessage) message;

            try {
                String text = message1.getText();

                List<TbItem> tbItems = JSON.parseArray(text, TbItem.class);
                for (TbItem tbItem : tbItems) {
                    System.out.println(tbItem.getTitle());
                    Map<String,String> map = JSON.parseObject(tbItem.getSpec(), Map.class);
                    Map map1 = new HashMap();
                    for (String key : map.keySet()) {
                        map1.put("item_spec_"+ Pinyin.toPinyin(key,"").toLowerCase(),map.get(key));
                    }
                    tbItem.setSpecMap(map1);
                }
                itemSearchService.importDataList(tbItems);
                System.out.println("审核通过的数据成功保存到solr中");


            } catch (JMSException e) {
                e.printStackTrace();
            }

        }
    }
}
