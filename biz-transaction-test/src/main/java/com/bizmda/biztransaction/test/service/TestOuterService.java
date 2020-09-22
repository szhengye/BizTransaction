package com.bizmda.biztransaction.test.service;

import com.bizmda.biztransaction.exception.BizTranException;
import com.bizmda.biztransaction.exception.BizTranTimeOutException;
import com.bizmda.biztransaction.util.AsyncServiceCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TestOuterService {
    @Autowired
    private AsyncServiceCallback asyncServiceCallback;

    int maxTimeoutTimes = 3;
    int currentTimeoutTimes;

    public void setMaxTimeoutTimes(int maxTimeoutTimes) {
        this.maxTimeoutTimes = maxTimeoutTimes;
        this.currentTimeoutTimes = 0;
    }

    public void init() {
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

    public Object doServiceAsync(String serviceId,String transactionKey,String inParams) {
        log.info("doServiceAsync()");
        AsyncServiceCallback asyncServiceCallback = this.asyncServiceCallback;
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                    try {
                        Object result = asyncServiceCallback.callback(serviceId, transactionKey, "callback:"+ inParams);
                        log.info("回调结果:{}",result);
                    } catch (BizTranException e) {
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

    public void doServiceOfTimeout() throws BizTranTimeOutException {
        log.info("doServiceWithTimeout() 触发服务调用超时");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        throw new BizTranTimeOutException();
    }

    public boolean confirmService(boolean result) throws BizTranTimeOutException {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.currentTimeoutTimes ++;
        if (this.currentTimeoutTimes < this.maxTimeoutTimes) {
            log.info("confirmService()：响应超时");
            throw new BizTranTimeOutException();
        }
        log.info("confirmService({})",result);
        return result;
    }

    public boolean confirmServiceBeforeTimeout(int times,boolean result) throws BizTranTimeOutException {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.currentTimeoutTimes ++;
        if (this.currentTimeoutTimes <= times) {
            log.info("confirmServiceBeforeTimeout()：响应超时");
            throw new BizTranTimeOutException();
        }
        log.info("confirmServiceBeforeTimeout()：返回{}",result);
        return result;
    }
}
