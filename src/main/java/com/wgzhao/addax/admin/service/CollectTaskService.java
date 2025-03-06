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
    public List<CollectTask> getCollectTasks() {
        return collectTaskRepository.findByTaskStatus("N");
    }
}
