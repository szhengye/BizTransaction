package com.bizmda.biztransaction.test.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TestInnerService1 {

    public void doService() {
        log.info("doService()");
    }

    public void rollbackService() {
        log.info("rollbackService()");
    }

}
