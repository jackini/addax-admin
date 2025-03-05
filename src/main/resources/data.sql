
insert into users (USERNAME, PASSWORD, enabled)
values  ('user', '$2a$10$7pdkeQNqyfj/H.xSqniqDeVNCq8CnXTVNcoP0fgbzFkR53cDgF0z.', true),
        ('admin', '$2a$10$B/24QvXSICyz/qUAe9Va0OWmGKOBA./9HiJBvfHw2QDudDBsGZ43K', true);

INSERT INTO field_mapping_rule (SOURCE_TYPE, TARGET_TYPE, NOTE) VALUES('long', 'string', '字符类型');
INSERT INTO field_mapping_rule (SOURCE_TYPE, TARGET_TYPE, NOTE) VALUES('clob', 'string', '字符类型');
INSERT INTO field_mapping_rule (SOURCE_TYPE, TARGET_TYPE, NOTE) VALUES('character', 'string', '字符类型');
INSERT INTO field_mapping_rule (SOURCE_TYPE, TARGET_TYPE, NOTE) VALUES('text', 'string', '字符类型');
INSERT INTO field_mapping_rule (SOURCE_TYPE, TARGET_TYPE, NOTE) VALUES('datetime', 'string', '日期时间型');
INSERT INTO field_mapping_rule (SOURCE_TYPE, TARGET_TYPE, NOTE) VALUES('date', 'string', '日期时间型');
INSERT INTO field_mapping_rule (SOURCE_TYPE, TARGET_TYPE, NOTE) VALUES('timestamp', 'string', '日期时间型');
INSERT INTO field_mapping_rule (SOURCE_TYPE, TARGET_TYPE, NOTE) VALUES('number', 'decimal', '数值型');
INSERT INTO field_mapping_rule (SOURCE_TYPE, TARGET_TYPE, NOTE) VALUES('integer', 'bigint', '数值型');
INSERT INTO field_mapping_rule (SOURCE_TYPE, TARGET_TYPE, NOTE) VALUES('numeric', 'decimal', '数值型');
INSERT INTO field_mapping_rule (SOURCE_TYPE, TARGET_TYPE, NOTE) VALUES('decimal', 'decimal', '数值型');
INSERT INTO field_mapping_rule (SOURCE_TYPE, TARGET_TYPE, NOTE) VALUES('double', 'double', '数值型');
INSERT INTO field_mapping_rule (SOURCE_TYPE, TARGET_TYPE, NOTE) VALUES('int', 'bigint', '数值型');
INSERT INTO field_mapping_rule (SOURCE_TYPE, TARGET_TYPE, NOTE) VALUES('blob', 'string', '字符类型');
INSERT INTO field_mapping_rule (SOURCE_TYPE, TARGET_TYPE, NOTE) VALUES('raw', 'string', '字符类型');
INSERT INTO field_mapping_rule (SOURCE_TYPE, TARGET_TYPE, NOTE) VALUES('rowid', 'string', '字符类型');
INSERT INTO field_mapping_rule (SOURCE_TYPE, TARGET_TYPE, NOTE) VALUES('float', 'float', '数值型');
INSERT INTO field_mapping_rule (SOURCE_TYPE, TARGET_TYPE, NOTE) VALUES('nvarchar', 'string', '字符类型');
INSERT INTO field_mapping_rule (SOURCE_TYPE, TARGET_TYPE, NOTE) VALUES('bit', 'boolean', 'sqlserver的布尔型，jdbc连接时返回的true/false');
INSERT INTO field_mapping_rule (SOURCE_TYPE, TARGET_TYPE, NOTE) VALUES('image', 'binary', '2024-13-30');
INSERT INTO field_mapping_rule (SOURCE_TYPE, TARGET_TYPE, NOTE) VALUES('smalldatetime', 'string', '日期时间型');
INSERT INTO field_mapping_rule (SOURCE_TYPE, TARGET_TYPE, NOTE) VALUES('bigint', 'decimal', '数值型');
INSERT INTO field_mapping_rule (SOURCE_TYPE, TARGET_TYPE, NOTE) VALUES('longtext', 'string', '2024-13-30');
INSERT INTO field_mapping_rule (SOURCE_TYPE, TARGET_TYPE, NOTE) VALUES('nvarchar2', 'string', '字符类型');
INSERT INTO field_mapping_rule (SOURCE_TYPE, TARGET_TYPE, NOTE) VALUES('char', 'string', '字符类型');
INSERT INTO field_mapping_rule (SOURCE_TYPE, TARGET_TYPE, NOTE) VALUES('varchar', 'string', '字符类型');
INSERT INTO field_mapping_rule (SOURCE_TYPE, TARGET_TYPE, NOTE) VALUES('varchar2', 'string', '字符类型');
INSERT INTO field_mapping_rule (SOURCE_TYPE, TARGET_TYPE, NOTE) VALUES('tinyint', 'int', 'mysql的TINYINT');
INSERT INTO field_mapping_rule (SOURCE_TYPE, TARGET_TYPE, NOTE) VALUES('ntext', 'string', '2024-13-30');
INSERT INTO field_mapping_rule (SOURCE_TYPE, TARGET_TYPE, NOTE) VALUES('nchar', 'string', '2024-13-30');
INSERT INTO field_mapping_rule (SOURCE_TYPE, TARGET_TYPE, NOTE) VALUES('smallint', 'int', 'mysql的SMALLINT');
INSERT INTO field_mapping_rule (SOURCE_TYPE, TARGET_TYPE, NOTE) VALUES('string', 'string', '2024-13-30');
INSERT INTO field_mapping_rule (SOURCE_TYPE, TARGET_TYPE, NOTE) VALUES('time', 'string', '字符类型');
INSERT INTO field_mapping_rule (SOURCE_TYPE, TARGET_TYPE, NOTE) VALUES('money', 'decimal(19,4)', 'sqlserver的金额类型');