package com.will.demo.service.impl;

import com.will.demo.service.IDemoService;
import com.will.spring.annotation.Service;

@Service
public class DemoServiceImpl implements IDemoService {
    public String get(String name) {
        return "my name is " + name;
    }
}
