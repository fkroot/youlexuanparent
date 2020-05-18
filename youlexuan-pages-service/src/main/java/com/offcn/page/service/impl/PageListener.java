package com.offcn.page.service.impl;

import com.offcn.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@Component
public class PageListener implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {
        TextMessage message1 = (TextMessage) message;




        try {
        String  text = message1.getText();
            System.out.println("接收到消息");
            boolean b = itemPageService.genItemHtml(Long.parseLong(text));
            System.out.println("完成消息的更新");
        } catch (JMSException e) {
            e.printStackTrace();
        }
        


    }
}
