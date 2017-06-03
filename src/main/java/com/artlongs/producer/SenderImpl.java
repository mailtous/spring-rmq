package com.artlongs.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 功能概要：消息产生,提交到队列中去
 * 
 * @author lqf
 * @since  2016年1月15日 
 */
@Component
public class SenderImpl<SEND,READ> implements Sender<SEND,READ> {
	
	private Logger logger = LoggerFactory.getLogger(SenderImpl.class);

	@Autowired
	private AmqpTemplate amqpTemplate;  //同步template

	@Autowired
	private AsyncRabbitTemplate asyncRabbitTemplate;  //异步template

    @Value("${rabbitmq.topic.exchange}")
    private String defaultTopicExchange; //默认主题模式交换机

    @Value("${rabbitmq.fanout.exchange}")
    private String defaultFanoutExchange; //默认广播模式交换机

	@Value("${rabbitmq.direct.exchange}")
    private String defaultDirectExchange; //默认直接模式交换机



	/**
     * 通过主题匹配方式发布消息
     * @param topic  主题,可正则匹配
     * @param message
     */
    public void sendMessageByTopic(String exchange,String topic, SEND message) {
		String currentExchange = StringUtils.isEmpty(exchange)? defaultTopicExchange:exchange;
        amqpTemplate.convertAndSend(currentExchange,topic, message);
    }

    /**
     * 广播方法发布消息
     * @param message
     */
	public void broadcast(String exchange,String topic, SEND message){
		String currentExchange = StringUtils.isEmpty(exchange)? defaultFanoutExchange:exchange;
	    amqpTemplate.convertAndSend(currentExchange,topic,message);
	}


    /**
     * 直连方式发布消息(异步)
     * @param directKey 直连KEY,(key要完全匹配)
     * @param message
     * @return
     */
	public AsyncRabbitTemplate.RabbitConverterFuture<READ> sendAndReceiveByAsync(String exchange,String directKey, SEND message) {
		String currentExchange = StringUtils.isEmpty(exchange)? defaultDirectExchange:exchange;
		AsyncRabbitTemplate.RabbitConverterFuture<READ> future = asyncRabbitTemplate.convertSendAndReceive(currentExchange,directKey,message);
		return future;
	}



}
