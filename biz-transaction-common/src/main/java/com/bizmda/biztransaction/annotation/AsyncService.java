package com.bizmda.biztransaction.annotation;

import java.lang.annotation.*;

/**
 * 支持回调的异步服务注解
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AsyncService {
    /**
     * 设置异步回调方法
     * @return
     */
    String callbackMethod() default "asyncServiceCallback";

    /**
     * 设置超时后回调方法
     * @return
     */
    String timeoutMethod() default "asyncServiceTimeout";

    /**
     * 设置异步回调的超时时间
     * @return
     */
    int timeout() default 10;
}