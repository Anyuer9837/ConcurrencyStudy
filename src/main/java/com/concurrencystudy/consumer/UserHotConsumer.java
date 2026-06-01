package com.concurrencystudy.consumer;

import com.concurrencystudy.config.RabbitConfig;
import com.concurrencystudy.dao.UserHot;
import com.concurrencystudy.mapper.UserHotMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserHotConsumer {

    @Autowired
    private UserHotMapper userHotMapper;

    // @RabbitListener 会让 Spring 自动启动后台线程死循环监听这个队列
    @RabbitListener(queues = RabbitConfig.HOT_QUEUE)
    public void receiveUserHotMessage(String message) {
        try {
            // 1. 解析消息（把 "1,11" 拆成 uid=1, hot=11）
            String[] parts = message.split(",");
            Integer uid = Integer.parseInt(parts[0]);
            Integer newHot = Integer.parseInt(parts[1]);
            UserHot updateData = new UserHot();
            updateData.setUid(uid);
            updateData.setHot(newHot);
            userHotMapper.updateByPrimaryKeySelective(updateData);

            // System.out.println("【MQ成功落盘】用户 " + uid + " 的热度已同步为 " + newHot);
        } catch (Exception e) {
            System.err.println("【MQ消费失败】" + e.getMessage());
            // 注意：这里如果抛出异常，RabbitMQ 会自动把这条消息重新放回队列头部，实现自动重试！
            throw e;
        }
    }
}