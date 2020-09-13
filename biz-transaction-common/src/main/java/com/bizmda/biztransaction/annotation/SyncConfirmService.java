package com.bizmda.biztransaction.annotation;

import java.lang.annotation.*;

/**
 * 同步超时确认服务的自定义注解
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SyncConfirmService {
    /**
     * 超时后发起的确认方法
     * @return
     */
    String confirmMethod() default "syncServiceConfirm";

    /**
     * 成功后的后续提交方法
     * @return
     */
    String commitMethod() default "syncServiceCommit";

    /**
     * 失败后的后续失败回滚处理
     * @return
     */
    String rollbackMethod() default "syncServiceRollback";
}

