package com.bizmda.biztransaction.test.controller;

import com.bizmda.biztransaction.exception.TransactionException;
import com.bizmda.biztransaction.test.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TestController {
	@Autowired
	private ApplicationService1 applicationService1 ;
	@Autowired
	private ApplicationService2 applicationService2 ;
	@Autowired
	private ApplicationService3 applicationService3 ;
	@Autowired
	private ApplicationService4 applicationService4 ;
	@Autowired
	private ApplicationService5 applicationService5 ;
	@Autowired
	private ApplicationService6 applicationService6 ;
	@Autowired
	private ApplicationService7 applicationService7 ;
	@Autowired
	private ApplicationService8 applicationService8 ;
	@Autowired
	private ApplicationService9 applicationService9 ;

	@GetMapping("/app1")
	public String applicationService1 (){
		try {
			applicationService1.doService("hello");
		} catch (TransactionException e) {
			e.printStackTrace();
		}
		return "依次执行beforeSyncService()、doSyncService()、afterSyncService()";
	}

	@GetMapping("/app2")
	public String applicationService2 (){
		try {
			applicationService2.doService("hello");
		} catch (TransactionException e) {
			e.printStackTrace();
		}
		return "doSyncService()返回失败，应触发rollbackService()";
	}

	@GetMapping("/app3")
	public String applicationService3 (){
		try {
			applicationService3.doService("hello");
		} catch (TransactionException e) {
			log.info("applicationService3.doService() error code:{}",e.getCode());
		}
		return "doSyncService()超时，触发confirmSyncService()，连续2次超时，第3次返回成功";
	}

	@GetMapping("/app4")
	public String applicationService4 (){
		try {
			applicationService4.doService("hello");
		} catch (TransactionException e) {
			e.printStackTrace();
		}
		return "doSyncService()超时，触发confirmSyncService()，连续2次超时，第3次返回失败";
	}

	@GetMapping("/app5")
	public String applicationService5 (){
		try {
			applicationService5.doService("hello");
		} catch (TransactionException e) {
			e.printStackTrace();
		}
		return "异常调用，并回调";
	}

	@GetMapping("/app6")
	public String applicationService6 (){
		applicationService6.doService("hello");
		return "异步任务调用";
	}

	@GetMapping("/app7")
	public String applicationService7(@RequestParam("flag")String flag){
		try {
			applicationService7.doService(flag);
		} catch (TransactionException e) {
			e.printStackTrace();
		}
		return "测试@SyncService注解方法";
	}

	@GetMapping("/app8")
	public String applicationService8(){
		try {
			applicationService8.doService("hello");
		} catch (TransactionException e) {
			e.printStackTrace();
		}
		return "测试@AsyncService注解方法";
	}

	@GetMapping("/app9")
	public String applicationService9(){
		try {
			applicationService9.doService("hello");
		} catch (TransactionException e) {
			e.printStackTrace();
		}
		return "测试@AsyncService注解方法";
	}
}
