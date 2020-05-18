package com.offcn.utils;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

@Component
public class EmailUtil {

  public void sendEmail(String emailMessage,String emailAddress){
      ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring/spring-mail.xml");

      JavaMailSenderImpl mailsend=(JavaMailSenderImpl) context.getBean("mailSender");

      //创建简单的邮件
      SimpleMailMessage msg = new SimpleMailMessage();
      msg.setFrom("fk973669498@163.com");
      msg.setTo(emailAddress);
      msg.setSubject("优乐选项目测试邮件");
      msg.setText(emailMessage);

      //发送邮件

      mailsend.send(msg);

      System.out.println("send ok");

  }





}
