package org.example.controller;

import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.example.domain.entity.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author romic
 * @date 2021-11-20
 */
@RestController
public class ProduderController {
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @GetMapping("/sendMsg")
    void sendMsg(){
        // 默认使用同步发送, 但拿不到回执, 源码见下文org.apache.rocketmq.spring.core.RocketMQTemplate.doSent
        Task task=new Task(122,50,"13823764395","400200100");

          rocketMQTemplate.convertAndSend("task-topic", task);

/*        rocketMQTemplate.send("test-topic", MessageBuilder.withPayload(entity).build());
// 带tag
        rocketMQTemplate.convertAndSend("test-topic:tag1", entity);
        rocketMQTemplate.send("test-topic:tag2", MessageBuilder.withPayload(entity).build());*/
    }

}
