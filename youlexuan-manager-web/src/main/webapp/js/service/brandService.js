app.service('brandService',function ($http) {

    this.search = function (page,rows,searchEntity) {
        return $http.post('../brand/search.do?page='+page+'&rows='+rows,searchEntity);
    }


    this.findPage = function (page,rows) {
        return  $http.get('../brand/findPage.do?page='+page+'&rows='+rows);
    }

    this.dele = function (selectIds) {
        return $http.get('../brand/dele.do?selectIds='+selectIds);
    }

    this.save = function (methodName,entity) {
        return $http.post('../brand/'+ methodName +'.do',entity)
    }

    this.findOne  = function (id) {
        return $http.get('../brand/findOne.do?id='+id)
    }

    this.findAll = function () {
        return $http.get('../brand/findAll.do')
    }

    this.selectOptionList = function () {

        return $http.get('../brand/selectOptionList.do')
    }

})