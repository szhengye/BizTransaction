package com.bizmda.biztransaction.test.service;

import com.bizmda.biztransaction.annotation.SyncService;
import com.bizmda.biztransaction.exception.TransactionException;
import com.bizmda.biztransaction.exception.TransactionTimeOutException;
import com.bizmda.biztransaction.service.AbstractTransaction;
import com.bizmda.biztransaction.service.AbstractTransaction1;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

@Slf4j
@Service
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ApplicationService7 extends AbstractTransaction {
    @Autowired
    private TestInnerService1 testInnerService1 ;
    @Autowired
    private TestInnerService2 testInnerService2 ;
    @Autowired
    private TestOuterService testOuterService ;



    @Override
    public Object doService(Object msg) throws TransactionException {
        try {
            Object o =  ((ApplicationService7) AopContext.currentProxy()).doSyncService(msg);
            return o;
        } catch (TransactionTimeOutException e) {
            e.printStackTrace();
            return null;
        }
    }

    @SyncService
    public Object doSyncService(Object msg) throws TransactionTimeOutException {
        testOuterService.setMaxTimeoutTimes(3);
        testInnerService1.process();
        log.info("doOuterService()");
        testOuterService.processWithTimeout();
        return "ok";
    }

    public boolean syncServiceConfirm() throws TransactionTimeOutException {
        log.info("confirmOuterService()");
        return testOuterService.confirmTimeoutAndReturn(true);
    }

    public void syncServiceCommit() {
        log.info("doInnerService2()");
        testInnerService2.process();
    }

    public void syncServiceRollback() {
        log.info("cancelInnerService1()");
        testInnerService1.cancel();
    }

}
