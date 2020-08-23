package com.bizmda.biztransaction.test.controller;

import com.bizmda.biztransaction.test.service.ApplicationService1;
import com.bizmda.biztransaction.test.service.ApplicationService2;
import com.bizmda.biztransaction.test.service.ApplicationService3;
import com.bizmda.biztransaction.test.service.ApplicationService4;
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

	@GetMapping("/app1")
	public String applicationService1 (){
		log.info("doInnerService1->doOuterService(true)->doInnerService2");
		applicationService1.doService("hello");
		return "doInnerService1->doOuterService(true)->doInnerService2";
	}

	@GetMapping("/app2")
	public String applicationService2 (){
		log.info("doInnerService1->doOuterService(false)->cancelInnerService1");
		applicationService2.doService("hello");
		return "doInnerService1->doOuterService(false)->cancelInnerService1";
	}

	@GetMapping("/app3")
	public String applicationService3 (){
		log.info("doInnerService1->doOuterService(timeout)->confirmOuterService(true)->doInnerService2");
		applicationService3.doService("hello");
		return "doInnerService1->doOuterService(timeout)->confirmOuterService(true)->doInnerService2";
	}

	@GetMapping("/app4")
	public String applicationService4 (){
		log.info("doInnerService1->doOuterService(timeout)->confirmOuterService(false)->cancelInnerService1");
		applicationService4.doService("hello");
		return "doInnerService1->doOuterService(timeout)->confirmOuterService(false)->cancelInnerService1";
	}
}
