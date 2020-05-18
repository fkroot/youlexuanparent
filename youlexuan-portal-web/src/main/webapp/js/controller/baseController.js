app.controller('baseController',function ($scope) {

    $scope.reloadList = function() {
        /* $scope.findPage($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);*/
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage)


    }
    $scope.paginationConf = {
        currentPage: 1,
        totalItems: 10,
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function () {
            $scope.reloadList();//重新加载
        }
    }

    //批量删除
    $scope.selectIds = [];

    $scope.changeSelect = function($event,id){

        if($event.target.checked){

            $scope.selectIds.push(id);
        }else{
            var index =  $scope.selectIds.indexOf(id);
            $scope.selectIds.split(index,1)
        }


    };



    $scope.jsonToString=function(jsonString,key){
        var json=JSON.parse(jsonString);//将json字符串转换为json对象
        var value="";
        for(var i=0;i<json.length;i++){
            if(i>0){
                value+=","
            }
            value+=json[i][key];
        }
        return value;
    }
    //提取json字符串数据中某个属性，返回拼接字符串 逗号分隔


})