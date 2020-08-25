package com.bizmda.biztransaction.test.service;

import com.bizmda.biztransaction.exception.Transaction2Exception;
import com.bizmda.biztransaction.service.AbstractTransaction2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Slf4j
@Service
public class ApplicationService5 extends AbstractTransaction2 {

    @Autowired
    private TestOuterService testOuterService ;

    @Override
    public Object doService(Object inParams) throws Transaction2Exception {
        log.info("doServiceBeforeAsync({})", inParams);
        String transactionKey = String.valueOf(Clock.systemDefaultZone().millis());
        this.saveState("TestOuterService",transactionKey,10);
        log.info("saveState('TestOuterService',{},10)",transactionKey);
        testOuterService.processAsync(transactionKey);
        return null;
    }

    @Override
    public Object doServiceAfterAsync(Object inParams) throws Transaction2Exception {
        log.info("doServiceAfterAsync({})", inParams);
        return null;
    }

    @Override
    public void callbackTimeout() throws Transaction2Exception {

    }
}
