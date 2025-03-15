package com.wgzhao.addax.admin.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wgzhao.addax.admin.model.CollectJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class JobQueue {
    private static final Logger logger = LoggerFactory.getLogger(JobQueue.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String QUEUE_KEY = "addax:job:queue";


    @Autowired
    public JobQueue(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
        // Register module for Java 8 date/time types
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        // Optional: configure dates to be serialized as strings rather than timestamps
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    }

    /**
     * 将作业加入队列
     * @param job 作业对象
     */
    public void enqueue(CollectJob job) {
        try {
            String jobJson = objectMapper.writeValueAsString(job);
            redisTemplate.opsForList().rightPush(QUEUE_KEY, jobJson);
            logger.info("作业 [{}] 已加入队列", job.getJobName());

        } catch (Exception e) {
            logger.error("将作业 [{}] 加入队列时发生错误", job.getJobName(), e);
            throw new RuntimeException("Failed to enqueue job", e);
        }
    }

    /**
     * 从队列获取作业
     * @return 作业对象，如果队列为空则返回null
     */
    public CollectJob dequeue() {
        try {
            String jobJson = redisTemplate.opsForList().leftPop(QUEUE_KEY);
            if (jobJson == null) {
                return null;
            }
            CollectJob job = objectMapper.readValue(jobJson, CollectJob.class);
            logger.info("从队列获取到作业 [{}], 当前队列大小 {}", job.getJobName(), getQueueSize());

            return job;
        } catch (Exception e) {
            logger.error("从队列获取作业时发生错误", e);
            throw new RuntimeException("Failed to dequeue job", e);
        }
    }

    /**
     * 获取队列长度
     * @return 队列中的作业数量
     */
    public long getQueueSize() {
        return redisTemplate.opsForList().size(QUEUE_KEY);
    }

    /**
     * 清空队列
     * @return 清除的作业数量
     */
    public long clearQueue() {
        long size = getQueueSize();
        redisTemplate.delete(QUEUE_KEY);
        logger.info("队列已清空，共移除 {} 个作业", size);
        return size;
    }
}