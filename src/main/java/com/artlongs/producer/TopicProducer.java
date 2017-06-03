package com.artlongs.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * <p>Function:</p>
 *
 * @version $Revision$ $Date$
 *          Date: 4/11/17
 *          Time: 10:00
 * @author: lqf
 * @since 1.0
 */
@Component
public class TopicProducer {

    public static void main(final String... args) throws Exception {

        AbstractApplicationContext ctx = new ClassPathXmlApplicationContext("application.xml");
        RabbitTemplate template = ctx.getBean(RabbitTemplate.class);
        //template.convertAndSend("Hello, world!");
        Foo foo = new Foo("linton",18);
      //  template.setMessageConverter(new Jackson2JsonMessageConverter());
        template.convertAndSend(foo);
        //template.convertAndSend("================ hello ==============");
        Thread.sleep(1000);
        ctx.destroy();
    }


}
