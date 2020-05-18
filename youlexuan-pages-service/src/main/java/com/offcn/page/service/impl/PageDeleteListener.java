package com.offcn.page.service.impl;

import com.offcn.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.io.Serializable;

@Component
public class PageDeleteListener implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;


    @Override
    public void onMessage(Message message) {
        ObjectMessage message1 = (ObjectMessage) message;
        try {
            Long[] object = (Long[]) message1.getObject();
            itemPageService.deletePageById(object);

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
