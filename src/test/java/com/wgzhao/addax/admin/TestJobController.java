package com.wgzhao.addax.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wgzhao.addax.admin.config.SecurityConfiguration;
import com.wgzhao.addax.admin.controller.JobController;
import com.wgzhao.addax.admin.model.CollectJob;
import com.wgzhao.addax.admin.service.JobService;
import com.wgzhao.addax.admin.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(JobController.class)
@Import({JobService.class, SecurityConfiguration.class, JwtService.class})
public class TestJobController
{

    @Autowired
    private MockMvc mockMvc;

    private final JobService jobService = mock(JobService.class);

//    @MockBean
//    private JobService jobService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetAllJobs()
            throws Exception
    {
        // 准备测试数据
        CollectJob job1 = new CollectJob();
        job1.setId(1L);
        job1.setJobName("测试作业1");

        CollectJob job2 = new CollectJob();
        job2.setId(2L);
        job2.setJobName("测试作业2");

        List<CollectJob> jobs = Arrays.asList(job1, job2);

        // 设置模拟行为
        when(jobService.getAllJobs()).thenReturn(jobs);

        // 执行请求并验证结果
        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].jobName").value("测试作业1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].jobName").value("测试作业2"));
    }

    @Test
    public void testgetJob()
            throws Exception
    {
        // 准备测试数据
        CollectJob job = new CollectJob();
        job.setId(1L);
        job.setJobName("测试作业");

        // 设置模拟行为
        when(jobService.getJob(1L)).thenReturn(Optional.of(job));

        // 执行请求并验证结果
        mockMvc.perform(get("/api/jobs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.jobName").value("测试作业"));

        // 测试不存在的作业ID
        when(jobService.getJob(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/jobs/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateJob()
            throws Exception
    {
        // 准备测试数据
        CollectJob job = new CollectJob();
        job.setJobName("新作业");
        job.setSourceId(1);
        job.setSourceSchema("test");
        job.setSourceTable("t");
        job.setTargetSchema("odstest");
        job.setTargetTable("t");
        job.setSourceTemplateId(1);
        job.setHdfsTemplateId(1);
        job.setJobStatus("N");
        job.setCronExpression("0/5 * * * * ?");

        CollectJob savedJob = new CollectJob();
        savedJob.setId(1L);
        savedJob.setJobName("新作业");

        // 设置模拟行为
        when(jobService.createJob(any(CollectJob.class))).thenReturn(savedJob);

        // 执行请求并验证结果
        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(job)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.jobName").value("新作业"));
    }

//    @Test
//    public void testUpdateJob() throws Exception {
//        // 准备测试数据
//        CollectJob job = new CollectJob();
//        job.setId(1L);
//        job.setJobName("更新后的作业");
//
//        // 设置模拟行为
//        when(jobService.updateJob(anyLong(), any(CollectJob.class))).thenReturn(Optional.of(job));
//
//        // 执行请求并验证结果
//        mockMvc.perform(put("/api/jobs/1")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(job)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value(1))
//                .andExpect(jsonPath("$.jobName").value("更新后的作业"));
//
//        // 测试不存在的作业ID
//        when(jobService.updateJob(anyLong(), any(CollectJob.class))).thenReturn(Optional.empty());
//
//        mockMvc.perform(put("/api/jobs/999")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(job)))
//                .andExpect(status().isNotFound());
//    }

//    @Test
//    public void testDeleteJob() throws Exception {
//        // 设置模拟行为
//        when(jobService.deleteJob(1L)).thenReturn(true);
//        when(jobService.deleteJob(999L)).thenReturn(false);
//
//        // 执行请求并验证结果
//        mockMvc.perform(delete("/api/jobs/1"))
//                .andExpect(status().isNoContent());
//
//        // 测试不存在的作业ID
//        mockMvc.perform(delete("/api/jobs/999"))
//                .andExpect(status().isNotFound());
//    }

    @Test
    public void testSyncTableColumns()
            throws Exception
    {
        // 准备测试数据
        CollectJob job = new CollectJob();
        job.setId(1L);

        // 设置模拟行为
        when(jobService.getJob(1L)).thenReturn(Optional.of(job));
        doNothing().when(jobService).syncTableColumns(any(CollectJob.class));

        // 执行请求并验证结果
        mockMvc.perform(post("/api/jobs/1/sync-columns"))
                .andExpect(status().isOk());

        // 测试不存在的作业ID
        when(jobService.getJob(999L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/jobs/999/sync-columns"))
                .andExpect(status().isNotFound());
    }
}