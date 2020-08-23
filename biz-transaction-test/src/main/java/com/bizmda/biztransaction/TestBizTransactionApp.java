/**
 * 
 */
package com.bizmda.biztransaction;

import com.bizmda.biztransaction.service.SpringContextsUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

/** 
* 类说明
* mq ttl消息 
*/
 
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
