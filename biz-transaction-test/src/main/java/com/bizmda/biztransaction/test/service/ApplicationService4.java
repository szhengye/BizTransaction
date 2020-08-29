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
public class ApplicationService4 extends AbstractTransaction1 {
    @Autowired
    private TestInnerService1 testInnerService1 ;
    @Autowired
    private TestInnerService2 testInnerService2 ;
    @Autowired
    private TestOuterService testOuterService ;

    @Override
    public void doInnerService1(Object msg) {
        testOuterService.setMaxTimeoutTimes(3);
        testInnerService1.process();
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

        return testOuterService.confirmTimeoutAndReturn(false);
    }

    @Override
    public void cancelInnerService1() {
        log.info("cancelInnerService1()");
        testInnerService1.cancel();
    }
}
