app.service('indexService',function ($http) {



    this.findLoginName = function () {

        return $http.get('../login/name.do')
    }


});