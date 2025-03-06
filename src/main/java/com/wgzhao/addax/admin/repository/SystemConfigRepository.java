package com.wgzhao.addax.admin.repository;

import com.wgzhao.addax.admin.model.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {
    SystemConfig findByConfigGroupAndConfigKey(String configGroup, String configKey);
}
