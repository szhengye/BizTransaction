# Biz-Transaction业务交易中间件

Biz-Transaction是一个简单易用的业务交易中间件，主要是对业务交易系统常见的交易处理类型进行规范化、模板化处理。
主要支持的交易处理类型有：
1. AbstractTransaction1：涉及一个第三方服务的交易处理类型，在第三方服务超时后，采用重试查询和向前补偿的机制，来保证整个分布式事务。
2. AbstractTransaction2：涉及第三方服务异步返回的交易处理类型，实现第三方异步调用前和异步回调的统一处理。
## 一、AbstractTransaction1（涉及一个第三方服务的分布式事务交易模板）
AbstractTransaction1交易模板，主要保证多个服务之间的分布式事务完整性。
相比其它的分布式事务中间件，具有更简单易用的特点，并能满足多种类型服务之间组装后的事务完整性保障：
* TCC事务中间件：只有简单的Try、Confirm、Cancel机制，对于Confirm重试机制无法灵活使用。
* Seata中间件：Seata有AT、MT和Saga三种处理机制，AT采用无痕数据库事务回滚的机制，简单粗暴，但没有事务痕迹是个硬伤；MT模式类似于TCC模式，灵活性较差；Saga模式通过状态图的方式来驱动事务流程，但配置复杂、开发复杂是影响广泛使用的原因。

以上几种分布式事务中间件都有优劣势，我们认识到在真实使用场景中，事务处理机制用一种机制来归纳，确实难度非常高。

Biz-Transaction分布式事务中间件采用方案是：

* 对事务处理核心服务采用抽象模板类封装的方式，每种抽象模板只针对一种特定的分布式事务场景；
* 抽象模板类把特定的分布式事务涉及的处理分支统一封装，服务只要继承抽象模板类，实现约定的分支方法即可，实现一个服务处理逻辑的高聚合；
* 抽象模板类统一封装了具体的分布式事务处理逻辑，服务开发者只需专注于服务实现，无需关注内在复杂的分布式处理逻辑。

### 运行机制

Biz-Transaction在设计架构上可以同时支持多种分布式事务模式，目前只简单实现了最常用的“in-out-in事务模式"，后续可以在些框架上扩充其它类型的分布式事务模式。


#### in-out-in事务模式

in-out-in事务模式是一种比较常见的整合单个外部第三方应用的事务模式，事务处理流程是：
1. 调用内部系统的服务：由内部系统提供的服务，一般是调用外部第三方应用前需要做的前置处理。
2. 调用外部第三方应用的服务：由外部第三方应用提供的服务，会大量存在响应超时的可能性。对于第三方应用的超时，事务中间件会采用重发确认交易的处理逻辑，一般是查询上笔交易的最终处理状态，根据交易处理状态来决定后续的处理逻辑，一般处理成功会继续后续服务，处理失败会对第1步的服务做补偿（冲正）处理。
3. 调用内部系统的服务：由内部系统提供的服务，是调用外部第三方应用成功后，要做的后续处理。

