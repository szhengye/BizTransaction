package com.bizmda.biztransaction.test.service;

import com.bizmda.biztransaction.annotation.SyncService;
import com.bizmda.biztransaction.exception.TransactionException;
import com.bizmda.biztransaction.exception.TransactionTimeOutException;
import com.bizmda.biztransaction.service.AbstractTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Scope("prototype")
public class ApplicationService7 extends AbstractTransaction {
    @Autowired
    private TestInnerService1 testInnerService1 ;
    @Autowired
    private TestInnerService2 testInnerService2 ;
    @Autowired
    private TestOuterService testOuterService ;


    @Override
    public Object doService(Object flag) throws TransactionException {
            Object o =  ((ApplicationService7) AopContext.currentProxy()).doSyncService((String)flag);
            return o;
    }

    @SyncService
    public boolean doSyncService(String flag) throws TransactionTimeOutException {
        log.info("doSyncService()");
        testOuterService.setMaxTimeoutTimes(3);
        if (flag.equals("1")) {
            return testOuterService.doService(true);
        }
        else if (flag.equals("0")) {
            return testOuterService.doService(false);
        }
        else {
            testOuterService.doServiceOfTimeout();
            return false;
        }
    }

    public boolean syncServiceConfirm() throws TransactionTimeOutException {
        log.info("syncServiceConfirm()");
        return testOuterService.confirmService(false);
    }

    public void syncServiceCommit() {
        log.info("syncServiceCommit()");
        testInnerService2.doService();
    }

    public void syncServiceRollback() {
        log.info("syncServiceRollback()");
        testInnerService1.rollbackService();
    }

}
