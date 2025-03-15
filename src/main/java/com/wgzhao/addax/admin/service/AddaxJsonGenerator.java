package com.wgzhao.addax.admin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wgzhao.addax.admin.model.CollectJob;
import com.wgzhao.addax.admin.model.DataSource;
import com.wgzhao.addax.admin.model.HdfsTemplate;
import com.wgzhao.addax.admin.model.SourceTemplate;
import com.wgzhao.addax.admin.model.TableColumn;
import com.wgzhao.addax.admin.repository.DataSourceRepository;
import com.wgzhao.addax.admin.repository.HdfsTemplateRepository;
import com.wgzhao.addax.admin.repository.SourceTemplateRepository;
import com.wgzhao.addax.admin.repository.TableColumnRepository;
import com.wgzhao.addax.admin.utils.DbUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Addax JSON 生成器
 * 用于生成 Addax 任务定义的 JSON 配置
 */
@Service
@Slf4j
@AllArgsConstructor
public class AddaxJsonGenerator {

    private final DataSourceRepository dataSourceRepository;
    private final HdfsTemplateRepository hdfsTemplateRepository;
    private final SourceTemplateRepository sourceTemplateRepository;
    private final ObjectMapper objectMapper;
//    private final SourceTableMetaService sourceTableMetaService;
    private final FieldMappingService fieldMappingService;
    private final TableColumnRepository tableColumnRepository;

    /**
     * 生成 Addax 任务定义 JSON
     * @param job 采集作业
     * @return Addax 任务定义 JSON 字符串
     */
    public String generateAddaxJson(CollectJob job) {
        try {
            log.info("开始为作业 {} 生成Addax JSON配置", job.getId());
            
            // 获取相关数据
            Optional<DataSource> dataSourceOpt = dataSourceRepository.findById(job.getSourceId());
            Optional<HdfsTemplate> hdfsTemplateOpt = hdfsTemplateRepository.findById(job.getHdfsTemplateId());
            Optional<SourceTemplate> sourceTemplateOpt = sourceTemplateRepository.findById(job.getSourceTemplateId());

            if (!dataSourceOpt.isPresent() || !hdfsTemplateOpt.isPresent() || !sourceTemplateOpt.isPresent()) {
                log.error("生成Addax JSON失败：数据源或模板不存在，作业ID: {}", job.getId());
                return null;
            }

            DataSource dataSource = dataSourceOpt.get();
            HdfsTemplate hdfsTemplate = hdfsTemplateOpt.get();
            SourceTemplate sourceTemplate = sourceTemplateOpt.get();

            // 创建 JSON 结构
            ObjectNode rootNode = objectMapper.createObjectNode();
            
            // 设置作业配置
            ObjectNode jobNode = rootNode.putObject("job");
            jobNode.set("setting", createSettingNode());
            
            // 设置内容
            ArrayNode contentArray = jobNode.putArray("content");
            contentArray.add(createContentNode(job, dataSource, hdfsTemplate, sourceTemplate));

            String jsonConfig = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
            log.info("成功生成作业 {} 的Addax JSON配置", job.getId());
            
            return jsonConfig;
        } catch (Exception e) {
            log.error("生成Addax JSON时发生错误，作业ID: {}", job.getId(), e);
            return null;
        }
    }

    /**
     * 创建设置节点
     * @return 设置节点
     */
    private ObjectNode createSettingNode() {
        // 这是一个空方法，后续实现
        // 目前返回一个基本的设置节点
        ObjectNode settingNode = objectMapper.createObjectNode();
        
        // 设置速度限制
        ObjectNode speedNode = settingNode.putObject("speed");
        speedNode.put("channel", 3); // 默认并发度
        
        // 设置错误限制
        ObjectNode errorLimitNode = settingNode.putObject("errorLimit");
        errorLimitNode.put("record", 0);
        errorLimitNode.put("percentage", 0.02);
        
        return settingNode;
    }

