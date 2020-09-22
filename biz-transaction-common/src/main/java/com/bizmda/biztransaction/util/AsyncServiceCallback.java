package com.bizmda.biztransaction.util;

import com.bizmda.biztransaction.exception.BizTranException;
import com.bizmda.biztransaction.service.BizTranService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 异步回调工具类
 */
@Slf4j
@Service
public class AsyncServiceCallback {
    @Autowired
    private BizTranService bizTranService ;

    /**
     * 外部服务异步回调后，应由开发者主动调用的方法，以触发回调后的业务逻辑
     * @param outerId 服务id
     * @param transactionKey 交易唯一主键
     * @param inParams 回调输入参数
     * @return 处理返回结果
     * @throws BizTranException
     */
    public Object callback(String outerId, String transactionKey, Object inParams) throws BizTranException {
        log.info("callback({},{},{})",outerId,transactionKey,inParams);
        return bizTranService.asyncServiceCallback(outerId,transactionKey,inParams);
    }
}
