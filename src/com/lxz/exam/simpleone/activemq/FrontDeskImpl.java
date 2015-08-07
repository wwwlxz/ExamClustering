package com.lxz.exam.simpleone.activemq;

import java.util.HashMap;
import java.util.Map;

import javax.jms.Destination;

import org.springframework.jms.core.support.JmsGatewaySupport;

public class FrontDeskImpl extends JmsGatewaySupport implements FrontDesk{
	private Destination destination;
	
	public void setDestination(Destination destination){
		this.destination = destination;
	}
	
	@Override
	public void sendMail(final Mail mail) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("mailID", mail.getMailID());
		map.put("country", mail.getCountry());
		map.put("weight", mail.getWeight());
		getJmsTemplate().convertAndSend(map);
//		getJmsTemplate().send(new MessageCreator(){
//			public Message createMessage(Session session) throws JMSException{
//				MapMessage message = session.createMapMessage();
//				message.setString("mailID", mail.getMailID());
//				message.setString("country", mail.getCountry());
//				message.setString("weight", mail.getWeight());
//				return message;
//			}
//		});
	}

}
