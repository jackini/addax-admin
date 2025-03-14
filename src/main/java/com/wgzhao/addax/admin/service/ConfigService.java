package com.wgzhao.addax.admin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ConfigService {
    
    private final JdbcTemplate jdbcTemplate;
    
    @Autowired
    public ConfigService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    /**
     * 获取配置值
     * @param group 配置组
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public String getConfigValue(String group, String key, String defaultValue) {
        String sql = "SELECT config_value FROM system_config WHERE config_group = ? AND config_key = ?";
        
        try {
            return jdbcTemplate.queryForObject(sql, String.class, group, key);
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * 获取布尔类型配置值
     * @param group 配置组
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 布尔配置值
     */
    public boolean getBooleanValue(String group, String key, boolean defaultValue) {
        String value = getConfigValue(group, key, String.valueOf(defaultValue));
        return "true".equalsIgnoreCase(value) || "1".equals(value) || "yes".equalsIgnoreCase(value);
    }
    
    /**
     * 获取整数类型配置值
     * @param group 配置组
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 整数配置值
     */
    public int getIntValue(String group, String key, int defaultValue) {
        String value = getConfigValue(group, key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}