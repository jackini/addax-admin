package com.wgzhao.addax.admin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisQueueService
{
    private static final String QUEUE_NAME = "task_queue"; // Redis 中的队列名称

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 向 Redis 队列中添加任务
     *
     * @param taskId 任务 ID
     */
    public void enqueueTask(String taskId) {
        redisTemplate.opsForList().leftPush(QUEUE_NAME, taskId); // LPUSH
    }

    /**
     * 从 Redis 队列中阻塞式获取任务
     *
     * @return 返回任务 ID 或 null
     */
    public String dequeueTask() {
        return redisTemplate.opsForList().rightPop(QUEUE_NAME, 0, TimeUnit.SECONDS); // BRPOP 实现阻塞
    }
}
