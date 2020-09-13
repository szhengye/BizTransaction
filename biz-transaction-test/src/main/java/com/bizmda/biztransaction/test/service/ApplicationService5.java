package com.bizmda.biztransaction.test.service;

import com.bizmda.biztransaction.exception.TransactionException;
import com.bizmda.biztransaction.service.AbstractBizTran2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Slf4j
@Service
@Scope("prototype")
public class ApplicationService5 extends AbstractBizTran2 {

    @Autowired
    private TestOuterService testOuterService ;

    @Override
    public Object doServiceBeforeAsync(Object inParams) throws TransactionException {
        log.info("doServiceBeforeAsync({})", inParams);
        String transactionKey = String.valueOf(Clock.systemDefaultZone().millis());
        this.saveState("TestOuterService",transactionKey,10);
        testOuterService.doServiceAsync("TestOuterService",transactionKey,"doServiceBeforeAsync");
        return null;
    }

    @Override
    public Object doServiceAfterAsync(Object inParams) throws TransactionException {
        log.info("doServiceAfterAsync({})", inParams);
        return null;
    }

    @Override
    public void callbackTimeout() throws TransactionException {

    }
}
