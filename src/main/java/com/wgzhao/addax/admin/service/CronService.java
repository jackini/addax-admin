package com.wgzhao.addax.admin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CronService {
    private static final Logger logger = LoggerFactory.getLogger(CronService.class);
    
    /**
     * 获取下一次执行时间
     * @param cronExpression cron表达式
     * @param baseTime 基准时间
     * @return 下一次执行时间
     */
    public LocalDateTime getNextExecutionTime(String cronExpression, LocalDateTime baseTime) {
        try {
            CronExpression cron = CronExpression.parse(cronExpression);
            return cron.next(baseTime);
        } catch (Exception e) {
            logger.error("解析cron表达式失败: {}", cronExpression, e);
            throw new RuntimeException("Invalid cron expression: " + cronExpression, e);
        }
    }
    
    /**
     * 验证cron表达式是否有效
     * @param cronExpression cron表达式
     * @return 是否有效
     */
    public boolean isValidCronExpression(String cronExpression) {
        try {
            CronExpression.parse(cronExpression);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 计算未来几次执行时间
     * @param cronExpression cron表达式
     * @param baseTime 基准时间
     * @param times 次数
     * @return 未来执行时间数组
     */
    public LocalDateTime[] getNextExecutionTimes(String cronExpression, LocalDateTime baseTime, int times) {
        LocalDateTime[] executionTimes = new LocalDateTime[times];
        try {
            CronExpression cron = CronExpression.parse(cronExpression);
            LocalDateTime time = baseTime;
            
            for (int i = 0; i < times; i++) {
                time = cron.next(time);
                executionTimes[i] = time;
            }
            
            return executionTimes;
        } catch (Exception e) {
            logger.error("解析cron表达式失败: {}", cronExpression, e);
            throw new RuntimeException("Invalid cron expression: " + cronExpression, e);
        }
    }
}