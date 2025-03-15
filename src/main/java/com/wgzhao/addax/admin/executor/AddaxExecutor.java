package com.wgzhao.addax.admin.executor;

import com.wgzhao.addax.admin.model.CollectJob;
import com.wgzhao.addax.admin.model.JobExecution;
import com.wgzhao.addax.admin.repository.JobRepository;
import com.wgzhao.addax.admin.service.ConfigService;
import com.wgzhao.addax.admin.service.JobExecutionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
@AllArgsConstructor
public class AddaxExecutor
{
    private final ConfigService configService;
    private final JobRepository jobRepository;
    private final JobExecutionService jobExecutionService;

    @Async
    public void executeAddax(JobExecution task, CollectJob job)
    {

        log.info("执行作业: {}", task.getJobId());
        String json = task.getAddaxJson();
        log.info("执行作业: {}", json);

        String status = "SUCCESS";
        String flag = "Y";

        // write to temp file
        Path path = null;
        try {
            path = Files.createFile(Path.of("/tmp/addax-" + task.getId() + ".json"));
            Files.writeString(path, json);

            String addaxHome = configService.getConfigValue("addax", "home", "/opt/app/addax");

            // check if addax home exists
            if (!Files.exists(Path.of(addaxHome))) {
                log.error("Addax home not found: {}", addaxHome);
                status = "FAIL";
                flag = "E";
            }
            else {
                ProcessBuilder pb = new ProcessBuilder(addaxHome + "/bin/addax.sh", path.toString());
                pb.redirectErrorStream(true);
                Process process = pb.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("Addax output: {}", line);
                }

                int exitCode = process.waitFor();

                if (exitCode != 0) {
                    log.error("Addax execution failed with exit code: {}", exitCode);
                    status = "FAIL";
                    flag = "E";
                }
            }
        }
        catch (Exception e) {
            log.error("Failed to execute addax", e);
            status = "FAIL";
            flag = "E";
        }
        finally {

            // delete temp file
            if (path != null) {
                try {
                    Files.deleteIfExists(path);
                }
                catch (Exception e) {
                    log.error("Failed to delete temp file", e);
                }
            }
            // 更新任务状态
            jobExecutionService.completeExecution(task, status, null);

            // 更新作业状态
            job.setJobStatus(flag);

            jobRepository.save(job);
        }
    }
}
