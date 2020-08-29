package com.bizmda.biztransaction.test.service;

import com.bizmda.biztransaction.exception.TransactionTimeOutException;
import com.bizmda.biztransaction.service.AbstractTransaction1;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Scope("prototype")
public class ApplicationService3 extends AbstractTransaction1 {
    @Autowired
    private TestInnerService1 testInnerService1 ;
    @Autowired
    private TestInnerService2 testInnerService2 ;
    @Autowired
    private TestOuterService testOuterService ;

    private String val1;
    private int val2;

    public String getVal1() {
        return val1;
    }

    public void setVal1(String val1) {
        log.info("setVal1({})",val1);
        this.val1 = val1;
    }

//    public int getVal2() {
//        return val2;
//    }
//
//    public void setVal2(int val2) {
//        this.val2 = val2;
//    }

    @Override
    public void doInnerService1(Object msg) {
        testOuterService.setMaxTimeoutTimes(3);
        testInnerService1.process();
        this.val1 = "Hello world!";
        this.val2 = 98;
    }

    @Override
    public boolean doOuterService() throws TransactionTimeOutException {
        log.info("doOuterService()");
        testOuterService.processWithTimeout();
        return true;
    }

    @Override
    public Object doInnerService2() {
        log.info("doInnerService2()");

        testInnerService2.process();
        return null;
    }

    @Override
    public boolean confirmOuterService() throws TransactionTimeOutException {
        log.info("confirmOuterService()");
        log.info("val1:{}, val2:{}",this.val1,this.val2);

        return testOuterService.confirmTimeoutAndReturn(true);
    }

    @Override
    public void cancelInnerService1() {
        log.info("cancelInnerService1()");
        testInnerService1.cancel();
    }
}
