package com.will.demo.mvc.action;

import com.will.demo.service.IDemoService;
import com.will.spring.annotation.Autowired;
import com.will.spring.annotation.Controller;
import com.will.spring.annotation.RequestMapping;
import com.will.spring.annotation.RequestParam;

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
