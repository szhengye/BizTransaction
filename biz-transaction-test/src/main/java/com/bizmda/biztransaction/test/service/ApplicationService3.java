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
public class ApplicationService3 extends AbstractBizTran {
    @Autowired
    private TestInnerService1 testInnerService1 ;
    @Autowired
    private TestInnerService2 testInnerService2 ;
    @Autowired
    private TestOuterService testOuterService ;

    @SyncConfirmService(confirmMethod="confirmSyncService",commitMethod="afterSyncService",rollbackMethod="rollbackService")
    public String doService3(String msg) throws BizTranRespErrorException, BizTranTimeOutException {
        log.info("doService3({})",msg);
        testOuterService.init();
        testInnerService1.doService();
        testOuterService.doServiceOfTimeout();
        return "hello";
    }

    public Object afterSyncService() {
        log.info("afterSyncService()");

        testInnerService2.doService();
        return null;
    }

    public void confirmSyncService() throws BizTranTimeOutException {
        testOuterService.confirmService(true);
    }

    public void rollbackService() {
        log.info("rollbackService()");
        testInnerService1.rollbackService();
    }
}
