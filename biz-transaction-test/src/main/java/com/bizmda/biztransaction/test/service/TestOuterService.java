package com.bizmda.biztransaction.test.service;

import com.bizmda.biztransaction.exception.TransactionException;
import com.bizmda.biztransaction.exception.TransactionTimeOutException;
import com.bizmda.biztransaction.service.AsyncServiceCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TestOuterService {
    @Autowired
    private AsyncServiceCallback asyncServiceCallback;

    int maxTimeoutTimes;
    int currentTimeoutTimes;

    public void setMaxTimeoutTimes(int maxTimeoutTimes) {
        this.maxTimeoutTimes = maxTimeoutTimes;
        this.currentTimeoutTimes = 0;
    }

    public boolean doService(boolean result) {
        log.info("doService() 返回:{}",result);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Object doServiceAsync(Object inParams) {
        log.info("doServiceAsync()");
        String transactionKey = (String)inParams;
        AsyncServiceCallback asyncServiceCallback = this.asyncServiceCallback;
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                    try {
                        asyncServiceCallback.callback("TestOuterService", transactionKey, "TestOuterService.processAsync() return object");
                    } catch (TransactionException e) {
                        e.printStackTrace();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        return "Return by TestOuterService.doServiceAsync() !";
    }

    public void doServiceOfTimeout() throws TransactionTimeOutException {
        log.info("doServiceWithTimeout() 触发服务调用超时");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        throw new TransactionTimeOutException();
    }

    public boolean confirmService(boolean result) throws TransactionTimeOutException {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.currentTimeoutTimes ++;
        if (this.currentTimeoutTimes < this.maxTimeoutTimes) {
            log.info("confirmService()：响应超时");
            throw new TransactionTimeOutException();
        }
        log.info("confirmService({})",result);
        return result;
    }
}
