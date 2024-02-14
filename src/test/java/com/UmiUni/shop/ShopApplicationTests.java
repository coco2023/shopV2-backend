package com.UmiUni.shop;

import com.UmiUni.shop.dto.SalesOrderDTO;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ShopApplicationTests {

//	@Test
//	void contextLoads() {
//	}
//
//	@Autowired
//	private RabbitTemplate rabbitTemplate;
//
//	@Value("${rabbitmq.queues.order_process.name}")
//	private String orderQueueName;
//
//	@Value("${rabbitmq.dlx.queue.name}")
//	private String dlxQueueName;
//
//	@Test
//	public void testMessageExpiresAndGoesToDLX() throws InterruptedException {
//		// 构造一个示例订单消息
//		SalesOrderDTO salesOrder = new SalesOrderDTO();
//		salesOrder.setSalesOrderSn("SO-1706895156174-3801");
//		// 设置其他属性...
//
//		// 发送消息到订单队列
//		rabbitTemplate.convertAndSend(orderQueueName, salesOrder);
//
//		// 等待足够的时间以确保消息过期并路由到DLX
//		Thread.sleep(80000); // 等待时间稍长于消息的TTL
//
//		// 从死信队列接收消息
//		SalesOrderDTO expiredOrder = (SalesOrderDTO) rabbitTemplate.receiveAndConvert(dlxQueueName);
//
//		// 断言验证：确保从死信队列接收到的消息不为空，并且订单号匹配
//		assertNotNull(expiredOrder);
//		assertEquals("SO-1706895156174-3801", expiredOrder.getSalesOrderSn());
//	}
}
