package com.offcn.search.service.impl;

import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.io.Serializable;
import java.util.Arrays;

@Component
public class ItemDeleteListener implements MessageListener {

    @Autowired
    ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        ObjectMessage objectMessage = (ObjectMessage) message;
        try {
            Long []  list = (Long[]) objectMessage.getObject();
            System.out.println("监听到删除数据时传递过来的信息");

            itemSearchService.deleteGoodsByIds(list);
            System.out.println("成功删除索引库中的数据");



        } catch (JMSException e) {
            e.printStackTrace();
        }


    }
}
