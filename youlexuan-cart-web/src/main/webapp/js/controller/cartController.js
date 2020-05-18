app.controller('cartController', function ($scope, cartService) {

    // 查询购物车列表
    $scope.findCartList = function () {
        cartService.findCartList().success(
            function (response) {
              /*  console.log(response)*/
                $scope.cartList = response;
                $scope.totalValue = cartService.sum($scope.cartList);
            }
        );
    }


    $scope.addGoodsToCartList = function (itemId, num) {
        cartService.addGoodsToCartList(itemId, num).success(
            function (response) {
                if (response.success) {
                    $scope.findCartList();
                } else {
                    alert(response.message);
                }
            }
        );
    }

    $scope.findListByUserId = function () {
        cartService.findListByUserId().success(function (response) {
                              $scope.addressList =  response;

                           /*   给予默认选项初始值*/
                             for (var i = 0;i<$scope.addressList.length;i++){
                                 if($scope.addressList[i].isDefault == '1'){

                                     $scope.address = $scope.addressList[i];
                                     break;
                                 }
                             }



        })

    }
$scope.order={paymentType:'1'};

    $scope.selectAddress = function(address){
        $scope.address = address;

    }



 $scope.isSelectAddress = function (address) {

        if($scope.address == address){
            return true;
        }else {
            return false;
        }


 }
 $scope.submit = function () {
     $scope.order.receiverAreaName = $scope.address.address;//地址
     $scope.order.receiverMobile = $scope.address.mobile;//手机
     $scope.order.receiver = $scope.address.contact;//联系人


     cartService.add($scope.order).success(function (response) {
         if(response.success){
             if($scope.order.paymentType == "1"){ /*扫码支付*/
                 location.href = "pay.html"

             }else {
                 location.href = "paysuccess.html"
             }

         }else {
             location.href = "payfail.html"
         }

     })


 }

          $scope.All = function () {
              $scope.findListByUserId();
              $scope.findCartList();
              $scope.findProvinceList();
          }
          $scope.findProvinceList = function () {

           cartService.findProvinceList().success(function (response) {

                                     $scope.sendAddressList1 =  response;

           })

          }
        /*  找出城市*/
    $scope.$watch('entity.provinceId',function (newValue,oldValue) {

        if(newValue){
            cartService.findCityList(newValue).success(function (response) {

                                  $scope.sendAddressList2 =  response

            })
        }


    })
    /*  找出小区*/

    $scope.$watch('entity.cityId',function (newValue,oldValue) {

        if(newValue){
            cartService.findAreasList(newValue).success(function (response) {

                $scope.sendAddressList3 =  response

            })
        }


    })
/*    //地址数组*/
    $scope.Alias = ['家里','父母家','公司']

    $scope.selectAlias = function (index) {

        $scope.entity.alias = $scope.Alias[index];
    }




});