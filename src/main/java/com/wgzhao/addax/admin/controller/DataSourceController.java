package com.wgzhao.addax.admin.controller;

import com.wgzhao.addax.admin.model.DataSource;
import com.wgzhao.addax.admin.service.DataSourceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/datasource")
@Slf4j
@AllArgsConstructor
public class DataSourceController
{
    private DataSourceService dataSourceService;

    @PostMapping()
    public void createDataSource(@RequestBody DataSource dataSource) {
        if (dataSource == null) {
            return;
        }
        dataSourceService.createDataSource(dataSource);
    }
}
