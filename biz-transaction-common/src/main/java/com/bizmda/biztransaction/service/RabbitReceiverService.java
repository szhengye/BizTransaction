package com.bizmda.biztransaction.service;

import com.bizmda.biztransaction.exception.TransactionMaxConfirmFailException;
import com.bizmda.biztransaction.exception.TransactionTimeOutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * RabbitMQ接收消息服务
 **/
@Slf4j
@Service
public class RabbitReceiverService {
    @Autowired
    private RabbitSenderService rabbitSenderService ;

    /**
     * ttl消息
     * @param map
     */
    @RabbitListener(queues = {"dead.real.queue"},containerFactory = "multiListenerContainer")
    public void consumeExpirMsg(Map map){
        int type = Integer.parseInt((String)map.get("type"));
        String beanName = (String)map.get("beanName");
        int no = Integer.parseInt((String)map.get("no"));
        Object msg = (Object)map.get("msg");
        Map context = (Map)map.get("context");
        switch (type) {
            case 1:
                AbstractTransaction1 transaction1 = (AbstractTransaction1)SpringContextsUtil.getBean(beanName, AbstractTransaction1.class);
                transaction1.setTransactionContext(context);
                try {
                    if (transaction1.confirmOuterService(msg)) {
                        transaction1.doInnerService2(msg);
                    }
                    else {
                        transaction1.cancelInnerService1(msg);
                    }
                } catch (TransactionTimeOutException e) {
                    no = no + 1;
                    try {
                        rabbitSenderService.sendTTLExpireMsg(type, beanName, no, msg, context);
                    } catch (TransactionMaxConfirmFailException transactionMaxConfirmFailException) {
                        transactionMaxConfirmFailException.printStackTrace();
                    }
                }

        }
    }
}












