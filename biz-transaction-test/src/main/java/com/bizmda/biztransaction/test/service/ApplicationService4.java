package com.bizmda.biztransaction.test.service;

import com.bizmda.biztransaction.exception.TransactionTimeOutException;
import com.bizmda.biztransaction.service.AbstractBizTran1;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ApplicationService4 extends AbstractBizTran1 {
    @Autowired
    private TestInnerService1 testInnerService1 ;
    @Autowired
    private TestInnerService2 testInnerService2 ;
    @Autowired
    private TestOuterService testOuterService ;

    @Override
    public void beforeSyncService(Object msg) {
        log.info("beforeSyncService()");
        testOuterService.setMaxTimeoutTimes(3);
        testInnerService1.doService();
    }

    @Override
    public boolean doSyncService() throws TransactionTimeOutException {
        log.info("doSyncService()");
        testOuterService.doServiceOfTimeout();
        return true;
    }

    @Override
    public Object afterSyncService() {
        log.info("afterSyncService()");
        testInnerService2.doService();
        return null;
    }

    @Override
    public boolean confirmSyncService() throws TransactionTimeOutException {
        log.info("confirmSyncService()");

        return testOuterService.confirmService(false);
    }

    @Override
    public void rollbackService() {
        log.info("rollbackService()");
        testInnerService1.rollbackService();
    }
}
