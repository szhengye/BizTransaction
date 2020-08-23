package com.bizmda.biztransaction.test.service;

import com.bizmda.biztransaction.exception.Transaction2Exception;
import com.bizmda.biztransaction.exception.TransactionTimeOutException;
import com.bizmda.biztransaction.service.AbstractTransaction2;
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

    public Object processAsync(Object inParams) {
        log.info("processAsync()");
        String transactionKey = (String)inParams;
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                    try {
                        AbstractTransaction2.callback("TestOuterService", transactionKey, "TestOuterService.processAsync() return object");
                    } catch (Transaction2Exception e) {
                        e.printStackTrace();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        return "Return by TestOuterService.processAsync() !";
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
