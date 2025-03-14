package com.wgzhao.addax.admin.controller;

import com.wgzhao.addax.admin.model.DataSource;
import com.wgzhao.addax.admin.service.DataSourceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/datasource")
@Slf4j
@AllArgsConstructor
public class DataSourceController
{
    private DataSourceService dataSourceService;

    /**
     * 创建数据源
     * @param dataSource 数据源对象
     * @return 创建的数据源
     */
    @PostMapping
    public ResponseEntity<DataSource> createDataSource(@RequestBody DataSource dataSource) {
        if (dataSource == null) {
            return ResponseEntity.badRequest().build();
        }
        log.info("创建数据源: {}", dataSource.getName());
        DataSource createdDataSource = dataSourceService.createDataSource(dataSource);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDataSource);
    }
    
    /**
     * 获取所有数据源
     * @return 数据源列表
     */
    @GetMapping
    public ResponseEntity<List<DataSource>> getAllDataSources() {
        log.info("获取所有数据源");
        List<DataSource> dataSources = dataSourceService.getAllDataSources();
        return ResponseEntity.ok(dataSources);
    }
    
    /**
     * 根据ID获取数据源
     * @param id 数据源ID
     * @return 数据源对象
     */
    @GetMapping("/{id}")
    public ResponseEntity<DataSource> getDataSourceById(@PathVariable Integer id) {
        log.info("获取数据源，ID: {}", id);
        Optional<DataSource> dataSource = dataSourceService.getDataSourceById(id);
        return dataSource.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 更新数据源
     * @param id 数据源ID
     * @param dataSource 更新的数据源对象
     * @return 更新后的数据源
     */
    @PutMapping("/{id}")
    public ResponseEntity<DataSource> updateDataSource(@PathVariable Integer id, 
                                                      @RequestBody DataSource dataSource) {
        if (dataSource == null) {
            return ResponseEntity.badRequest().build();
        }
        
        log.info("更新数据源，ID: {}", id);
        dataSource.setId(id);
        Optional<DataSource> updatedDataSource = dataSourceService.updateDataSource(dataSource);
        
        return updatedDataSource.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 删除数据源
     * @param id 数据源ID
     * @return 无内容响应
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDataSource(@PathVariable Integer id) {
        log.info("删除数据源，ID: {}", id);
        boolean deleted = dataSourceService.deleteDataSource(id);
        
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 测试数据源连接
     * @param id 数据源ID
     * @return 测试结果
     */
    @GetMapping("/{id}/test")
    public ResponseEntity<Map<String, Object>> testDataSourceConnection(@PathVariable Integer id) {
        log.info("测试数据源连接，ID: {}", id);
        Map<String, Object> result = new HashMap<>();
        
        try {
            boolean success = dataSourceService.testConnection(id);
            result.put("success", success);
            
            if (success) {
                result.put("message", "连接成功");
                return ResponseEntity.ok(result);
            } else {
                result.put("message", "连接失败");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }
        } catch (Exception e) {
            log.error("测试数据源连接时发生错误", e);
            result.put("success", false);
            result.put("message", "连接失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * 根据数据源类型获取数据源
     * @param type 数据源类型
     * @return 数据源列表
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<DataSource>> getDataSourcesByType(@PathVariable String type) {
        log.info("获取类型为 {} 的数据源", type);
        List<DataSource> dataSources = dataSourceService.getDataSourcesByType(type);
        return ResponseEntity.ok(dataSources);
    }
    
    /**
     * 获取数据源的表列表
     * @param id 数据源ID
     * @param schema 模式名（可选）
     * @return 表列表
     */
    @GetMapping("/{id}/tables")
    public ResponseEntity<List<Map<String, Object>>> getDataSourceTables(
            @PathVariable Integer id,
            @RequestParam(required = false) String schema) {
        
        log.info("获取数据源的表列表，ID: {}, Schema: {}", id, schema);
        try {
            List<Map<String, Object>> tables = dataSourceService.getDataSourceTables(id, schema);
            return ResponseEntity.ok(tables);
        } catch (Exception e) {
            log.error("获取数据源表列表时发生错误", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 获取表的列信息
     * @param id 数据源ID
     * @param schema 模式名（可选）
     * @param table 表名
     * @return 列信息列表
     */
    @GetMapping("/{id}/tables/{table}/columns")
    public ResponseEntity<List<Map<String, Object>>> getTableColumns(
            @PathVariable Integer id,
            @RequestParam(required = false) String schema,
            @PathVariable String table) {
        
        log.info("获取表的列信息，数据源ID: {}, Schema: {}, 表名: {}", id, schema, table);
        try {
            List<Map<String, Object>> columns = dataSourceService.getTableColumns(id, schema, table);
            return ResponseEntity.ok(columns);
        } catch (Exception e) {
            log.error("获取表列信息时发生错误", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
