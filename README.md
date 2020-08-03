# Biz-Transaction分布式事务中间件

Biz-Transaction是一个简单易用的分布式事务中间件，主要保证多个服务之间的分布式事务完整性。
相比其它的分布式事务中间件，具有更简单易用的特点，并能满足多种类型服务之间组装后的事务完整性保障：
* TCC事务中间件：只有简单的Try、Confirm、Cancel机制，对于Confirm重试机制无法灵活使用。
* Seata中间件：Seata有AT、MT和Saga三种处理机制，AT采用无痕数据库事务回滚的机制，简单粗暴，但没有事务痕迹是个硬伤；MT模式类似于TCC模式，灵活性较差；Saga模式通过状态图的方式来驱动事务流程，但配置复杂、开发复杂是影响广泛使用的原因。

以上几种分布式事务中间件都有优劣势，我们认识到在真实使用场景中，事务处理机制用一种机制来归纳，确实难度非常高。

Biz-Transaction分布式事务中间件采用方案是：

* 对事务处理核心服务采用抽象模板类封装的方式，每种抽象模板只针对一种特定的分布式事务场景；
* 抽象模板类把特定的分布式事务涉及的处理分支统一封装，服务只要继承抽象模板类，实现约定的分支方法即可，实现一个服务处理逻辑的高聚合；
* 抽象模板类统一封装了具体的分布式事务处理逻辑，服务开发者只需专注于服务实现，无需关注内在复杂的分布式处理逻辑。

## 安装

### 容器中安装并启动RabbitMQ
1. 运行：docker pull rabbitmq
2. 运行：docker run -d --name rabbitmq -e RABBITMQ_DEFAULT_USER=guest -e RABBITMQ_DEFAULT_PASS=guest -p 15672:15672 -p 5672:5672 rabbitmq:management
3. 下载“rabbitmq_delayed_message_exchange-3.8.0.ez”文件
3. 将下载文件拷贝到容器指定目录下：docker cp rabbitmq_delayed_message_exchange-3.8.0.ez rabbitmq:/plugins
4. 进行容器bash环境：docker exec -it <容器ID> bash
5. 在容器bash环境中运行：rabbitmq-plugins enable rabbitmq_delayed_message_exchange

### 启动测试应用
1. 从[BizTransaction](https://github.com/szhengye/BizTransaction)中下载项目源码；
2. 在Eclipse或IDEA中作为MAVEN项目导入；
3. 设置```biz-transaction-test/src/main/resources/application.yml```中的RabbitMQ的地址、用户名和密码：
4. 运行```src/com/bizmda/biztransaction/TransactionMqCenterApp.java```；

### 运行测试案例
1. 访问"http://127.0.0.1:8080/app1"，响应"doInnerService1->doOuterService(true)->doInnerService2"，后台运行日志：
```
2020-08-03 13:47:09.288  INFO 57377 --- [nio-8080-exec-1] c.b.b.test.controller.TestController     : doInnerService1->doOuterService(true)->doInnerService2
2020-08-03 13:47:09.289  INFO 57377 --- [nio-8080-exec-1] c.b.b.test.service.ApplicationService1   : doInnerService1()
2020-08-03 13:47:09.289  INFO 57377 --- [nio-8080-exec-1] c.b.b.test.service.TestInnerService1     : TestInnerService1.process()
2020-08-03 13:47:09.289  INFO 57377 --- [nio-8080-exec-1] c.b.b.test.service.ApplicationService1   : doOuterService()
2020-08-03 13:47:09.289  INFO 57377 --- [nio-8080-exec-1] c.b.b.test.service.TestOuterService      : TestOuterService.process()
2020-08-03 13:47:12.292  INFO 57377 --- [nio-8080-exec-1] c.b.b.test.service.ApplicationService1   : doInnerService2()
2020-08-03 13:47:12.293  INFO 57377 --- [nio-8080-exec-1] c.b.b.test.service.TestInnerService2     : TestInnerService2.process()
```
2. 访问"http://127.0.0.1:8080/app1"，响应"doInnerService1->doOuterService(true)->doInnerService2"
