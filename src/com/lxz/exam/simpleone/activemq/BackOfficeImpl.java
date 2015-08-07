package com.lxz.exam.simpleone.activemq;

import java.util.Map;

import javax.jms.Destination;

import org.springframework.jms.core.support.JmsGatewaySupport;

public class BackOfficeImpl extends JmsGatewaySupport implements BackOffice{
	private Destination destination;

	public void setDestination(Destination destination) {
		this.destination = destination;
	}

	@Override
	public Mail receiveMail() {
		Map map = (Map)getJmsTemplate().receiveAndConvert();
		Mail mail = new Mail();
		mail.setMailID((String) map.get("mailID"));
		mail.setCountry((String) map.get("country"));
		mail.setWeight((String) map.get("weight"));
		return mail;
//		MapMessage message = (MapMessage) getJmsTemplate().receive();
//		try{
//			if(message == null){
//				return null;
//			}
//			Mail mail = new Mail();
//			mail.setMailID(message.getString("mailID"));
//			mail.setCountry(message.getString("country"));
//			mail.setWeight(message.getString("weight"));
//			return mail;
//		}catch(JMSException e){
//			throw JmsUtils.convertJmsAccessException(e);
//		}
	}
}
