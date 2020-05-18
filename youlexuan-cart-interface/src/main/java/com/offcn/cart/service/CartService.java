package com.offcn.cart.service;


import com.offcn.entity.Cart;

import java.util.List;

public interface CartService {


     List<Cart> addGoodsToCartList(List<Cart> list, Long itemId, Integer num);

     //从redis中获取cartList
     List<Cart> findCartListFromRedis(String userName);
//想redis中存储数据
     void addCartListToRedis(List<Cart> list,String userName);
//合并cookie中的购物车和rediis中的购物车，合并的情况，当用户登录时并且为登录的时候选择了商品

     List<Cart> mergeCartList(List<Cart> cookieList,List<Cart> redisList);


}
