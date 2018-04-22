package com.will.ioc_di.v1.demo.service.impl;

import com.will.ioc_di.v1.demo.service.IDemoService;
import com.will.ioc_di.v1.spring.annotation.Service;

@Service
public class DemoServiceImpl implements IDemoService {
    public String get(String name) {
        return "my name is " + name;
    }
}
