package com.bizmda.biztransaction.test.service;

import com.bizmda.biztransaction.annotation.QueueService;
import com.bizmda.biztransaction.service.AbstractBizTran;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ApplicationService6 extends AbstractBizTran {

    public Object doService6(Object inParams) {
        Person person = new Person();
        person.setAddress("address");
        person.setMobile("mobile");
        person.setName("name");
        this.getTranContext().setAttribute("person",person);
        ((ApplicationService6)AopContext.currentProxy()).step1("world",88,person);
        return null;
    }

    @QueueService
    public void step1(String str,Integer num,Person person) {
        log.info("step1({},{},{},{})",str,num,this.getTranContext().getAttribute("person"),person.toString());
    }
}
