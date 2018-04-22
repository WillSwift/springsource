package com.will.ioc_di.v2.demo.action;

import com.will.ioc_di.v2.annotation.Autowired;
import com.will.ioc_di.v2.annotation.Controller;
import com.will.ioc_di.v2.annotation.RequestMapping;
import com.will.ioc_di.v2.annotation.RequestParam;
import com.will.ioc_di.v2.demo.service.IQueryService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/web")
public class MyAction {

    @Autowired
    IQueryService iQueryService;

    @RequestMapping("query.json")
    public void query(HttpServletRequest request, HttpServletResponse response,
                      @RequestParam("name") String name) {
        String result = iQueryService.query(name);
        System.out.println(result);
    }
}
