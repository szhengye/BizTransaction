package com.bizmda.biztransaction.test.service;

import com.bizmda.biztransaction.annotation.QueueService;
import com.bizmda.biztransaction.annotation.QueueServiceAOP;
import com.bizmda.biztransaction.service.AbstractTransaction;
import com.bizmda.biztransaction.service.AbstractTransaction2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

@Slf4j
@Service
@Scope("prototype")
public class ApplicationService6 extends AbstractTransaction {
    @Override
    public Object doService(Object inParams) {
//        String result = this.step1("world");
        ((ApplicationService6)AopContext.currentProxy()).step1("world",88);
//        log.info("result:{}",result);
        return null;
    }

    @QueueService
    public void step1(String str,Integer num) {
        log.info("step1({},{})",str,num);
//        return "hello:"+str + "，" + String.valueOf(num);
    }
}
