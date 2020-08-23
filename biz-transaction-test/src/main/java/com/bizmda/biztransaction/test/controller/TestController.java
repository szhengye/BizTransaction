package com.bizmda.biztransaction.test.controller;

import com.bizmda.biztransaction.exception.Transaction1Exception;
import com.bizmda.biztransaction.exception.Transaction2Exception;
import com.bizmda.biztransaction.test.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

	@GetMapping("/app1")
	public String applicationService1 (){
		log.info("doInnerService1->doOuterService(true)->doInnerService2");
		try {
			applicationService1.doService("hello");
		} catch (Transaction1Exception e) {
			e.printStackTrace();
		}
		return "doInnerService1->doOuterService(true)->doInnerService2";
	}

	@GetMapping("/app2")
	public String applicationService2 (){
		log.info("doInnerService1->doOuterService(false)->cancelInnerService1");
		try {
			applicationService2.doService("hello");
		} catch (Transaction1Exception e) {
			e.printStackTrace();
		}
		return "doInnerService1->doOuterService(false)->cancelInnerService1";
	}

	@GetMapping("/app3")
	public String applicationService3 (){
		log.info("doInnerService1->doOuterService(timeout)->confirmOuterService(true)->doInnerService2");
		try {
			applicationService3.doService("hello");
		} catch (Transaction1Exception e) {
			log.info("applicationService3.doService() error code:{}",e.getCode());
		}
		return "doInnerService1->doOuterService(timeout)->confirmOuterService(true)->doInnerService2";
	}

	@GetMapping("/app4")
	public String applicationService4 (){
		log.info("doInnerService1->doOuterService(timeout)->confirmOuterService(false)->cancelInnerService1");
		try {
			applicationService4.doService("hello");
		} catch (Transaction1Exception e) {
			e.printStackTrace();
		}
		return "doInnerService1->doOuterService(timeout)->confirmOuterService(false)->cancelInnerService1";
	}

	@GetMapping("/app5")
	public String applicationService5 (){
		log.info("doInnerService1->doOuterService(timeout)->confirmOuterService(false)->cancelInnerService1");
		try {
			applicationService5.doServiceBeforeAsync("hello");
		} catch (Transaction2Exception e) {
			e.printStackTrace();
		}
		return "doInnerService1->doOuterService(timeout)->confirmOuterService(false)->cancelInnerService1";
	}
}
