package com.artlongs.producer;

import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.util.StringUtils;

/**
 * <p>Function:消息发布</p>
 *
 * @version $Revision$ $Date$
 *          Date: 5/12/17
 *          Time: 10:41
 * @author: lqf
 * @since 1.0
 */
public interface Sender<SEND,READ> {


    /**
     * 主题方法发布消息
     * @param exchange 消息交换机
     * @param topic    主题
     * @param message  消息内容
     */
    void sendMessageByTopic(String exchange,String topic, SEND message);


    /**
     * 广播方法发布消息
     * @param exchange
     * @param topic
     * @param message
     */
    void broadcast(String exchange,String topic, SEND message);


    /**
     * 直连方式发布消息(异步)
     * @param directKey 直连KEY,(key要完全匹配)
     * @param message
     * @return
     */
    AsyncRabbitTemplate.RabbitConverterFuture<READ> sendAndReceiveByAsync(String exchange, String directKey, SEND message);
}
