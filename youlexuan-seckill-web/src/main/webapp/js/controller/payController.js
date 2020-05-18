app.controller('payController',function ($scope,payService,$location) {


    $scope.createNative = function () {

        payService.createNative().success(function (response) {
     $scope.monney = (response.total_fee/100).toFixed(2);
     $scope.out_trade_no = response.out_trade_no;

     var qr = new QRious({
         element:document.getElementById('qrious'),
         size:250,
         level:'H',
         value:response.qrcode

     })

            $scope.queryPayStatus(response.out_trade_no)
        })



    }
$scope.queryPayStatus = function (out_trade_no) {
        payService.queryPayStatus(out_trade_no).success(function (response) {
            console.log(response)
            if(response.success){
                location.href = "paysuccess.html#?monney="+$scope.monney;
            }else {
                if(response.message == '支付超时'){

                    location.href = "payfail.html";
                }else {
                    location.href = "payfail.html";
                }



            }


        })




}

$scope.getMonney = function () {

       return $location.search()['monney'] ;
}



})