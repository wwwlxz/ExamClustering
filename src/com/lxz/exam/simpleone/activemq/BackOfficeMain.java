package com.lxz.exam.simpleone.activemq;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class BackOfficeMain {
	public static void main(String[] args){
		ApplicationContext context = new ClassPathXmlApplicationContext("spring-beans-back.xml");
		
		BackOffice backOffice = (BackOffice)context.getBean("backOffice");
		Mail mail = backOffice.receiveMail();
		System.out.println("Mail #" + mail.getMailID() + "received.");
	}
}
