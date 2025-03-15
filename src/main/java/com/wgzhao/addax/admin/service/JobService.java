package com.wgzhao.addax.admin.service;

import com.wgzhao.addax.admin.model.CollectJob;
import com.wgzhao.addax.admin.model.CollectTask;
import com.wgzhao.addax.admin.model.DataSource;
import com.wgzhao.addax.admin.model.JobExecution;
import com.wgzhao.addax.admin.model.JobTaskRelation;
import com.wgzhao.addax.admin.model.TableColumn;
import com.wgzhao.addax.admin.model.TableMetadata;
import com.wgzhao.addax.admin.repository.DataSourceRepository;
import com.wgzhao.addax.admin.repository.JobRepository;
import com.wgzhao.addax.admin.repository.JobTaskRelationRepository;
import com.wgzhao.addax.admin.repository.TableColumnRepository;
import com.wgzhao.addax.admin.repository.TaskRepository;
import com.wgzhao.addax.admin.scheduler.JobQueue;
import com.wgzhao.addax.admin.utils.DbUtil;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class JobService
{
    private static final Logger logger = LoggerFactory.getLogger(JobService.class);

    private final JobRepository jobRepository;
    private final TaskRepository taskRepository;
    private final JobTaskRelationRepository jobTaskRelationRepository;
    private final JobExecutionService jobExecutionService;
    private final JobQueue jobQueue;
    private final FieldMappingService fieldMappingService;
    private final TableColumnRepository tableColumnRepository;
    private final DataSourceRepository dataSourceRepository;

    /**
     * 获取所有作业
     *
     * @return 作业列表
     */
    public List<CollectJob> getAllJobs()
    {
        return jobRepository.findAll();
    }

    /**
     * 获取指定作业
     *
     * @param id 作业ID
     * @return 作业对象
     */
    public Optional<CollectJob> getJob(Long id)
    {
        return jobRepository.findById(id);
    }

    /**
     * 创建新作业
     *
     * @param job 作业对象
     * @return 保存后的作业对象
     */
    @Transactional
    public CollectJob createJob(CollectJob job)
    {
        // 设置默认值
        if (job.getJobStatus() == null) {
            job.setJobStatus("N"); // 未运行
        }

        if (job.getConcurrentFlag() == null) {
            job.setConcurrentFlag(0); // 默认不允许并发
        }

        if (job.getTimeoutSecs() == null) {
            job.setTimeoutSecs(7200); // 默认2小时超时
        }

        if (job.getRetryTimes() == null) {
            job.setRetryTimes(3); // 默认重试3次
        }

        if (job.getRetryInterval() == null) {
            job.setRetryInterval(60); // 默认重试间隔60秒
        }

        CollectJob savedJob = jobRepository.save(job);
        logger.info("已创建新作业，ID: {}, 名称: {}", savedJob.getId(), savedJob.getJobName());
        return savedJob;
    }

    /**
     * 更新作业
     *
     * @param job 作业对象
     * @return 更新后的作业对象
     */
    @Transactional
    public CollectJob updateJob(CollectJob job)
    {
        CollectJob savedJob = jobRepository.save(job);
        logger.info("已更新作业，ID: {}, 名称: {}", savedJob.getId(), savedJob.getJobName());
        return savedJob;
    }

    /**
     * 删除作业
     *
     * @param id 作业ID
     */
    @Transactional
    public void deleteJob(Long id)
    {
        // 先删除作业任务关联
        jobTaskRelationRepository.deleteByJobId(id);

        // 再删除作业
        jobRepository.deleteById(id);
        logger.info("已删除作业，ID: {}", id);
    }

    /**
     * 手动触发作业
     *
     * @param id 作业ID
     * @return 执行记录ID
     */
    @Transactional
    public JobExecution triggerJob(Long id)
    {
        CollectJob job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(STR."Job not found: \{id}"));

        // 创建执行记录
        JobExecution jobExecution = jobExecutionService.createJobExecution(id, "MANUAL");

        // 更新作业状态
        job.setJobStatus("R"); // 运行中
        job.setLastFireTime(LocalDateTime.now());
        jobRepository.save(job);

        // 提交到作业队列
        jobQueue.enqueue(job);

        logger.info("已手动触发作业，ID: {}, 名称: {}", id, job.getJobName());
        return jobExecution;
    }

    /**
     * 暂停作业
     *
     * @param id 作业ID
     * @return 更新后的作业对象
     */
    @Transactional
    public CollectJob pauseJob(Long id)
    {
        CollectJob job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found: " + id));

        job.setJobStatus("P"); // 暂停
        CollectJob savedJob = jobRepository.save(job);

        logger.info("已暂停作业，ID: {}, 名称: {}", id, job.getJobName());
        return savedJob;
    }

    /**
     * 恢复作业
     *
     * @param id 作业ID
     * @return 更新后的作业对象
     */
    @Transactional
    public CollectJob resumeJob(Long id)
    {
        CollectJob job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found: " + id));

        job.setJobStatus("N"); // 未运行
        CollectJob savedJob = jobRepository.save(job);

        logger.info("已恢复作业，ID: {}, 名称: {}", id, job.getJobName());
        return savedJob;
    }

    /**
     * 关联任务到作业
     *
     * @param jobId 作业ID
     * @param taskId 任务ID
     * @param order 执行顺序
     */
    @Transactional
    public void associateTaskWithJob(Long jobId, Long taskId, int order)
    {
        // 检查是否已存在关联
        JobTaskRelation existing = jobTaskRelationRepository.findByJobIdAndTaskId(jobId, taskId);

        if (existing != null) {
            // 更新执行顺序
            existing.setTaskOrder(order);
            jobTaskRelationRepository.save(existing);
            logger.info("已更新任务 {} 与作业 {} 的关联顺序为 {}", taskId, jobId, order);
        }
        else {
            // 创建新关联
            jobTaskRelationRepository.createJobTaskRelation(jobId, taskId, order);
            logger.info("已将任务 {} 关联到作业 {}, 执行顺序: {}", taskId, jobId, order);
        }
    }

    /**
     * 解除任务与作业的关联
     *
     * @param jobId 作业ID
     * @param taskId 任务ID
     */
    @Transactional
    public void disassociateTaskFromJob(Long jobId, Long taskId)
    {
        JobTaskRelation relation = jobTaskRelationRepository.findByJobIdAndTaskId(jobId, taskId);

        if (relation != null) {
            jobTaskRelationRepository.delete(relation);
            logger.info("已解除任务 {} 与作业 {} 的关联", taskId, jobId);
        }
    }

    /**
     * 获取作业关联的任务列表（包含执行顺序）
     *
     * @param jobId 作业ID
     * @return 任务列表（包含执行顺序）
     */
    public List<Map<String, Object>> getTasksWithOrderByJobId(Long jobId)
    {
        List<JobTaskRelation> relations = jobTaskRelationRepository.findByJobIdOrderByTaskOrder(jobId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (JobTaskRelation relation : relations) {
            Optional<CollectTask> taskOpt = taskRepository.findById(relation.getTaskId());

            if (taskOpt.isPresent()) {
                CollectTask task = taskOpt.get();
                Map<String, Object> taskMap = new HashMap<>();
                taskMap.put("taskId", task.getId());
                taskMap.put("sourceTable", task.getSourceTable());
                taskMap.put("targetTable", task.getTargetTable());
                taskMap.put("taskStatus", task.getTaskStatus());
                taskMap.put("order", relation.getTaskOrder());

                result.add(taskMap);
            }
        }

        return result;
    }

    /**
     * 批量触发作业
     *
     * @param jobGroup 作业组（可选）
     * @param includeDisabled 是否包含禁用的作业
     * @return 触发结果
     */
    @Transactional
    public Map<String, Object> triggerBatchJobs(String jobGroup, boolean includeDisabled)
    {
        List<CollectJob> jobs;

        // 根据条件查询作业
        if (jobGroup != null && !jobGroup.trim().isEmpty()) {
            // 如果指定了作业组，则只触发该组的作业
            if (includeDisabled) {
                // 包含所有状态的作业
                jobs = jobRepository.findByJobGroup(jobGroup);
            }
            else {
                // 只包含未运行状态的作业
                jobs = jobRepository.findByJobGroupAndJobStatus(jobGroup, "N");
            }
        }
        else {
            // 未指定作业组，触发所有作业
            if (includeDisabled) {
                // 包含所有状态的作业，但排除运行中的作业
                jobs = jobRepository.findByJobStatusNot("R");
            }
            else {
                // 只包含未运行状态的作业
                jobs = jobRepository.findByJobStatus("N");
            }
        }

        logger.info("批量触发：找到 {} 个符合条件的作业", jobs.size());

        int successCount = 0;
        int failureCount = 0;
        List<Map<String, Object>> triggeredJobs = new ArrayList<>();

        // 批量触发作业
        for (CollectJob job : jobs) {
            try {
                // 创建执行记录
                JobExecution jobExecution = jobExecutionService.createJobExecution(job.getId(), "MANUAL_BATCH");

                // 更新作业状态
                job.setJobStatus("R"); // 运行中
                job.setLastFireTime(LocalDateTime.now());
                jobRepository.save(job);

                // 提交到作业队列
                jobQueue.enqueue(job);

                // 记录成功信息
                Map<String, Object> jobInfo = new HashMap<>();
                jobInfo.put("jobId", job.getId());
                jobInfo.put("jobName", job.getJobName());
                jobInfo.put("executionId", jobExecution.getJobId());
                jobInfo.put("status", "success");
                triggeredJobs.add(jobInfo);

                successCount++;
                logger.info("成功触发作业 [{}]", job.getJobName());
            }
            catch (Exception e) {
                // 记录失败信息
                Map<String, Object> jobInfo = new HashMap<>();
                jobInfo.put("jobId", job.getId());
                jobInfo.put("jobName", job.getJobName());
                jobInfo.put("status", "failure");
                jobInfo.put("error", e.getMessage());
                triggeredJobs.add(jobInfo);

                failureCount++;
                logger.error("触发作业 [{}] 失败", job.getJobName(), e);
            }
        }

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("totalJobs", jobs.size());
        result.put("successCount", successCount);
        result.put("failureCount", failureCount);
        result.put("triggeredJobs", triggeredJobs);
        result.put("triggerTime", LocalDateTime.now());

        return result;
    }

    /**
     * 从给定的数据源表中获取表字段信息，填充到 table_column 表中
     * 其中 target_column_name 默认和 column_name 一致
     * target_column_type 则根据 field_mapping_rule 规则生成
     *
     * @param collectJob {@link CollectJob} 采集作业
     */
    @Transactional
    public void syncTableColumns(CollectJob collectJob)
    {
        logger.info("开始同步作业 [{}] 的表字段信息", collectJob.getJobName());

        String sourceTable = collectJob.getSourceTable();
        String targetTable = collectJob.getTargetTable();

        try {

            // 从数据源获取表字段信息
            List<TableMetadata> columns = getSourceTableColumns(collectJob);

            if (columns.isEmpty()) {
                logger.warn("无法获取源表 [{}] 的字段信息", sourceTable);
                return;
            }

            // 处理每个字段，并保存到 table_column 表
            for (TableMetadata column: columns) {
                TableColumn tableColumn = new TableColumn();
                tableColumn.setCollectId(collectJob.getId());
                tableColumn.setColumnName(column.getColumnName());
                tableColumn.setColumnType(column.getColumnTypeName());
                tableColumn.setColumnPosition(column.getColumnPosition());
                tableColumn.setTargetColumnName(column.getColumnName());
                tableColumn.setTargetColumnType(fieldMappingService.getTargetTypeBySourceType(column.getColumnTypeName()));
                tableColumnRepository.save(tableColumn);
            }

            logger.info("成功同步任务 [{}] 的表字段信息，源表: {}, 目标表: {}", collectJob.getId(), sourceTable, targetTable);
        }
        catch (Exception e) {
            logger.error("同步任务 [{}] 表字段时出错", collectJob.getId(), e);
        }

        logger.info("完成作业 [{}] 的表字段同步", collectJob.getJobName());
    }

    /**
     * 获取源表的字段信息
     *
     * @param job 采集任务
     * @return 字段信息列表
     */
    private List<TableMetadata> getSourceTableColumns(CollectJob job)
    {
        // 这里需要根据实际情况实现从数据源获取表字段信息的逻辑
        // 可能需要使用JDBC或其他方式连接到源数据库，执行元数据查询

        DataSource dataSource = dataSourceRepository.findById(job.getSourceId()).orElseThrow();
        try (Connection connection = DbUtil.getConnect(dataSource.getConnectionUrl(), dataSource.getUsername(), dataSource.getPass())
        ) {
            Statement sts = connection.createStatement();
            // 获取源表字段信息
            String sql = String.format("select * from %s.%s where 1 = 2", job.getSourceSchema(), job.getSourceTable());
            ResultSet resultSet = sts.executeQuery(sql);
            ResultSetMetaData metaData = resultSet.getMetaData();
            List<TableMetadata> columns = new ArrayList<>();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                TableMetadata column = TableMetadata
                        .builder()
                        .columnName(metaData.getColumnName(i))
                        .columnLabel(metaData.getColumnLabel(i))
                        .columnPosition(i)
                        .columnTypeName(metaData.getColumnTypeName(i))
                        .columnType(metaData.getColumnType(i))
                        .remarks(metaData.getColumnLabel(i))
                        .scale(metaData.getScale(i))
                        .precision(metaData.getPrecision(i))
                        .build();
                columns.add(column);
            }
            return columns;
        }
        catch (Exception e) {
            logger.error("获取源表字段信息时出错", e);
            return new ArrayList<>();
        }
    }

    /**
     * 根据映射规则转换列类型
     *
     * @param sourceColumnType 源列类型
     * @param mappingRule 映射规则
     * @return 目标列类型
     */
    private String mapColumnType(String sourceColumnType, String mappingRule)
    {
        // 根据映射规则转换列类型
        // 映射规则可能是JSON格式或其他格式，需要解析并应用规则

        // TODO: 实现映射规则解析和应用

        // 示例实现，简单返回原始类型
        // 实际项目中需要根据规则进行转换
        if (mappingRule == null || mappingRule.isEmpty()) {
            return sourceColumnType;
        }

        // 简单示例：假设映射规则是以逗号分隔的"源类型:目标类型"对
        String[] rules = mappingRule.split(",");
        for (String rule : rules) {
            String[] parts = rule.trim().split(":");
            if (parts.length == 2 && parts[0].equalsIgnoreCase(sourceColumnType)) {
                return parts[1];
            }
        }

        return sourceColumnType;
    }
}