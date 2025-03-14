package com.wgzhao.addax.admin.service;

import com.wgzhao.addax.admin.model.DataSource;
import com.wgzhao.addax.admin.repository.DataSourceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class DataSourceService
{
    @Autowired
    private DataSourceRepository dataSourceRepository;

    /**
     * 创建数据源
     * @param dataSource 数据源对象
     * @return 创建的数据源
     */
    @Transactional
    public DataSource createDataSource(DataSource dataSource) {
        log.info("创建数据源: {}", dataSource.getName());
        return dataSourceRepository.save(dataSource);
    }

    /**
     * 获取所有数据源
     * @return 数据源列表
     */
    public List<DataSource> getAllDataSources() {
        log.info("获取所有数据源");
        return dataSourceRepository.findAll();
    }

    /**
     * 根据ID获取数据源
     * @param id 数据源ID
     * @return 数据源对象
     */
    public Optional<DataSource> getDataSourceById(Integer id) {
        log.info("获取数据源，ID: {}", id);
        return dataSourceRepository.findById(id);
    }

    /**
     * 更新数据源
     * @param dataSource 数据源对象
     * @return 更新后的数据源
     */
    @Transactional
    public Optional<DataSource> updateDataSource(DataSource dataSource) {
        log.info("更新数据源，ID: {}", dataSource.getId());
        if (!dataSourceRepository.existsById(dataSource.getId())) {
            return Optional.empty();
        }
        return Optional.of(dataSourceRepository.save(dataSource));
    }

    /**
     * 删除数据源
     * @param id 数据源ID
     * @return 是否删除成功
     */
    @Transactional
    public boolean deleteDataSource(Integer id) {
        log.info("删除数据源，ID: {}", id);
        if (!dataSourceRepository.existsById(id)) {
            return false;
        }
        dataSourceRepository.deleteById(id);
        return true;
    }

    /**
     * 测试数据源连接
     * @param id 数据源ID
     * @return 是否连接成功
     */
    public boolean testConnection(Integer id) {
        log.info("测试数据源连接，ID: {}", id);
        Optional<DataSource> dataSourceOpt = dataSourceRepository.findById(id);
        
        if (!dataSourceOpt.isPresent()) {
            log.error("数据源不存在，ID: {}", id);
            return false;
        }
        
        DataSource dataSource = dataSourceOpt.get();
        Connection conn = null;
        
        try {
            // 加载驱动
            if (dataSource.getDriverClass() != null && !dataSource.getDriverClass().isEmpty()) {
                Class.forName(dataSource.getDriverClass());
            }
            
            // 建立连接
            conn = DriverManager.getConnection(
                    dataSource.getConnectionUrl(),
                    dataSource.getUsername(),
                    dataSource.getPass()
            );
            
            log.info("数据源连接成功，ID: {}", id);
            return true;
        } catch (ClassNotFoundException e) {
            log.error("驱动类加载失败: {}", dataSource.getDriverClass(), e);
            return false;
        } catch (SQLException e) {
            log.error("数据库连接失败: {}", dataSource.getConnectionUrl(), e);
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.error("关闭数据库连接失败", e);
                }
            }
        }
    }

    /**
     * 根据数据库类型获取数据源
     * @param type 数据库类型
     * @return 数据源列表
     */
    public List<DataSource> getDataSourcesByType(String type) {
        log.info("获取类型为 {} 的数据源", type);
        return dataSourceRepository.findByDbType(type);
    }

    /**
     * 获取数据源的表列表
     * @param id 数据源ID
     * @param schema 模式名（可选）
     * @return 表列表
     */
    public List<Map<String, Object>> getDataSourceTables(Integer id, String schema) throws SQLException {
        log.info("获取数据源的表列表，ID: {}, Schema: {}", id, schema);
        Optional<DataSource> dataSourceOpt = dataSourceRepository.findById(id);
        
        if (!dataSourceOpt.isPresent()) {
            log.error("数据源不存在，ID: {}", id);
            throw new IllegalArgumentException("数据源不存在");
        }
        
        DataSource dataSource = dataSourceOpt.get();
        List<Map<String, Object>> tables = new ArrayList<>();
        Connection conn = null;
        ResultSet rs = null;
        
        try {
            // 加载驱动
            if (dataSource.getDriverClass() != null && !dataSource.getDriverClass().isEmpty()) {
                Class.forName(dataSource.getDriverClass());
            }
            
            // 建立连接
            conn = DriverManager.getConnection(
                    dataSource.getConnectionUrl(),
                    dataSource.getUsername(),
                    dataSource.getPass()
            );
            
            DatabaseMetaData metaData = conn.getMetaData();
            
            // 获取表列表
            rs = metaData.getTables(
                    null,                   // catalog
                    schema,                 // schema
                    null,                   // tableName
                    new String[]{"TABLE"}   // types
            );
            
            while (rs.next()) {
                Map<String, Object> table = new HashMap<>();
                table.put("tableName", rs.getString("TABLE_NAME"));
                table.put("tableType", rs.getString("TABLE_TYPE"));
                table.put("remarks", rs.getString("REMARKS"));
                
                if (schema == null || schema.isEmpty()) {
                    table.put("schema", rs.getString("TABLE_SCHEM"));
                } else {
                    table.put("schema", schema);
                }
                
                tables.add(table);
            }
            
            log.info("获取到 {} 张表", tables.size());
            return tables;
        } catch (ClassNotFoundException e) {
            log.error("驱动类加载失败: {}", dataSource.getDriverClass(), e);
            throw new SQLException("驱动类加载失败: " + e.getMessage());
        } catch (SQLException e) {
            log.error("获取表列表失败", e);
            throw e;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    log.error("关闭ResultSet失败", e);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.error("关闭数据库连接失败", e);
                }
            }
        }
    }

    /**
     * 获取表的列信息
     * @param id 数据源ID
     * @param schema 模式名（可选）
     * @param table 表名
     * @return 列信息列表
     */
    public List<Map<String, Object>> getTableColumns(Integer id, String schema, String table) throws SQLException {
        log.info("获取表的列信息，数据源ID: {}, Schema: {}, 表名: {}", id, schema, table);
        Optional<DataSource> dataSourceOpt = dataSourceRepository.findById(id);
        
        if (!dataSourceOpt.isPresent()) {
            log.error("数据源不存在，ID: {}", id);
            throw new IllegalArgumentException("数据源不存在");
        }
        
        DataSource dataSource = dataSourceOpt.get();
        List<Map<String, Object>> columns = new ArrayList<>();
        Connection conn = null;
        ResultSet rs = null;
        
        try {
            // 加载驱动
            if (dataSource.getDriverClass() != null && !dataSource.getDriverClass().isEmpty()) {
                Class.forName(dataSource.getDriverClass());
            }
            
            // 建立连接
            conn = DriverManager.getConnection(
                    dataSource.getConnectionUrl(),
                    dataSource.getUsername(),
                    dataSource.getPass()
            );
            
            DatabaseMetaData metaData = conn.getMetaData();
            
            // 获取列信息
            rs = metaData.getColumns(
                    null,    // catalog
                    schema,  // schema
                    table,   // tableName
                    null     // columnNamePattern
            );
            
            while (rs.next()) {
                Map<String, Object> column = new HashMap<>();
                column.put("columnName", rs.getString("COLUMN_NAME"));
                column.put("dataType", rs.getInt("DATA_TYPE"));
                column.put("typeName", rs.getString("TYPE_NAME"));
                column.put("columnSize", rs.getInt("COLUMN_SIZE"));
                column.put("decimalDigits", rs.getInt("DECIMAL_DIGITS"));
                column.put("nullable", rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                column.put("remarks", rs.getString("REMARKS"));
                column.put("defaultValue", rs.getString("COLUMN_DEF"));
                
                columns.add(column);
            }
            
            // 获取主键信息
            rs = metaData.getPrimaryKeys(null, schema, table);
            List<String> primaryKeys = new ArrayList<>();
            
            while (rs.next()) {
                primaryKeys.add(rs.getString("COLUMN_NAME"));
            }
            
            // 标记主键列
            for (Map<String, Object> column : columns) {
                column.put("isPrimaryKey", primaryKeys.contains(column.get("columnName")));
            }
            
            log.info("获取到 {} 个列", columns.size());
            return columns;
        } catch (ClassNotFoundException e) {
            log.error("驱动类加载失败: {}", dataSource.getDriverClass(), e);
            throw new SQLException("驱动类加载失败: " + e.getMessage());
        } catch (SQLException e) {
            log.error("获取列信息失败", e);
            throw e;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    log.error("关闭ResultSet失败", e);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.error("关闭数据库连接失败", e);
                }
            }
        }
    }
}
