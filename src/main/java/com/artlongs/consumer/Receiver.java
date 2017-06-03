package com.artlongs.consumer;

import com.artlongs.annotation.FanoutLicListener;
import com.artlongs.producer.JsonConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * <p>Function:消息接收类</p>
 *
 * @version $Revision$ $Date$
 *          Date: 4/11/17
 *          Time: 09:58
 * @author: lqf
 * @since 1.0
 */
@Component
public class Receiver {
    private static final Logger logger = LoggerFactory.getLogger(Receiver.class);

    @Resource
    private AsyncRabbitTemplate asyncRabbitTemplate;  //异步template

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "queue.#",durable="false"),
            exchange = @Exchange(value = "#{'${rabbitmq.topic.exchange}'}", type = ExchangeTypes.TOPIC,ignoreDeclarationExceptions = "true"),
            key="queue.test.hello"),
            admin="rabbitAdmin"
    )
    public void tip1(Message msg) {
        JsonConverter<String> jsonConverter = new JsonConverter<String>();
        String rmsg = jsonConverter.fromMessage(msg);
        logger.info("Received message {}", rmsg);
    }

    /**
     * 广播模式,接收信息
     * @param msg
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "toall"),
            exchange = @Exchange(value ="#{'${rabbitmq.fanout.exchange}'}", type = ExchangeTypes.FANOUT,ignoreDeclarationExceptions = "true")
            ,key="toall"),
            admin="rabbitAdmin"
    )
    public void fanout1(Message msg) {
        JsonConverter<String> jsonConverter = new JsonConverter<String>();
        String rmsg = jsonConverter.fromMessage(msg);
        logger.info("Received message {}", rmsg);
    }


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "toall"),
            exchange = @Exchange(value ="#{'${rabbitmq.fanout.exchange}'}", type = ExchangeTypes.FANOUT,ignoreDeclarationExceptions = "true")
            ,key="toall"),
            admin="rabbitAdmin"
    )
    public void fanout2(Message msg) {
        JsonConverter<String> jsonConverter = new JsonConverter<String>();
        String rmsg = jsonConverter.fromMessage(msg);
        logger.info("Received message {}", rmsg);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("test_direct"),
            exchange = @Exchange(value = "#{'${rabbitmq.direct.exchange}'}", type = ExchangeTypes.DIRECT)
            ,key="test_direct"
             ),
            admin="rabbitAdmin"
    )
    public void getMsgAndReply(Message msg) {
        JsonConverter<String> jsonConverter = new JsonConverter<String>();
        String rmsg = jsonConverter.fromMessage(msg);
        final Message replyMsg = new Message(("REPLY=> " + rmsg).getBytes(), msg.getMessageProperties());

        asyncRabbitTemplate.onMessage(replyMsg); //返回消息


    }


}