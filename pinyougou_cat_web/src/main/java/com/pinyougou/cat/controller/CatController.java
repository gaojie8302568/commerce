package com.pinyougou.cat.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cat.service.CatService;
import com.pinyougou.pojogroup.Cart;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CatController {
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;
    @Reference(timeout = 6000)
    private CatService catService;


    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {
        //当前登陆人的账号
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登陆:" + username);
        String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        if (cartListString == null || cartListString.equals("")) {
            cartListString = "[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
        if (username.equals("anonymousUser")) {//如果未登录
            //从cookie中提取购物车
            return cartList_cookie;
        } else {//如果已登陆
            //获取redis购物车
            List<Cart> cartList_redis = catService.findCartListFromRedis(username);
            //判断本地购物车中存在数据
            if(cartList_cookie.size()>0){
                //合并购物车逻辑得到合并后的购物车
                List<Cart> cartList = catService.mergeCartList(cartList_cookie, cartList_redis);
                //将合并后的购物车存入redis
                catService.saveCartListToRedis(username,cartList);
                //清除本地购物车
                CookieUtil.deleteCookie(request,response,"cartList");
                System.out.println("执行了合并购物车的逻辑");
                return cartList;
            }

            return cartList_redis;
        }


    }


    @RequestMapping("/addGoodsToCartList")
     @CrossOrigin(origins="http://localhost:9105")
    public Result addGoodsToCartList(Long itemId, Integer num) {

       // response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");//可以访问的域//如果涉及到cookie不能写通配符
       // response.setHeader("Access-Control-Allow-Credentials", "true");//如果操作cookie必须加上这句话
        //当前登陆人的账号
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登陆:" + name);

        try {
            //提取购物车
            List<Cart> cartList = findCartList();
            //调用方法操作购物车
            cartList = catService.addGoodsToCartList(cartList, itemId, num);
            if(name.equals("anonymousUser")){//如果未登陆
                //将新的购物车存入cookie
                String cartListString = JSON.toJSONString(cartList);
                CookieUtil.setCookie(request, response, "cartList", cartListString, 3600 * 24, "UTF-8");
            }else {//如果已经登陆
               catService.saveCartListToRedis(name,cartList);
            }



            return new Result(true, "存入购物车成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "存入购物车失败");
        }

    }

}
