package com.wgzhao.addax.admin.model;

import lombok.Data;
import lombok.Builder;

/**
 * 数据库表元数据信息类
 * 封装从 ResultSetMetaData 获取的表字段元数据
 */
@Data
@Builder
public class TableMetadata {
    // 列名
    private String columnName;

    // 列标签
    private String columnLabel;

    // 列索引位置
    private int columnPosition;

    // 列类型（数据库类型名称，如VARCHAR、INTEGER等）
    private String columnTypeName;

    // 列类型代码（java.sql.Types的常量）
    private int columnType;

    // 列的大小/长度
    private int scale;

    // 小数位数（对于数值类型）
    private int precision;

    // 列注释/备注
    private String remarks;

    // 列是否可为空
    private boolean nullable;

    // 列是否自增
    private boolean autoIncrement;

    // 表的目录名
    private String catalogName;

    // 表的模式名
    private String schemaName;

    // 表名
    private String tableName;

    // 列的类名（Java类型）
    private String className;
}