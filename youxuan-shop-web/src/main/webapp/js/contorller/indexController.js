app.controller('indexController' ,function($scope,indexService,$controller){




//获取登录人的用户名
           $scope.finName = function () {
               indexService.findLoginName().success(function (response) {
                        console.log(response);
                   $scope.loginName = response.loginName;

               })



           }



});
