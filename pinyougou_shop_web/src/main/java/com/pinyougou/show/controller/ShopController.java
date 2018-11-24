package com.pinyougou.show.controller;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/shop")
public class ShopController {

    @RequestMapping("/name.do")
    public Map name(){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
      Map map = new HashMap();
      map.put("shopName",name);
      return map;

    }
}
