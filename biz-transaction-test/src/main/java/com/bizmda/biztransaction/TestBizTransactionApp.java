/**
 * 
 */
package com.bizmda.biztransaction;

import com.bizmda.biztransaction.annotation.QueueServiceAOP;
import com.bizmda.biztransaction.service.SpringContextsUtil;
import com.bizmda.biztransaction.test.service.ApplicationService1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

/** 
* 类说明
* mq ttl消息 
*/

@Import(QueueServiceAOP.class)
@EnableAspectJAutoProxy(exposeProxy = true)
@Configuration
@SpringBootApplication
public class TestBizTransactionApp {
	
	public static void main(String[] args) {
//		固定端口启动
		ApplicationContext context = SpringApplication.run(TestBizTransactionApp.class, args);

		SpringContextsUtil springContextsUtil = new SpringContextsUtil();
		springContextsUtil.setApplicationContext(context);
		
	}

}
