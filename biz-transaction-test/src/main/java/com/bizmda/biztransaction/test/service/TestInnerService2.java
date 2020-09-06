package com.bizmda.biztransaction.test.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TestInnerService2 {

    public Object doService() {
        log.info("doService()");
        return "TestInnerService2.doService()返回结果";
    }

}
