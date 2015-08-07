package com.lxz.exam.simpleone.activemq;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class FrontDeskMain {
	public static void main(String[] args){
		ApplicationContext context = new ClassPathXmlApplicationContext("spring-beans-front.xml");
		
		FrontDesk frontDesk = (FrontDesk)context.getBean("frontDesk");
		frontDesk.sendMail(new Mail("1234", "US", "1.5"));
	}
}
