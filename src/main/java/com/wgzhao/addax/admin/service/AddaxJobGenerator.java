package com.wgzhao.addax.admin.service;

import com.wgzhao.addax.admin.model.CollectTask;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wgzhao.addax.admin.model.DataSource;
import com.wgzhao.addax.admin.model.HdfsTemplate;
import com.wgzhao.addax.admin.model.SourceTableMeta;
import com.wgzhao.addax.admin.utils.DbUtil;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AddaxJobGenerator {

    @Autowired
    private SourceTableMetaService sourceTableMetaService;

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * {
        "job": {
            "setting": {
            "speed": {
                "byte": -1,
                "channel": 2
            }
            },
            "content": {
            "reader": {
                "name": "mysqlreader",
                "parameter": {
                "username": "username",
                "password": "password",
                "autoPk":"true",
                "column": [
                    "col3","col2","col3","col4","col5","col6"
                ],
                "connection": {
                    "table": [ "tbl0"],
                    "jdbcUrl": "jdbc:mysql://localhost:3306/test"
                    }
                }
            },
            "writer": {
                "name": "hdfswriter",
                "parameter": {
                    "defaultFS": "hdfs://yytz",
                    "fileType": "$type",
                    "path": "/tmp/out_$type",
                    "fileName": "tbl0",
                    "column": [{"name": "col1","type": "string"}],
                    "writeMode": "overwrite",
                    "fieldDelimiter": "\u0001",
                    "compress": "SNAPPY",
                    "hdfsSitePath": "",
                    "hadoopConfig": {
                        "dfs.nameservices": "yytz",
                        "dfs.ha.namenodes.yytz": "nn1,nn2",
                        "dfs.namenode.rpc-address.yytz.nn1": "nn01.bigdata.gp51.com:8020",
                        "dfs.namenode.rpc-address.yytz.nn2": "nn02.bigdata.gp51.com:8020",
                        "dfs.client.failover.proxy.provider.yytz": "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider"
                    }
                }
            }
            }
        }
        }
     * @param task
     * @return
     */
    public String generateJobConfig(CollectTask task) {
        
        ObjectNode job = mapper.createObjectNode();
        ObjectNode settings = mapper.createObjectNode();
        ObjectNode reader = mapper.createObjectNode();
        ObjectNode writer = mapper.createObjectNode();
        // 获取 hdfs 模板
        HdfsTemplate hdfsTemplate = task.getHdfsTemplate();
        DataSource dataSource = task.getSource();
        // [col1, col2, col3, col4, col5, col6]
        List<String> column = sourceTableMetaService.getColumns(task.getId());
        
        settings.put("speed", mapper.createObjectNode()
                .put("byte", -1)
                .put("channel", 3));

        reader.put("name", DbUtil.getReaderName(dataSource.getConnectionUrl()))
               .putObject("parameter")
                   .put("username", dataSource.getUsername())
                   .put("password", task.getSource().getPass())
                   .put("where", task.getWhereCondition())
                   .put("autoPk", true)
                   .putArray("column")
                    .add(column)
                   .put("connection")
                       .putArray("table")
                       .add(task.getSourceSchema() + "." + task.getSourceTable())
                   .endObject()
              .endObject();
        writer.put("name", "hdfswriter")
              .putObject("parameter")
                  .put("path", "/data/" + task.getTargetSchema() + "/" + task.getTargetTable())
                  .put("fileType", "text")
                  .put("fileName", task.getTargetTable() + ".txt")
              .endObject();
        
        return job.toString();
    }
}