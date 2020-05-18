app.controller('brandController' ,function($scope,brandService,$controller){

    $controller('baseController',{$scope:$scope});

    //实现伪继承，贯通$scope域


    //模糊查询
    $scope.searchEntity = {};

    $scope.search = function(page,rows){
        brandService.search(page,rows,$scope.searchEntity).success(function (response) {

            $scope.paginationConf.totalItems  =  response.total;
            $scope.list =  response.rows

        })
    }



    $scope.findPage = function(page,rows){
        brandService.findPage(page,rows).success(function (response) {

            $scope.paginationConf.totalItems  =  response.total;
            $scope.list =  response.rows

        })

    }



    $scope.dele = function(){
        brandService.dele($scope.selectIds).success(function (response) {
            if(response.success){
                $scope.reloadList();
            }else {
                alert(response.message);
            }


        })
    }




//新建
    $scope.save=function(){
        var methodName='add';//方法名称
        if($scope.entity.id!=null){//如果有ID
            methodName='update';//则执行修改方法
        }
        brandService.save(methodName,$scope.entity).success(
            function(response){
                if(response.success){
                    //重新查询
                    $scope.reloadList();//重新加载
                }else{
                    alert(response.message);
                }
            }
        );
    }



    //修改
    $scope.findOne = function(id){
        /*console.log(id);*/
        brandService.findOne(id).success(function (response) {

            $scope.entity =   response;

        })
    };



    //读取列表数据绑定到表单中
    $scope.findAll=function(){
        brandService.findAll().success(
            function(response){
                $scope.list=response;
            }
        );
    }
    //全选方法
    $scope.selectAll=function($event){
        if($event.target.checked){
            //循环遍历list集合
            for(var i=0;i<$scope.list.length;i++){
                $scope.selectIds.push($scope.list[i].id);
            }
        }else{
            $scope.selectIds=[];
        }
    }

    //判断当前复选框是否被选中，选中返回true，未选中返回false
    $scope.iselected=function(id){
        //循环遍历选中数组集合
        for(var i=0;i<$scope.selectIds.length;i++){
            if($scope.selectIds[i]==id){
                return true;
            }
        }
        return false;
    }


    /*   判断单选框是否被选中*/





})
