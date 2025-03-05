package com.wgzhao.addax.admin.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "hdfs_template")
@Data
public class HdfsTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "default_fs", nullable = false, length = 255)
    private String defaultFs;

    @Column(name = "config_path", length = 500)
    private String configPath;

    @Column(name = "hadoop_user", length = 100)
    private String hadoopUser;

    @Column(name = "file_type", nullable = false, length = 20)
    private String fileType = "orc";

    @Column(name = "compress_type", nullable = false, length = 50)
    private String compressType = "SNAPPY";

    @Column(name = "base_path", nullable = false, length = 20)
    private String basePath;

    @Column(name = "enable_kerberos", nullable = false)
    private Boolean kerberosEnabled = false;

    @Column(name = "kerberos_principal", length = 255)
    private String kerberosPrincipal;

    @Column(name = "keytab_path", length = 500)
    private String keytabPath;

    @Lob
    private String properties;

    private String note;

    @Column(name = "is_enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;
}