package com.lxz.exam.simpleone.activemq;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.springframework.jms.support.JmsUtils;

public class MailListener implements MessageListener{

	@Override
	public void onMessage(Message message) {
		MapMessage mapMessage = (MapMessage) message;
		try{
			Mail mail = new Mail();
			mail.setMailID(mapMessage.getString("mailID"));
//			mail.setCountry(mapmessage.getS)
		}catch(JMSException e){
			throw JmsUtils.convertJmsAccessException(e);
		}
	}

}
