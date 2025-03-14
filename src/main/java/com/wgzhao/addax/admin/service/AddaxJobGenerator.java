//package com.wgzhao.addax.admin.service;
//
//import com.fasterxml.jackson.databind.json.JsonMapper;
//import com.wgzhao.addax.admin.model.CollectTask;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import com.wgzhao.addax.admin.model.DataSource;
//import com.wgzhao.addax.admin.model.HdfsTemplate;
//import com.wgzhao.addax.admin.utils.DbUtil;
//import lombok.AllArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.util.Pair;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//@Slf4j
//@Service
//@AllArgsConstructor
//public class AddaxJobGenerator {
//
//    @Autowired
//    private SourceTableMetaService sourceTableMetaService;
//
//    @Autowired
//    private FieldMappingService fieldMappingService;
//
//    private static final ObjectMapper mapper = new ObjectMapper();
//
//
//    public Map<String, Object> generateJobConfig(CollectTask task) {
//        // Get necessary data
//        HdfsTemplate hdfsTemplate = task.getHdfsTemplate();
//        DataSource dataSource = task.getSource();
//        List<Pair<String, String>> columnInfo = sourceTableMetaService.getColumnInfo(task.getId());
//        String readerName = DbUtil.getReaderName(dataSource.getConnectionUrl());
//
//        // Create the job configuration using string templates
//        ObjectNode rootNode = mapper.createObjectNode();
//        try {
//            // Create a root JSON object
//            ObjectNode jobNode = rootNode.putObject("job");
//
//            // Setting section
//            ObjectNode settingNode = jobNode.putObject("setting");
//            ObjectNode speedNode = settingNode.putObject("speed");
//            speedNode.put("byte", -1);
//            speedNode.put("channel", 2);
//
//            // Content section
//            ObjectNode contentNode = jobNode.putObject("content");
//
//            // Reader section
//            ObjectNode readerNode = contentNode.putObject("reader");
//            readerNode.put("name", readerName);
//
//            ObjectNode readerParamNode = readerNode.putObject("parameter");
//            readerParamNode.put("username", dataSource.getUsername());
//            readerParamNode.put("password", dataSource.getPass());
//            readerParamNode.put("autoPk", "true");
//            readerParamNode.set("column", mapper.readTree(sourceColumn(columnInfo)));
//
//            ObjectNode connectionNode = readerParamNode.putObject("connection");
//            connectionNode.set("table", mapper.createArrayNode().add(task.getSourceTable()));
//            connectionNode.put("jdbcUrl", dataSource.getConnectionUrl());
//
//            // Writer section
//            ObjectNode writerNode = contentNode.putObject("writer");
//            writerNode.put("name", "hdfswriter");
//
//            ObjectNode writerParamNode = writerNode.putObject("parameter");
//            writerParamNode.put("defaultFS", hdfsTemplate.getDefaultFs());
//            writerParamNode.put("fileType", hdfsTemplate.getFileType());
//            writerParamNode.put("path", generateHdfsPath(hdfsTemplate.getBasePath(), task.getSourceTable()));
//            writerParamNode.put("fileName", task.getSourceTable());
//            writerParamNode.set("column", mapper.readTree(generateHdfsColumn(columnInfo)));
//            writerParamNode.put("writeMode", "overwrite");
//            writerParamNode.put("fieldDelimiter", "\u0001");
//            writerParamNode.put("compress", "SNAPPY");
//            writerParamNode.put("hdfsSitePath", hdfsTemplate.getConfigPath());
//            writerParamNode.set("hadoopConfig", mapper.valueToTree(hdfsTemplate.getProperties()));
//
//        } catch (Exception e) {
//            log.error("Error generating job config", e);
//        }
//        finally {
//            // convert the rootNode to Map object
//            return mapper.convertValue(rootNode, Map.class);
//        }
//    }
//
//    private String generateHdfsPath(String basePath, String tableName) {
//        // basePath/table/logdate='yyyy-MM-dd'
//        LocalDate date =  LocalDate.now();
//        return String.format("%s/%s/logdate='%s'", basePath, tableName, date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
//    }
//    // Helper method to convert columns to JSON array
//    private String sourceColumn(List<Pair<String, String>> columns) {
//        List<String> column = new ArrayList<>();
//        for (Pair<String, String> col : columns) {
//            column.add(col.getFirst());
//        }
//        return String.join(",", column);
//    }
//
//    /**
//     * Generate HDFS column configuration, like this
//     * [
//     *    {"name": "col1", "type": "string"},
//     *    {"name": "col2", "type": "int"}
//     * ]
//     * @param columns List of column name and type pairs
//     * @return JSON string
//     */
//    private String generateHdfsColumn(List<Pair<String, String>> columns)
//    {
//        try {
//            List<ObjectNode> columnNodes = new ArrayList<>();
//
//            for (Pair<String, String> column : columns) {
//                ObjectNode node = mapper.createObjectNode();
//                node.put("name", column.getFirst());
//                // Convert database type to HDFS type
//                node.put("type", fieldMappingService.getTargetTypeBySourceType(column.getSecond()));
//                columnNodes.add(node);
//            }
//
//            return mapper.writeValueAsString(columnNodes);
//        } catch (Exception e) {
//            log.error("Error generating HDFS column config", e);
//            return "[]";
//        }
//
//    }
//
//}