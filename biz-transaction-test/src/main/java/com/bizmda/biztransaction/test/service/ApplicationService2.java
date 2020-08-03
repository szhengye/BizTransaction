package com.bizmda.biztransaction.test.service;

import com.bizmda.biztransaction.exception.TransactionTimeOutException;
import com.bizmda.biztransaction.service.AbstractTransaction1;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ApplicationService2 extends AbstractTransaction1 {
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
    public boolean doOuterService(Object msg) throws TransactionTimeOutException {
        log.info("doOuterService()");

        return testOuterService.process(false);
    }

    @Override
    public void doInnerService2(Object msg) {
        log.info("doInnerService2()");

        testInnerService2.process();
    }

    @Override
    public boolean confirmOuterService(Object msg) throws TransactionTimeOutException {
        log.info("confirmOuterService()");

        return testOuterService.confirmTimeoutAndReturn(true);
    }

    @Override
    public void cancelInnerService1(Object msg) {
        log.info("cancelInnerService1()");
        testInnerService1.cancel();
    }
}