package com.offcn.entity;

import com.offcn.pojo.TbOrderItem;

import java.io.Serializable;
import java.util.List;

public class Cart implements Serializable {

    private String sellerId;

    private String sellerName;

    private List<TbOrderItem> tbOrderItems;


    public Cart() {
    }



    public Cart(String sellerId, String sellerName, List<TbOrderItem> tbOrderItems) {
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.tbOrderItems = tbOrderItems;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public List<TbOrderItem> getTbOrderItems() {
        return tbOrderItems;
    }

    public void setTbOrderItems(List<TbOrderItem> tbOrderItems) {
        this.tbOrderItems = tbOrderItems;
    }

    @Override
    public String toString() {
        return "Cart{" +
                "sellerId='" + sellerId + '\'' +
                ", sellerName='" + sellerName + '\'' +
                ", tbOrderItems=" + tbOrderItems +
                '}';
    }
}
