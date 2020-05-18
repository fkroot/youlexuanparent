app.controller('seckillGoodsController',function ($scope,seckillGoodsService,$location,$interval) {


    $scope.findList = function () {
        seckillGoodsService.findList().success(function (response) {
                              $scope.list  =   response;
                              console.log(response)


        })
    }
    $scope.findOne = function () {
            var id =  $location.search()['id']
        seckillGoodsService.findOne(id).success(function (response) {
        $scope.entity = response;

        allSecond = Math.floor(((new Date($scope.entity.endTime).getTime()) - (new Date().getTime()))/1000)

            time = $interval(function () {
                if(allSecond > 0){
                    allSecond = allSecond -1;
                    $scope.timeString = converTimeString(allSecond);
                }else {
                    $interval.cancel(time);
                    alert('秒杀活动已经结束，请你下次再来')
                }


            },1000)


        })

    }

converTimeString = function(allSecond){

        var day = Math.floor(allSecond/(3600*24))
        var hours = Math.floor((allSecond-(day*3600*24))/(3600))
         var mins = Math.floor((allSecond-(day*3600*24)-(hours*3600))/60)
          var seconds = allSecond-(day*3600*24)-(hours*3600)-(mins*60)
           var timeString = ''

    if(day>0){
        timeString = day+"天:"
    }
         return timeString+""+hours+":"+mins+":"+seconds

}




$scope.tiaoZhuan = function (id) {
    location.href = 'seckill-item.html#?id='+id
}
//用户下单后提交订单
    $scope.submitOrder = function () {
        seckillGoodsService.submitOrder($scope.entity.id).success(function (response) {
            if(response.success){
                location.href = 'pay.html'
            }else{
                if(response.message == '请先进行登录'){
                    location.href = 'login.html'
                }
                alert(response.message)
            }



        })
    }


})