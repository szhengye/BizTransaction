package com.bizmda.biztransaction.test.service;

import com.bizmda.biztransaction.annotation.SyncConfirmService;
import com.bizmda.biztransaction.exception.BizTranException;
import com.bizmda.biztransaction.exception.BizTranTimeOutException;
import com.bizmda.biztransaction.service.AbstractBizTran;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ApplicationService7 extends AbstractBizTran {
    @Autowired
    private TestInnerService1 testInnerService1 ;
    @Autowired
    private TestInnerService2 testInnerService2 ;
    @Autowired
    private TestOuterService testOuterService ;


    @Override
    public Object doService(Object flag) throws BizTranException {
            Object o =  ((ApplicationService7) AopContext.currentProxy()).doSyncService((String)flag);
            return o;
    }

    @SyncConfirmService
    public boolean doSyncService(String flag) throws BizTranTimeOutException {
        log.info("doSyncService()");
        testOuterService.setMaxTimeoutTimes(3);
        if ("1".equals(flag)) {
            return testOuterService.doService(true);
        }
        else if ("0".equals(flag)) {
            return testOuterService.doService(false);
        }
        else {
            testOuterService.doServiceOfTimeout();
            return false;
        }
    }

    public boolean syncServiceConfirm() throws BizTranTimeOutException {
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
