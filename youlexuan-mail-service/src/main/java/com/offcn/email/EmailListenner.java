package com.offcn.email;

import com.offcn.utils.EmailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

@Component("emailListener")
public class EmailListenner implements MessageListener {
    @Autowired
    private EmailUtil emailUtil;


    @Override
    public void onMessage(Message message) {

        MapMessage mapMessage = (MapMessage) message;
        try {
            System.out.println("接收到的邮件信息为"+mapMessage.getString("emailMessage")+mapMessage.getString("emailMessage"));
          emailUtil.sendEmail(mapMessage.getString("emailMessage"),mapMessage.getString("emailAddress"));

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
