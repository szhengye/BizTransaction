package com.bizmda.biztransaction.test.service;

import com.bizmda.biztransaction.annotation.AsyncService;
import com.bizmda.biztransaction.exception.BizTranException;
import com.bizmda.biztransaction.service.AbstractBizTran;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Slf4j
@Service
public class ApplicationService8 extends AbstractBizTran {

    @Autowired
    private TestOuterService testOuterService ;

    @Override
    public Object doService(Object inParams) throws BizTranException {
        String transactionKey = String.valueOf(Clock.systemDefaultZone().millis());
        ((ApplicationService8) AopContext.currentProxy()).doAsyncService("TestOuterService",transactionKey);
        return null;
    }

    @AsyncService
    public Object doAsyncService(String serviceId,String transactionKey) {
        log.info("doAsyncService({},{})", serviceId,transactionKey);
        return null;
    }

    public void asyncServiceCallback(Object inParams) throws BizTranException {
        log.info("asyncServiceCallback({})", inParams);
    }

    public void asyncServiceTimeout() {
        log.warn("asyncServiceTimeout()");
    }
}