in-out-in事务模式，在编码实现时是继承com.bizmda.biztransaction.service.AbstractTransaction1抽象类，实现其中的5个方法即可：
```java
    // 实现第1步内部服务的处理逻辑
    public abstract void doInnerService1(Object inParams);
    // 实现第2步调用外部第三方应用的处理逻辑，如果响应超时，应抛出TransactionTimeoutException，Biz-Transaction会根据超时重试机制自动重发，具体实现是通过RabbitMQ的延迟队列来实现的。
    public abstract boolean doOuterService() throws TransactionTimeOutException;
    // 实现第2步内部服务的处理逻辑
    public abstract Object doInnerService2();
    // 实现第2步调用外部第三方应用超时无响应后，后续向第三方应用发起交易确认的处理逻辑，如果超时，应抛出TransactionTimeoutException，Biz-Transaction会根据超时重试机制自动重发，具体实现是通过RabbitMQ的延迟队列来实现的。
    public abstract boolean confirmOuterService() throws TransactionTimeOutException;
    // 针对第1步内部服务的补偿服务处理逻辑。
    public abstract void cancelInnerService1();
```
![avatar](https://www.processon.com/chart_image/id/5f445d86e401fd5f24858786.png)

### 安装

#### 容器中安装并启动RabbitMQ
1. 运行：docker pull rabbitmq
2. 运行：docker run -d --name rabbitmq -e RABBITMQ_DEFAULT_USER=guest -e RABBITMQ_DEFAULT_PASS=guest -p 15672:15672 -p 5672:5672 rabbitmq:management
3. 下载“rabbitmq_delayed_message_exchange-3.8.0.ez”文件
3. 将下载文件拷贝到容器指定目录下：docker cp rabbitmq_delayed_message_exchange-3.8.0.ez rabbitmq:/plugins
4. 进行容器bash环境：docker exec -it <容器ID> bash
5. 在容器bash环境中运行：rabbitmq-plugins enable rabbitmq_delayed_message_exchange

#### 启动测试应用
1. 从[BizTransaction](https://github.com/szhengye/BizTransaction)中下载项目源码；
2. 在Eclipse或IDEA中作为MAVEN项目导入；
3. 设置```biz-transaction-test/src/main/resources/application.yml```中的RabbitMQ的地址、用户名和密码：
4. 运行```src/com/bizmda/biztransaction/TransactionMqCenterApp.java```；

#### 运行测试案例

1. 访问"`http://127.0.0.1:8080/app1`"

![avatar](https://cdn.nlark.com/yuque/__puml/96a043e2a75a320a37a822fb9ec77b65.svg)

浏览器响应：
```
doInnerService1->doOuterService(true)->doInnerService2
```
后台运行日志：
```
2020-08-01 13:47:09.288  INFO 57377 --- [nio-8080-exec-1] c.b.b.test.controller.TestController     : doInnerService1->doOuterService(true)->doInnerService2
2020-08-01 13:47:09.289  INFO 57377 --- [nio-8080-exec-1] c.b.b.test.service.ApplicationService1   : doInnerService1()
2020-08-01 13:47:09.289  INFO 57377 --- [nio-8080-exec-1] c.b.b.test.service.TestInnerService1     : TestInnerService1.process()
2020-08-01 13:47:09.289  INFO 57377 --- [nio-8080-exec-1] c.b.b.test.service.ApplicationService1   : doOuterService()
2020-08-01 13:47:09.289  INFO 57377 --- [nio-8080-exec-1] c.b.b.test.service.TestOuterService      : TestOuterService.process()
2020-08-01 13:47:12.292  INFO 57377 --- [nio-8080-exec-1] c.b.b.test.service.ApplicationService1   : doInnerService2()
2020-08-01 13:47:12.293  INFO 57377 --- [nio-8080-exec-1] c.b.b.test.service.TestInnerService2     : TestInnerService2.process()
```

2. 访问"`http://127.0.0.1:8080/app2`"

![avatar](https://cdn.nlark.com/yuque/__puml/a83cc561f4f99140c9255ae5ace0d8be.svg)

浏览器响应：
```
doInnerService1->doOuterService(false)->cancelInnerService1
```
后台运行日志：
```
2020-08-01 13:57:39.232  INFO 57377 --- [nio-8080-exec-5] c.b.b.test.controller.TestController     : doInnerService1->doOuterService(false)->cancelInnerService1
2020-08-01 13:57:39.235  INFO 57377 --- [nio-8080-exec-5] c.b.b.test.service.TestInnerService1     : TestInnerService1.process()
2020-08-01 13:57:39.236  INFO 57377 --- [nio-8080-exec-5] c.b.b.test.service.ApplicationService2   : doOuterService()
2020-08-01 13:57:39.236  INFO 57377 --- [nio-8080-exec-5] c.b.b.test.service.TestOuterService      : TestOuterService.process()
2020-08-01 13:57:42.237  INFO 57377 --- [nio-8080-exec-5] c.b.b.test.service.ApplicationService2   : cancelInnerService1()
2020-08-01 13:57:42.238  INFO 57377 --- [nio-8080-exec-5] c.b.b.test.service.TestInnerService1     : TestInnerService1.cancel()
```

3. 访问"`http://127.0.0.1:8080/app3`"

![avatar](https://cdn.nlark.com/yuque/__puml/adb3a48fddeb158c56ae182f84beb555.svg)

浏览器响应：
```
doInnerService1->doOuterService(timeout)->confirmOuterService(true)->doInnerService2
```
后台运行日志：
```
2020-08-01 13:58:49.141  INFO 57377 --- [ntContainer#0-1] c.b.b.test.service.ApplicationService3   : confirmOuterService()
2020-08-01 13:58:49.142  INFO 57377 --- [ntContainer#0-1] c.b.b.test.service.TestOuterService      : TestOuterService.confirmTimeoutAndReturn(true)
2020-08-01 13:58:49.791  INFO 57377 --- [nio-8080-exec-8] c.b.b.test.controller.TestController     : doInnerService1->doOuterService(timeout)->confirmOuterService(true)->doInnerService2
2020-08-01 13:58:49.792  INFO 57377 --- [nio-8080-exec-8] c.b.b.test.service.TestInnerService1     : TestInnerService1.process()
2020-08-01 13:58:49.792  INFO 57377 --- [nio-8080-exec-8] c.b.b.test.service.ApplicationService3   : doOuterService()
2020-08-01 13:58:49.792  INFO 57377 --- [nio-8080-exec-8] c.b.b.test.service.TestOuterService      : TestOuterService.processWithTimeout()
2020-08-01 13:58:52.146  INFO 57377 --- [ntContainer#0-1] c.b.b.service.RabbitSenderService        : sendTTLExpireMsg(1, applicationService3, 3, hello, null)
2020-08-01 13:58:52.146  INFO 57377 --- [ntContainer#0-1] c.b.b.service.RabbitSenderService        : Message expiration：8000
2020-08-01 13:58:52.164  INFO 57377 --- [ 127.0.0.1:5672] c.b.b.config.RabbitmqConfig              : 消息发送成功:correlationData(null),ack(true),cause(null)
2020-08-01 13:58:52.797  INFO 57377 --- [nio-8080-exec-8] c.b.b.service.RabbitSenderService        : sendTTLExpireMsg(1, applicationService3, 0, hello, null)
2020-08-01 13:58:52.797  INFO 57377 --- [nio-8080-exec-8] c.b.b.service.RabbitSenderService        : Message expiration：0
2020-08-01 13:58:52.819  INFO 57377 --- [ 127.0.0.1:5672] c.b.b.config.RabbitmqConfig              : 消息发送成功:correlationData(null),ack(true),cause(null)
2020-08-01 13:59:00.152  INFO 57377 --- [ntContainer#0-3] c.b.b.test.service.ApplicationService3   : confirmOuterService()
2020-08-01 13:59:00.152  INFO 57377 --- [ntContainer#0-2] c.b.b.test.service.ApplicationService3   : confirmOuterService()
2020-08-01 13:59:00.152  INFO 57377 --- [ntContainer#0-3] c.b.b.test.service.TestOuterService      : TestOuterService.confirmTimeoutAndReturn(true)
2020-08-01 13:59:00.152  INFO 57377 --- [ntContainer#0-2] c.b.b.test.service.TestOuterService      : TestOuterService.confirmTimeoutAndReturn(true)
2020-08-01 13:59:03.156  INFO 57377 --- [ntContainer#0-2] c.b.b.test.service.ApplicationService3   : doInnerService2()
2020-08-01 13:59:03.156  INFO 57377 --- [ntContainer#0-2] c.b.b.test.service.TestInnerService2     : TestInnerService2.process()
2020-08-01 13:59:03.158  INFO 57377 --- [ntContainer#0-3] c.b.b.service.RabbitSenderService        : sendTTLExpireMsg(1, applicationService3, 4, hello, null)
2020-08-01 13:59:03.159  INFO 57377 --- [ntContainer#0-3] c.b.b.service.RabbitSenderService        : Message expiration：16000
2020-08-01 13:59:03.174  INFO 57377 --- [ 127.0.0.1:5672] c.b.b.config.RabbitmqConfig              : 消息发送成功:correlationData(null),ack(true),cause(null)
2020-08-01 13:59:19.186  INFO 57377 --- [ntContainer#0-4] c.b.b.test.service.ApplicationService3   : confirmOuterService()
2020-08-01 13:59:19.186  INFO 57377 --- [ntContainer#0-4] c.b.b.test.service.TestOuterService      : TestOuterService.confirmTimeoutAndReturn(true)
2020-08-01 13:59:22.190  INFO 57377 --- [ntContainer#0-4] c.b.b.test.service.ApplicationService3   : doInnerService2()
2020-08-01 13:59:22.191  INFO 57377 --- [ntContainer#0-4] c.b.b.test.service.TestInnerService2     : TestInnerService2.process()
```

4. 访问"`http://127.0.0.1:8080/app4`"

![avatar](https://cdn.nlark.com/yuque/__puml/a3ba11002ac94b1fcb7617c06f34ab58.svg)

浏览器响应：
```
doInnerService1->doOuterService(timeout)->confirmOuterService(false)->cancelInnerService1
```
后台运行日志：
```
2020-08-01 14:00:32.617  INFO 57377 --- [nio-8080-exec-2] c.b.b.test.controller.TestController     : doInnerService1->doOuterService(timeout)->confirmOuterService(false)->cancelInnerService1
2020-08-01 14:00:32.617  INFO 57377 --- [nio-8080-exec-2] c.b.b.test.service.TestInnerService1     : TestInnerService1.process()
2020-08-01 14:00:32.618  INFO 57377 --- [nio-8080-exec-2] c.b.b.test.service.ApplicationService4   : doOuterService()
2020-08-01 14:00:32.618  INFO 57377 --- [nio-8080-exec-2] c.b.b.test.service.TestOuterService      : TestOuterService.processWithTimeout()
2020-08-01 14:00:35.624  INFO 57377 --- [nio-8080-exec-2] c.b.b.service.RabbitSenderService        : sendTTLExpireMsg(1, applicationService4, 0, hello, null)
2020-08-01 14:00:35.625  INFO 57377 --- [nio-8080-exec-2] c.b.b.service.RabbitSenderService        : Message expiration：0
2020-08-01 14:00:35.648  INFO 57377 --- [ntContainer#0-5] c.b.b.test.service.ApplicationService4   : confirmOuterService()
2020-08-01 14:00:35.648  INFO 57377 --- [ntContainer#0-5] c.b.b.test.service.TestOuterService      : TestOuterService.confirmTimeoutAndReturn(false)
2020-08-01 14:00:35.652  INFO 57377 --- [ 127.0.0.1:5672] c.b.b.config.RabbitmqConfig              : 消息发送成功:correlationData(null),ack(true),cause(null)
2020-08-01 14:00:38.654  INFO 57377 --- [ntContainer#0-5] c.b.b.service.RabbitSenderService        : sendTTLExpireMsg(1, applicationService4, 1, hello, null)
2020-08-01 14:00:38.654  INFO 57377 --- [ntContainer#0-5] c.b.b.service.RabbitSenderService        : Message expiration：2000
2020-08-01 14:00:38.666  INFO 57377 --- [ 127.0.0.1:5672] c.b.b.config.RabbitmqConfig              : 消息发送成功:correlationData(null),ack(true),cause(null)
2020-08-01 14:00:40.703  INFO 57377 --- [ntContainer#0-1] c.b.b.test.service.ApplicationService4   : confirmOuterService()
2020-08-01 14:00:40.704  INFO 57377 --- [ntContainer#0-1] c.b.b.test.service.TestOuterService      : TestOuterService.confirmTimeoutAndReturn(false)
2020-08-01 14:00:43.709  INFO 57377 --- [ntContainer#0-1] c.b.b.service.RabbitSenderService        : sendTTLExpireMsg(1, applicationService4, 2, hello, null)
2020-08-01 14:00:43.710  INFO 57377 --- [ntContainer#0-1] c.b.b.service.RabbitSenderService        : Message expiration：4000
2020-08-01 14:00:43.721  INFO 57377 --- [ 127.0.0.1:5672] c.b.b.config.RabbitmqConfig              : 消息发送成功:correlationData(null),ack(true),cause(null)
2020-08-01 14:00:47.751  INFO 57377 --- [ntContainer#0-3] c.b.b.test.service.ApplicationService4   : confirmOuterService()
2020-08-01 14:00:47.751  INFO 57377 --- [ntContainer#0-3] c.b.b.test.service.TestOuterService      : TestOuterService.confirmTimeoutAndReturn(false)
2020-08-01 14:00:50.751  INFO 57377 --- [ntContainer#0-3] c.b.b.test.service.ApplicationService4   : cancelInnerService1()
2020-08-01 14:00:50.752  INFO 57377 --- [ntContainer#0-3] c.b.b.test.service.TestInnerService1     : TestInnerService1.cancel()
```