    /**
     * 创建内容节点
     * @param job 采集作业
     * @param dataSource 数据源
     * @param hdfsTemplate HDFS模板
     * @param sourceTemplate 源表读取模板
     * @return 内容节点
     */
    private ObjectNode createContentNode(CollectJob job, DataSource dataSource, 
                                        HdfsTemplate hdfsTemplate, SourceTemplate sourceTemplate) {
        // 这是一个空方法，后续实现
        // 目前返回一个基本的内容节点
        ObjectNode contentNode = objectMapper.createObjectNode();

        List<TableColumn> columnInfo = tableColumnRepository.findByCollectId(job.getId());

        ObjectMapper mapper = new ObjectMapper();

        //contentNode.put("reader", "mysqlreader");
        //contentNode.put("writer", "hdfswriter");
        // Reader section
        String readerName = DbUtil.getReaderName(dataSource.getConnectionUrl());
        ObjectNode readerNode = contentNode.putObject("reader");
        readerNode.put("name", readerName);

        ObjectNode readerParamNode = readerNode.putObject("parameter");
        readerParamNode.put("username", dataSource.getUsername());
        readerParamNode.put("password", dataSource.getPass());
        readerParamNode.put("autoPk", "true");
        ArrayNode column = readerParamNode.putArray("column");
        columnInfo.stream().map(TableColumn::getColumnName).forEach(column::add);

        ObjectNode connectionNode = readerParamNode.putObject("connection");
        connectionNode.set("table", mapper.createArrayNode().add(job.getSourceTable()));
        connectionNode.put("jdbcUrl", dataSource.getConnectionUrl());

        // Writer section
        ObjectNode writerNode = contentNode.putObject("writer");
        writerNode.put("name", "hdfswriter");

        ObjectNode writerParamNode = writerNode.putObject("parameter");
        writerParamNode.put("defaultFS", hdfsTemplate.getDefaultFs());
        writerParamNode.put("fileType", hdfsTemplate.getFileType());
        writerParamNode.put("path", generateHdfsPath(hdfsTemplate.getBasePath(), job.getSourceTable()));
        writerParamNode.put("fileName", job.getSourceTable());

        writerParamNode.set("column", createColumnArrayNode(columnInfo));

        writerParamNode.put("writeMode", "overwrite");
        writerParamNode.put("fieldDelimiter", "\u0001");
        writerParamNode.put("compress", "SNAPPY");
        writerParamNode.put("hdfsSitePath", hdfsTemplate.getConfigPath());
        writerParamNode.set("hadoopConfig", mapper.valueToTree(hdfsTemplate.getProperties()));

        contentNode.put("reader", readerNode);
        contentNode.put("writer", writerNode);
        return contentNode;
    }


    private ArrayNode createColumnArrayNode(List<TableColumn> columns) {
        ArrayNode columnArray = objectMapper.createArrayNode();

        columns.forEach(column -> {
            ObjectNode columnNode = objectMapper.createObjectNode();
            columnNode.put("name", column.getColumnName());
            columnNode.put("type", fieldMappingService.getTargetTypeBySourceType(column.getColumnType()));
            columnArray.add(columnNode);
        });

        return columnArray;
    }

    private String generateHdfsPath(String basePath, String tableName) {
        // basePath/table/logdate='yyyy-MM-dd'
        LocalDate date =  LocalDate.now();
        return String.format("%s/%s/logdate='%s'", basePath, tableName, date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }
    // Helper method to convert columns to JSON array
    private String sourceColumn(List<Pair<String, String>> columns) {
        List<String> column = new ArrayList<>();
        for (Pair<String, String> col : columns) {
            column.add(col.getFirst());
        }
        return String.join(",", column);
    }

    /**
     * Generate HDFS column configuration, like this
     * [
     *    {"name": "col1", "type": "string"},
     *    {"name": "col2", "type": "int"}
     * ]
     * @param columns List of column name and type pairs
     * @return JSON string
     */
    private String generateHdfsColumn(List<Pair<String, String>> columns)
    {
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<ObjectNode> columnNodes = new ArrayList<>();

            for (Pair<String, String> column : columns) {
                ObjectNode node = mapper.createObjectNode();
                node.put("name", column.getFirst());
                // Convert database type to HDFS type
                node.put("type", fieldMappingService.getTargetTypeBySourceType(column.getSecond()));
                columnNodes.add(node);
            }

            return mapper.writeValueAsString(columnNodes);
        } catch (Exception e) {
            log.error("Error generating HDFS column config", e);
            return "[]";
        }

    }

}