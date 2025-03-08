package com.wgzhao.addax.admin.service;

import com.wgzhao.addax.admin.model.DataSource;
import com.wgzhao.addax.admin.repository.DataSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataSourceService
{
    @Autowired
    private DataSourceRepository dataSourceRepository;

    public void createDataSource(DataSource dataSource) {
        dataSourceRepository.save(dataSource);
    }

}
