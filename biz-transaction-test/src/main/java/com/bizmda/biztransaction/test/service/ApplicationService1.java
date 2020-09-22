package com.bizmda.biztransaction.test.service;

import com.bizmda.biztransaction.annotation.SyncConfirmService;
import com.bizmda.biztransaction.exception.BizTranException;
import com.bizmda.biztransaction.exception.BizTranRespErrorException;
import com.bizmda.biztransaction.exception.BizTranTimeOutException;
import com.bizmda.biztransaction.service.AbstractBizTran;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ApplicationService1 extends AbstractBizTran {
    @Autowired
    private TestInnerService1 testInnerService1 ;
    @Autowired
    private TestInnerService2 testInnerService2 ;
    @Autowired
    private TestOuterService testOuterService ;

    @SyncConfirmService(confirmMethod="confirmSyncService",commitMethod="afterSyncService",rollbackMethod="rollbackService")
    public String doService(String msg) throws BizTranTimeOutException, BizTranRespErrorException {
        log.info("beforeSyncService()");
        testOuterService.init();
        testInnerService1.doService();
        testOuterService.doService(true);
        return "hello";
    }

    public Object afterSyncService() {
        log.info("afterSyncService()");
        return testInnerService2.doService();
    }

    public boolean confirmSyncService() throws BizTranTimeOutException {
        log.info("confirmOuterService()");
        return testOuterService.confirmService(false);
    }

    public void rollbackService() {
        log.info("rollbackService()");
        testInnerService1.rollbackService();
    }

    @Override
    public Object doService(Object inParams) throws BizTranException {
        return null;
    }
}
