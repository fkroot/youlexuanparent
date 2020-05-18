package com.offcn.sellergoods.controller;


import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/loginName")
public class indexController {

    @RequestMapping("/name")
    public Map name(){
        String name=SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println(3);
        System.out.println(name);
        Map map=new HashMap();
        map.put("loginName", name);
        return map ;
    }
}

