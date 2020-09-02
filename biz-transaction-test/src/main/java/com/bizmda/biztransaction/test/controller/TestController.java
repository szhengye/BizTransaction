package com.bizmda.biztransaction.test.controller;

import com.bizmda.biztransaction.annotation.QueueServiceAOP;
import com.bizmda.biztransaction.exception.Transaction1Exception;
import com.bizmda.biztransaction.exception.Transaction2Exception;
import com.bizmda.biztransaction.exception.TransactionException;
import com.bizmda.biztransaction.exception.TransactionTimeOutException;
import com.bizmda.biztransaction.test.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
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

	@GetMapping("/app1")
	public String applicationService1 (){
		log.info("doInnerService1->doOuterService(true)->doInnerService2");
		try {
			applicationService1.doService("hello");
		} catch (Transaction1Exception e) {
			e.printStackTrace();
		}
		return "ApplicationService1.doInnerService1->doOuterService(true)->doInnerService2";
	}

	@GetMapping("/app2")
	public String applicationService2 (){
		log.info("doInnerService1->doOuterService(false)->cancelInnerService1");
		try {
			applicationService2.doService("hello");
		} catch (Transaction1Exception e) {
			e.printStackTrace();
		}
		return "ApplicationService2.doInnerService1->doOuterService(false)->cancelInnerService1";
	}

	@GetMapping("/app3")
	public String applicationService3 (){
		log.info("doInnerService1->doOuterService(timeout)->confirmOuterService(true)->doInnerService2");
		try {
			applicationService3.doService("hello");
		} catch (Transaction1Exception e) {
			log.info("applicationService3.doService() error code:{}",e.getCode());
		}
		return "ApplicationService3.doInnerService1->doOuterService(timeout)->confirmOuterService(true)->doInnerService2";
	}

	@GetMapping("/app4")
	public String applicationService4 (){
		log.info("doInnerService1->doOuterService(timeout)->confirmOuterService(false)->cancelInnerService1");
		try {
			applicationService4.doService("hello");
		} catch (Transaction1Exception e) {
			e.printStackTrace();
		}
		return "ApplicationService4.doInnerService1->doOuterService(timeout)->confirmOuterService(false)->cancelInnerService1";
	}

	@GetMapping("/app5")
	public String applicationService5 (){
		log.info("doInnerService1->doOuterService(timeout)->confirmOuterService(false)->cancelInnerService1");
		try {
			applicationService5.doService("hello");
		} catch (Transaction2Exception e) {
			e.printStackTrace();
		}
		return "ApplicationService5.doServiceBeforeAsync->TestOuterService.processAsync() * * *> AbstractTransaction2.callback()->ApplicationService5.doServiceAfterAsync()";
	}

	@GetMapping("/app6")
	public String applicationService6 (){
		applicationService6.doService("hello");
		return "app6";
	}

	@GetMapping("/app7")
	public String applicationService7(){
		log.info("doInnerService1->doOuterService(timeout)->confirmOuterService(false)->cancelInnerService1");
		try {
			applicationService7.doSyncService("hello");
		} catch (TransactionTimeOutException e) {
			e.printStackTrace();
		}

		return "ApplicationService5.doServiceBeforeAsync->TestOuterService.processAsync() * * *> AbstractTransaction2.callback()->ApplicationService5.doServiceAfterAsync()";
	}
}
