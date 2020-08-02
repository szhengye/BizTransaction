package com.bizmda.biztransaction.test.service;

import com.bizmda.biztransaction.exception.TransactionTimeOutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TestOuterService {
    int maxTimeoutTimes;
    int currentTimeoutTimes;

    public void setMaxTimeoutTimes(int maxTimeoutTimes) {
        this.maxTimeoutTimes = maxTimeoutTimes;
        this.currentTimeoutTimes = 0;
    }

    public boolean process(boolean result) {
        log.info("TestOuterService.process()");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void processWithTimeout() throws TransactionTimeOutException {
        log.info("TestOuterService.processWithTimeout()");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        throw new TransactionTimeOutException();
    }

    public boolean confirmTimeoutAndReturn(boolean result) throws TransactionTimeOutException {
        log.info("TestOuterService.confirmTimeoutAndReturn(" + String.valueOf(result) + ")");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.currentTimeoutTimes ++;
        if (this.currentTimeoutTimes < this.maxTimeoutTimes) {
            throw new TransactionTimeOutException();
        }
        return result;
    }
}
