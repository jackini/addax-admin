package com.wgzhao.addax.admin.service;

import com.wgzhao.addax.admin.model.CollectTask;
import com.wgzhao.addax.admin.repository.CollectTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CollectTaskService {
    @Autowired
    private CollectTaskRepository collectTaskRepository;


    // 获取所有需要采集的任务
    // task_status = 'N'
    public List<CollectTask> getNeedCollectTask() {
        return collectTaskRepository.findByTaskStatus("N");
    }

    public CollectTask getCollectTask(long collectId) {
        return collectTaskRepository.findById(collectId).orElse(null);
    }

    public void save(CollectTask task)
    {
        collectTaskRepository.save(task);
    }
}
