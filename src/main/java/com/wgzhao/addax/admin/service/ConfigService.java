package com.wgzhao.addax.admin.service;

import com.wgzhao.addax.admin.model.SystemConfig;
import com.wgzhao.addax.admin.repository.SystemConfigRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class ConfigService
{

    private final SystemConfigRepository configRepository;

    /**
     * 获取配置值
     *
     * @param group 配置组
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public String getConfigValue(String group, String key, String defaultValue)
    {

        return configRepository.findConfigValueByConfigGroupAndConfigKey(group, key)
                .map(SystemConfig::getConfigValue).orElse(defaultValue);
    }

    /**
     * 获取布尔类型配置值
     *
     * @param group 配置组
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 布尔配置值
     */
    public boolean getBooleanValue(String group, String key, boolean defaultValue)
    {
        String value = getConfigValue(group, key, String.valueOf(defaultValue));
        return "true".equalsIgnoreCase(value) || "1".equals(value) || "yes".equalsIgnoreCase(value);
    }

    /**
     * 获取整数类型配置值
     *
     * @param group 配置组
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 整数配置值
     */
    public int getIntValue(String group, String key, int defaultValue)
    {
        String value = getConfigValue(group, key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}