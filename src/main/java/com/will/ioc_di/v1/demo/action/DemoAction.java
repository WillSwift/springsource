package com.will.ioc_di.v1.demo.action;

import com.will.ioc_di.v1.demo.service.IDemoService;
import com.will.ioc_di.v1.spring.annotation.Autowired;
import com.will.ioc_di.v1.spring.annotation.Controller;
import com.will.ioc_di.v1.spring.annotation.RequestMapping;
import com.will.ioc_di.v1.spring.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/demo")
public class DemoAction {

    @Autowired
    IDemoService iDemoService;

    @RequestMapping("query.json")
    public void query(HttpServletRequest reqt, HttpServletResponse resp,
                      @RequestParam("name") String name) {
        String result = iDemoService.get(name);
        System.out.println(result);
    }

}
