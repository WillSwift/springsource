package com.will.ioc_di.v2.demo.service.impl;

import com.will.ioc_di.v2.demo.service.IQueryService;

public class QueryServiceImpl implements IQueryService {
    @Override
    public String query(String name) {
        return "my name is " + name;
    }
}
