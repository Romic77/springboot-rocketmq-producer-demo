package org.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQLocalRequestCallback;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.example.domain.entity.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author romic
 * @date 2021-11-20
 */
@RestController
@Slf4j
public class ProduderController {
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    AtomicInteger atomicInteger = new AtomicInteger(0);

    /**
     * 单个被叫号码下发
     *
     * @return void
     * @author romic
     * @date 2021-11-21 11:36
     */
    @GetMapping("/sendMsg")
    void sendMsg() {
        // 默认使用同步发送, 但拿不到回执, 源码见下文org.apache.rocketmq.spring.core.RocketMQTemplate.doSent
        // 单个被叫号码下发-得到单个号码消费完的状态
        for (int i = 0; i < 100; i++) {
            Task task = new Task(i, 150, getTel(), "400200100");

            //普通消息
            //rocketMQTemplate.convertAndSend("task-topic", task);

            //请求 应答语义支持
            // 同步发送需要在方法的参数中指明返回值类型
            //rocketMQTemplate.sendAndReceive("task-topic", MessageBuilder.withPayload(task).build(), String.class);

            // 异步发送request并且等待User类型的返回值;
            // timeout需要根据业务设置，5S肯定是不够的，默认我设置成1个小时
            rocketMQTemplate.sendAndReceive("task-topic", task, new RocketMQLocalRequestCallback<String>() {

                @Override
                public void onSuccess(String message) {
                    //System.out.printf("send task object and receive %s %n", message.toString());
                    //TODO 接收到了VCM消费成功之后，可以继续下发MYSQL的任务
                    log.info("消费者replay的消息:{}", message.toString());

                    if (atomicInteger.incrementAndGet() < 2) {
                        log.info("当前下发轮数：{}", atomicInteger.get());
                        //递归调用发送消息给VCM
                        sendMsg();
                    }
                }

                @Override
                public void onException(Throwable e) {
                    //响应异常需要处理
                    e.printStackTrace();
                }
            }, 3600000);
        }
    }

    /**
     * 批量下发被叫号码 - 减少rocketmq 创建链接的开销
     * 1.第一个被叫号码2秒挂掉，需要等到第100个号码 可能是1个小时之后才能响应回复给生产者下发100任务，不满足业务场景
     *
     * @return void
     * @author romic
     * @date 2021-11-21 11:36
     */
    @GetMapping("batchSendMsg")
    void batchSendMsg() {
        ArrayList<Task> taskList = new ArrayList<Task>();
        for (int i = 0; i < 100; i++) {
            Task task = new Task(i, 150, getTel(), "400200100");
            taskList.add(task);
        }

        // 异步发送request并且等待User类型的返回值
        rocketMQTemplate.sendAndReceive("task-topic", taskList, new RocketMQLocalRequestCallback<String>() {
            @Override
            public void onSuccess(String message) {
                //System.out.printf("send task object and receive %s %n", message.toString());
                //TODO 接收到了VCM消费成功之后，可以继续下发MYSQL的任务
                System.out.printf("消费者replay的消息 %s %n", message.toString());
            }

            @Override
            public void onException(Throwable e) {
                e.printStackTrace();
            }
        }, 5000);
    }


    /**
     * 返回手机号码
     */
    private static String[] telFirst = "134,135,136,137,138,139,150,151,152,157,158,159,130,131,132,155,156,133,153".split(",");

    private static String getTel() {
        int index = getNum(0, telFirst.length - 1);
        String first = telFirst[index];
        String second = String.valueOf(getNum(1, 888) + 10000).substring(1);
        String third = String.valueOf(getNum(1, 9100) + 10000).substring(1);
        return first + second + third;
    }

    public static int getNum(int start, int end) {
        return (int) (Math.random() * (end - start + 1) + start);
    }
}
