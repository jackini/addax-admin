package com.wgzhao.addax.admin.service;

import com.wgzhao.addax.admin.repository.SystemConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SystemConfigService {
    @Autowired
    private SystemConfigRepository systemConfigRepository;

    public String getAddaxHomePath() {
        return systemConfigRepository.findByConfigGroupAndConfigKey("addax", "home").getConfigValue();
    }
}
