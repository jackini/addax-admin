-- 数据源管理表
CREATE TABLE data_source (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '数据源ID',
    name VARCHAR(100) NOT NULL COMMENT '数据源名称',
    db_type VARCHAR(50) NOT NULL COMMENT '数据库类型',
    connection_url VARCHAR(500) NOT NULL COMMENT '连接URL',
    username VARCHAR(100) COMMENT '用户名',
    pass VARCHAR(255) COMMENT '密码（加密存储）',
    driver_class VARCHAR(200) COMMENT 'JDBC驱动类',
    properties TEXT COMMENT '额外连接属性（JSON格式）',
    note VARCHAR(500) COMMENT '数据源描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_enable boolean NOT NULL DEFAULT true COMMENT '是否启用',
    start_time time null comment '采集启动时间，格式为 HH:mm:ss'
    UNIQUE KEY uk_name (name)
) COMMENT='数据源配置表';

-- 源表读取模板
CREATE TABLE source_template (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID',
    name VARCHAR(100) NOT NULL COMMENT '模板名称',
    fetch_size int not null default 10000 comment '每次读取的记录数',
    is_auto_pk boolean not null default false comment '是否自动查找主键主键'
);

-- HDFS配置模板表
CREATE TABLE hdfs_template (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID',
    name VARCHAR(100) NOT NULL COMMENT '模板名称',
    default_fs VARCHAR(255) NOT NULL COMMENT 'defaultFS配置，如hdfs://localhost:9000 或 hdfs://cluster',
    config_path VARCHAR(500) COMMENT 'hdfs-site.xml配置文件路径',
    hadoop_user VARCHAR(100) COMMENT 'Hadoop用户名',
    file_type VARCHAR(20) not null default 'orc' comment '文件类型：ORC, PARQUET, TEXT等',
    compress_type VARCHAR(50) not null default 'SNAPPY' COMMENT '压缩格式：SNAPPY, GZIP, LZ4, NONE等',
    base_path varchar(20) not null comment '保存在HDFS的路径前缀，比如 /ods/odstl，后面会拼接表名，分区等',
    enable_kerberos  boolean not null default false comment '是否启用Kerberos认证',
    kerberos_principal VARCHAR(255) COMMENT 'Kerberos主体',
    keytab_path VARCHAR(500) COMMENT 'Kerberos密钥文件路径',
    properties JSON COMMENT '其他Hadoop配置参数（JSON格式）',
    note VARCHAR(500) COMMENT '模板描述',
    is_enabled boolean NOT NULL DEFAULT true COMMENT '是否启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_name (name)
) COMMENT='HDFS配置模板表';

-- 采集任务表
CREATE TABLE collect_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    source_id BIGINT NOT NULL COMMENT '数据源ID',
    source_schema VARCHAR(100) COMMENT '源模式名（适用于Oracle等）',
    source_table VARCHAR(200) NOT NULL COMMENT '源表名',
    target_schema VARCHAR(100) COMMENT '目标数据库名，针对Hive',
    target_table VARCHAR(200) NOT NULL COMMENT '目标表名，默认情况下和源表明相同',
    source_template_id INT NOT NULL COMMENT '源表读取模板ID',
    hdfs_template_id INT NOT NULL COMMENT 'HDFS模板ID',
    where_condition varchar(200) not null default '1=1' COMMENT '过滤条件',
    task_status char(1) NOT NULL DEFAULT 1 COMMENT '任务状态：X-已禁用，R-正在运行, W-等待执行, E-失败, Y-已完成, N-未运行',
    timeout_secs INT DEFAULT 7200 COMMENT '超时时间（秒）',
    retry_times INT DEFAULT 3 COMMENT '重试次数',
    etl_freq char(1) not null default 'D' comment '采集频率: D-天',
    need_create_table char(2) not null default 'Yn' comment '是否自动创建目标表, Y-需要, N-不需要, y-已经创建, n-未创建',
    need_update_meta char(2) not null default 'Ny' comment '是否自动更新元数据, Y-需要, N-不需要, y-已经更新, n-未更新',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_task_name (source_id, source_schema, source_table),
    CONSTRAINT fk_task_source FOREIGN KEY (source_id) REFERENCES data_source(id),
    CONSTRAINT fk_task_hdfs FOREIGN KEY (hdfs_template_id) REFERENCES hdfs_template(id),
    CONSTRAINT fk_task_template FOREIGN KEY (source_template_id) REFERENCES source_template(id)
) COMMENT='采集任务表';

-- 任务执行记录表
CREATE TABLE task_execution (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '执行ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration INT COMMENT '执行时长（秒）',
    exec_status VARCHAR(20) NOT NULL COMMENT '执行状态：RUNNING, SUCCESS, FAILED, TIMEOUT',
    total_records BIGINT COMMENT '总处理记录数',
    success_records BIGINT COMMENT '成功记录数',
    failed_records BIGINT COMMENT '失败记录数',
    rejected_records BIGINT COMMENT '拒绝记录数',
    bytes_speed BIGINT COMMENT '传输速率（bytes/s）',
    records_speed BIGINT COMMENT '记录速率（records/s）',
    error_message TEXT COMMENT '错误信息',
    log_path VARCHAR(500) COMMENT 'Addax日志路径',
    execution_json JSON COMMENT '执行任务的JSON配置',
    trigger_type VARCHAR(20) COMMENT '触发类型：MANUAL, SCHEDULED',
    KEY idx_task_id (task_id),
    KEY idx_batch_id (batch_id),
    KEY idx_start_time (start_time),
    KEY idx_status (status),
    CONSTRAINT fk_execution_task FOREIGN KEY (task_id) REFERENCES collect_task(id)
) COMMENT='任务执行记录表';

