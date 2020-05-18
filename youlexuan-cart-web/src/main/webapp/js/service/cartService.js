//用户表服务层
app.service('cartService',function($http){
	    	
	//购物车列表
	this.findCartList=function(){
		return $http.get('cart/findCartList.do');
	}

	// 添加商品到购物车
	this.addGoodsToCartList=function(itemId, num){
		return $http.get('cart/addGoodsToCartList.do?itemId=' + itemId + "&num=" + num);
	}

	this.sum=function(cartList){


		var totalValue = {totalNum:0,totalMoney:0.00};
		for (var i = 0; i < cartList.length; i++) {

			var cart = cartList[i];
			for (var j = 0; j < cart.tbOrderItems.length; j++) {
				var orderItem = cart.tbOrderItems[j];// 购物车明细
              /*  console.log('第一个商家第一种商品的价格'+cartList[i].tbOrderItems[j])*/

				totalValue.totalNum = totalValue.totalNum+orderItem.num;
				/*console.log('总数'+totalValue.totalNum)*/
				totalValue.totalMoney =totalValue.totalMoney+ orderItem.totalFee;
                /*console.log('总钱数'+totalValue.totalMoney)*/

			}
		}
		return totalValue;
	}

	this.findListByUserId = function () {
		return $http.get('../address/findListByUserId.do')

    }
    this.add = function (order) {
        return $http.post('../order/add.do',order)
    }

  /*  找出所有的省份进行展示*/
    this.findProvinceList = function () {

        return $http.get('../address/findProvinceList.do')
    }
    this.findCityList  = function (newValue) {

        return $http.get('../address/findCityList.do?provinceId='+newValue)
    }
    this.findAreasList = function (newValue) {
        return $http.get('../address/findAreasList.do?cityId='+newValue)

    }

});