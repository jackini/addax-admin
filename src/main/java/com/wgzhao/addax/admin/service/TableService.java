package com.wgzhao.addax.admin.service;

import com.wgzhao.addax.admin.repository.TableColumnRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TableService
{
    private final TableColumnRepository tableColumnRepository;
}
