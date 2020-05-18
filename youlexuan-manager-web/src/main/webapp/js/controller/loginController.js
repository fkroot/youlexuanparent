app.controller('loginController' ,function($scope,loginService,$controller){




//获取登录人的用户名

           $scope.showLoginName = function () {

               loginService.findLoginName().success(function (response) {
                        console.log(response);
                   $scope.loginName = response.loginName;

               })



           }



})
