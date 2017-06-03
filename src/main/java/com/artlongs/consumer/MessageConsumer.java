package com.artlongs.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessageProperties;

/**
 * 功能概要：消费接收
 * 
 * @author lqf
 * @since  2016年1月15日 
 */
public class MessageConsumer implements MessageListener {
	
	private Logger logger = LoggerFactory.getLogger(MessageConsumer.class);

	@Override
	public void onMessage(Message message) {
		
		logger.info("receive message:{}",message);
		MessageProperties mp = message.getMessageProperties();
		
				
	}

}
