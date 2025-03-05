package com.wgzhao.addax.admin.model;

import lombok.Data;
import javax.persistence.*;

@Entity
@Table(name = "source_template")
@Data
public class SourceTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(name = "fetch_size", nullable = false)
    private Integer fetchSize = 10000;
    
    @Column(name = "is_auto_pk", nullable = false)
    private Boolean autoPk = false;
}