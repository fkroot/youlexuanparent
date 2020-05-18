package com.offcn.cart.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.offcn.cart.service.CartService;
import com.offcn.entity.Cart;
import com.offcn.mapper.TbItemMapper;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbOrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper tbItemMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<Cart> addGoodsToCartList(List<Cart> list,Long itemId,Integer num) {

        System.out.println(1111);

//1.根据商品SKU ID查询SKU商品信息
        TbItem item = tbItemMapper.selectByPrimaryKey(itemId);

        if(null == item){
            throw new RuntimeException("商品不存在");
        }
        if(item.getStatus().equals("0")){
            throw new RuntimeException("商品状态不能操作");
        }


        //2.获取商家ID
        String sellerId = item.getSellerId();


        //3.根据商家ID判断购物车列表（List<Cart>）中是否存在该商家的购物车
                 Cart cart  = this.searchCartBySellerId(list,sellerId);

        //4.如果购物车列表中不存在该商家的购物车
        if(null == cart){
             Cart cart1 = new Cart();
             cart1.setSellerId(sellerId);
             cart1.setSellerName(item.getSeller());
                      TbOrderItem tbOrderItem = createOrderItem(item,num);
                      List<TbOrderItem> list1 = new ArrayList<>();
                      list1.add(tbOrderItem);
                      cart1.setTbOrderItems(list1);
                      list.add(cart1);

        }else {
            //5.如果购物车列表中存在该商家的购物车

            List<TbOrderItem> tbOrderItems = cart.getTbOrderItems();
            // 查询购物车明细列表中是否存在该商品
                         TbOrderItem tbOrderItem =  searchOrderItemByItemId(tbOrderItems,itemId);
                             if(null == tbOrderItem){

                                 TbOrderItem orderItem = createOrderItem(item, num);
                                cart.getTbOrderItems().add(orderItem);

                             }else {
                                 //如果存在这个商家并且这个商家的购物车中有这个商品了
                                 tbOrderItem.setNum(tbOrderItem.getNum()+num);
                                 tbOrderItem.setTotalFee(new BigDecimal(tbOrderItem.getNum()*tbOrderItem.getPrice().doubleValue()));
                                        if(tbOrderItem.getNum()<=0){
                                            tbOrderItems.remove(tbOrderItem);
                                        }
                                        if(tbOrderItems.size() == 0){
                                            list.remove(cart);
                                        }

                             }



        }
        //4.1 新建购物车对象
        //4.2 将新建的购物车对象添加到购物车列表



        //5.1. 如果没有，新增购物车明细
        //5.2. 如果有，在原购物车明细上添加数量，更改金额





        return list;
    }

    @Override
    public List<Cart> findCartListFromRedis(String userName) {

        List<Cart> cateList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(userName);
if(null == cateList){
    return new ArrayList<>();
}

        return cateList;
    }

    @Override
    public void addCartListToRedis(List<Cart> list,String userName) {
        redisTemplate.boundHashOps("cartList").put(userName,list);

    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cookieList, List<Cart> redisList) {

        for (Cart cart : cookieList) {

            for (TbOrderItem tbOrderItem : cart.getTbOrderItems()) {
                 addGoodsToCartList(redisList,tbOrderItem.getItemId(),tbOrderItem.getNum());

            }

        }


        return redisList;
    }

    public Cart searchCartBySellerId(List<Cart> list,String sellerId){

        for (Cart cart1 : list) {
            if(cart1.getSellerId().equals(sellerId)){
                return cart1;
            }

        }
        return null;

    }

    public TbOrderItem createOrderItem(TbItem item,Integer num){

        if(num <= 0){
            throw  new RuntimeException("创造商品列表时发生异常");
        }

        TbOrderItem tbOrderItem = new TbOrderItem();
        tbOrderItem.setGoodsId(item.getGoodsId());
        tbOrderItem.setItemId(item.getId());
        tbOrderItem.setNum(num);
        tbOrderItem.setPicPath(item.getImage());
        tbOrderItem.setPrice(item.getPrice());
        tbOrderItem.setTitle(item.getTitle());
        tbOrderItem.setSellerId(item.getSellerId());
        tbOrderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
        return tbOrderItem;


    }

    public TbOrderItem searchOrderItemByItemId(List<TbOrderItem> tbOrderItems,Long itemId){
        for (TbOrderItem tbOrderItem : tbOrderItems) {
            if(tbOrderItem.getItemId().longValue() == itemId.longValue()){
                return tbOrderItem;
            }


        }
        return null;

    }



}