-- 表结构变更风险表
CREATE TABLE schema_change_risk (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '风险ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    source_table VARCHAR(200) NOT NULL COMMENT '源表名',
    change_type VARCHAR(50) NOT NULL COMMENT '变更类型：ADD_COLUMN, DROP_COLUMN, MODIFY_COLUMN',
    column_name VARCHAR(100) NOT NULL COMMENT '字段名',
    old_data_type VARCHAR(100) COMMENT '原数据类型',
    new_data_type VARCHAR(100) COMMENT '新数据类型',
    is_processed TINYINT NOT NULL DEFAULT 0 COMMENT '是否已处理：0-未处理，1-已处理',
    detect_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '检测时间',
    process_time DATETIME COMMENT '处理时间',
    remarks VARCHAR(500) COMMENT '备注',
    KEY idx_task_id (task_id),
    KEY idx_source_table (source_table),
    KEY idx_is_processed (is_processed),
    CONSTRAINT fk_risk_task FOREIGN KEY (task_id) REFERENCES collect_task(id)
) COMMENT='表结构变更风险表';

-- 源表元数据表
CREATE TABLE source_table_meta (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '元数据ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    table_name VARCHAR(200) NOT NULL COMMENT '表名',
    schema_name VARCHAR(100) COMMENT '模式名',
    column_name VARCHAR(100) NOT NULL COMMENT '列名',
    column_type VARCHAR(100) NOT NULL COMMENT '列类型',
    column_position INT NOT NULL COMMENT '列位置',
    is_primary_key TINYINT NOT NULL DEFAULT 0 COMMENT '是否主键：0-否，1-是',
    is_nullable TINYINT NOT NULL DEFAULT 1 COMMENT '是否可为空：0-否，1-是',
    column_default TEXT COMMENT '默认值',
    column_comment VARCHAR(500) COMMENT '列注释',
    last_check_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后检查时间',
    INDEX idx_task_id (task_id),
    INDEX idx_table_name (table_name),
    CONSTRAINT fk_meta_task FOREIGN KEY (task_id) REFERENCES collect_task(id)
) COMMENT='源表元数据表';

-- 系统配置表
CREATE TABLE system_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    config_group VARCHAR(50) NOT NULL COMMENT '配置组',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value TEXT NOT NULL COMMENT '配置值',
    description VARCHAR(500) COMMENT '配置描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_group_key (config_group, config_key)
) COMMENT='系统配置表';

-- 字段映射规则表
CREATE TABLE field_mapping_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '规则ID',
    source_type VARCHAR(100) NOT NULL COMMENT '源字段类型',
    target_type VARCHAR(100) NOT NULL COMMENT '目标字段类型',
    note VARCHAR(500) COMMENT '规则描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_source_target (source_type, target_type)
) COMMENT='字段映射规则表';

-- 调度集成配置表
CREATE TABLE scheduler_integration (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    name VARCHAR(100) NOT NULL COMMENT '集成名称',
    scheduler_type VARCHAR(50) NOT NULL COMMENT '调度器类型：AZKABAN, AIRFLOW, DOLPHINSCHEDULER等',
    api_url VARCHAR(255) COMMENT '调度器API地址',
    username VARCHAR(100) COMMENT '用户名',
    password VARCHAR(255) COMMENT '密码（加密存储）',
    token VARCHAR(255) COMMENT '访问令牌',
    properties TEXT COMMENT '其他配置属性（JSON格式）',
    is_default TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认：0-否，1-是',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-有效，0-无效',
    description VARCHAR(500) COMMENT '描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_name (name)
) COMMENT='调度集成配置表';

-- 用户表
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(100) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    real_name VARCHAR(100) COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '电话',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    last_login_time DATETIME COMMENT '最后登录时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username)
) COMMENT='用户表';

-- 角色表
CREATE TABLE role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_name VARCHAR(100) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(100) NOT NULL COMMENT '角色编码',
    description VARCHAR(500) COMMENT '角色描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_role_code (role_code)
) COMMENT='角色表';

-- 操作日志表
CREATE TABLE operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(100) COMMENT '用户名',
    operation VARCHAR(200) NOT NULL COMMENT '操作内容',
    method VARCHAR(100) COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    time BIGINT COMMENT '执行时长(毫秒)',
    ip VARCHAR(64) COMMENT 'IP地址',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) COMMENT='操作日志表';

create table users(
    username varchar(50) not null primary key,
    password varchar(500) not null,
    enabled boolean not null
);

create table authorities (
    username varchar(50) not null,
    authority varchar(50) not null,
    constraint fk_authorities_users foreign key(username) references users(username)
);
create unique index ix_auth_username on authorities (username,authority);

create table groups (
    id bigint generated by default as identity(start with 1) primary key,
    group_name varchar(50) not null
);

create table group_authorities (
    group_id bigint not null,
    authority varchar(50) not null,
    constraint fk_group_authorities_group foreign key(group_id) references groups(id)
);

create table group_members (
        id bigint generated by default as identity(start with 1) primary key,
        username varchar(50) not null,
        group_id bigint not null,
        constraint fk_group_members_group foreign key(group_id) references groups(id)
);