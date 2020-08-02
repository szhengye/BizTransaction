package com.bizmda.biztransaction.test.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TestInnerService2 {

    public void process() {
        log.info("TestInnerService2.process()");
    }

    public void processWithException() throws Exception {
        log.info("TestInnerService2.processWithException");
        throw new Exception("TestInnerService2.processWithException");
    }

}
