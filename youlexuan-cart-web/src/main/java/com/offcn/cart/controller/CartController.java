package com.offcn.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.offcn.cart.service.CartService;
import com.offcn.entity.Cart;
import com.offcn.entity.Result;
import com.offcn.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {
   @Reference
   private CartService cartService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;


    @RequestMapping("/findCartList")
    public List<Cart> findCartList(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String cartListString  = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        if(cartListString==null || cartListString.equals("")){
            cartListString="[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
        if(username.equals("anonymousUser")){//如果未登录
            return cartList_cookie;
        }else{
            List<Cart> cartList_redis =cartService.findCartListFromRedis(username);//从redis中提取
            if(cartList_cookie.size()>0){//如果存在cookie购物车
                //合并购物车
                cartList_redis=cartService.mergeCartList(cartList_redis, cartList_cookie);
                //清除本地cookie的数据
                CookieUtil.deleteCookie(request, response, "cartList");
                //将合并后的数据存入redis
                cartService.addCartListToRedis(cartList_redis,username);
            }
            return cartList_redis;
        }




    }

    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins="http://localhost:9105",allowCredentials="true")
    public Result addGoodsToCartList(Long itemId,Integer num){

       /* response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
        response.setHeader("Access-Control-Allow-Credentials", "true");*/


        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录用户："+username);
        try {
            List<Cart> cartList =findCartList();//获取购物车列表
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            if(username.equals("anonymousUser")){ //如果是未登录，保存到cookie
               CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList),3600*24 ,"UTF-8");
                System.out.println("向cookie存入数据");
            }else{//如果是已登录，保存到redis
                cartService.addCartListToRedis(cartList,username);
            }
            return new Result(true, "添加成功");
        }  catch (RuntimeException e) {
            e.printStackTrace();
            return new Result(false, e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败");
        }



    }
}
