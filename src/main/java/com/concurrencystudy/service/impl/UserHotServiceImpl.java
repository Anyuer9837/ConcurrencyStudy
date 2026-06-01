package com.concurrencystudy.service.impl;

import com.concurrencystudy.config.RabbitConfig;
import com.concurrencystudy.dao.UserHot;
import com.concurrencystudy.mapper.UserHotMapper;
import com.concurrencystudy.service.UserHotService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;

@Service
public class UserHotServiceImpl implements UserHotService {

    @Autowired
    private UserHotMapper userHotMapper;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public UserHot getUserByUid(Integer uid) {
        return userHotMapper.selectByPrimaryKey(uid);
    }

    @Override
    public boolean updateHot(UserHot userHot) {
        String cacheKey = "user:hot:" + userHot.getUid();
        String lockKey = "lock:user-hot:" + userHot.getUid();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 1. 尝试获取分布式锁，限时 5 秒
            if (lock.tryLock(15, TimeUnit.SECONDS)) {
                int newHot;
                // 2. 隔离并发下的 Redis 操作
                try {
                    String cachedHot = redisTemplate.opsForValue().get(cacheKey);
                    int currentHot;
                    if (cachedHot != null) {
                        currentHot = Integer.parseInt(cachedHot);
                    } else {
                        UserHot dbData = userHotMapper.selectByPrimaryKey(userHot.getUid());
                        if (dbData == null) return false;
                        currentHot = dbData.getHot();
                        redisTemplate.opsForValue().set(cacheKey, String.valueOf(currentHot));
                    }
                    // 内存计算并回写 Redis
                    newHot = currentHot + 1;
                    redisTemplate.opsForValue().set(cacheKey, String.valueOf(newHot));
                } catch (Exception e) {
                    System.err.println("【❌ Redis故障】操作Redis失败，原因: " + e.getMessage());
                    return false;
                }
                // 3. 隔离 MQ 消息发送
                try {
                    String message = userHot.getUid() + "," + newHot;
                    rabbitTemplate.convertAndSend(
                            RabbitConfig.HOT_EXCHANGE,
                            RabbitConfig.HOT_ROUTING_KEY,
                            message
                    );
                    return true;
                } catch (Exception e) {
                    System.err.println("【MQ故障】消息发送到RabbitMQ失败！原因: " + e.getMessage());

                    // 💡 开始执行回滚逻辑
                    System.out.println("【触发回滚】正在将 Redis 数据恢复原状...");
                    try {
                        // 使用 decrement 原子操作，把刚才加的 1 减回去
                        redisTemplate.opsForValue().decrement(cacheKey);
                        System.out.println("【回滚成功】Redis 数据已撤销，保证了数据一致性。");
                    } catch (Exception rollbackEx) {
                        // 🚨 极端边缘场景：刚才写 Redis 还好好的，回滚的时候 Redis 突然宕机或断网了！
                        System.err.println("【致命灾难】MQ发送失败，且 Redis 回滚也失败！出现严重数据不一致！");
                        System.err.println("急需人工介入！出问题的 UID: " + userHot.getUid() + "，报错: " + rollbackEx.getMessage());
                        // 真实生产环境中，这里通常会打印 Error 级别日志触发钉钉/邮件报警，
                        // 或者把这条失败记录写到一个本地磁盘日志文件里，等重启后人工修数据。
                    }
                    return false;
                }
            } else {
                System.out.println("【锁竞争激烈】获取分布式锁超时，其他线程正占用锁，本次更新放弃。");
                return false;
            }
        } catch (InterruptedException e) {
            System.out.println("【线程中断】等待锁的过程中线程被中断: " + e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        } finally {
            // 严格释放锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}