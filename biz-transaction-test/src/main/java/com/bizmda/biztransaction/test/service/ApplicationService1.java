package com.bizmda.biztransaction.test.service;

import com.bizmda.biztransaction.exception.TransactionTimeOutException;
import com.bizmda.biztransaction.service.AbstractBizTran1;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ApplicationService1 extends AbstractBizTran1 {
    @Autowired
    private TestInnerService1 testInnerService1 ;
    @Autowired
    private TestInnerService2 testInnerService2 ;
    @Autowired
    private TestOuterService testOuterService ;

    /**
     * 0：返回失败
     * 1：返回成功
     * 2：超时
     */
    private int doServiceFlag;

    /**
     * 0：确认失败
     * 1：确认成功
     * 2：超时
     */
    private int confirmServiceFlag;

    public void setDoServiceFlag(int doServiceFlag) {
        this.doServiceFlag = doServiceFlag;
    }

    public void setConfirmServiceFlag(int confirmServiceFlag) {
        this.confirmServiceFlag = confirmServiceFlag;
    }

    @Override
    public void beforeSyncService(Object msg) {
        log.info("beforeSyncService()");
        testOuterService.setMaxTimeoutTimes(3);
        testInnerService1.doService();
    }

    @Override
    public boolean doSyncService() throws TransactionTimeOutException {
        log.info("doSyncService()");
        return testOuterService.doService(true);
    }

    @Override
    public Object afterSyncService() {
        log.info("afterSyncService()");
        return testInnerService2.doService();
    }

    @Override
    public boolean confirmSyncService() throws TransactionTimeOutException {
        log.info("confirmOuterService()");
        return testOuterService.confirmService(false);
    }

    @Override
    public void rollbackService() {
        log.info("rollbackService()");
        testInnerService1.rollbackService();
    }
}
