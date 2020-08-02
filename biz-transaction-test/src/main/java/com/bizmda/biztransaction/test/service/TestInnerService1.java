package com.bizmda.biztransaction.test.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TestInnerService1 {

    public void process() {
        log.info("TestInnerService1.process()");
    }

    public void processWithException() throws Exception {
        log.info("TestInnerService1.processWithException");
        throw new Exception("TestInnerService1.processWithException");
    }

    public void cancel() {
        log.info("TestInnerService1.cancel()");
    }

    public void cancelWithException() throws Exception {
        log.info("TestInnerService1.cancelWithException");
        throw new Exception("TestInnerService1.cancelWithException");
    }

}
