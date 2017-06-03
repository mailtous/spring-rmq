package com.artlongs;

import com.artlongs.producer.Sender;
import com.artlongs.producer.SenderImpl;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 * 功能概要：
 *
 * @author lqf
 * @since 2016年1月15日
 */
public class MessageTest {

	private Logger logger = LoggerFactory.getLogger(MessageTest.class);

	private ApplicationContext context = null;

	private String BLANK_EXCHANGE="";


	@Before
	public void setUp() throws Exception {
		context = new ClassPathXmlApplicationContext("application.xml");
	}

	@Test
	public void broadcast() throws Exception {
		Sender sender = (Sender) context.getBean("senderImpl");
		sender.broadcast(BLANK_EXCHANGE, "toall", "这是一条广播方式的消息 TO ALL");
	}


	@Test
	public void sendOfAsync() throws Exception {
        Sender sender = (Sender) context.getBean("senderImpl");

		AsyncRabbitTemplate.RabbitConverterFuture future = sender.sendAndReceiveByAsync(BLANK_EXCHANGE, "test_direct", " This message send of Async.");

		future.addCallback(new ListenableFutureCallback<String>() {

			@Override
			public void onSuccess(String result) {//对方收到消息,并返回成功信息
				out("SUCCESS => " + result);
			}

			@Override
			public void onFailure(Throwable ex) {
				//throw new RuntimeException(ex);
				ex.printStackTrace();
			}

		});
		Thread.sleep(1000);

	}

	public void out(String msg) {
		logger.info(msg);

	}

	@Test
	public void sendOfTopic() throws Exception {
        Sender sender = (Sender) context.getBean("senderImpl");

		for (int i = 0; i < 1; i++) {
			sender.sendMessageByTopic(BLANK_EXCHANGE,"queue.test.hello", "Hello, I am amq sender num :" + i);
			try {
				//暂停一下，好让消息消费者去取消息打印出来
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
