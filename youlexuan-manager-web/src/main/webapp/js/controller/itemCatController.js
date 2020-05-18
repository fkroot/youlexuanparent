 //ååç±»ç®控制层 
app.controller('itemCatController' ,function($scope,$controller,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	//初值
	$scope.parentId = 0;

    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		itemCatService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		itemCatService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		itemCatService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=itemCatService.update( $scope.entity ); //修改  
		}else{

			$scope.entity.parentId = $scope.parentId;
			serviceObject=itemCatService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询

		        	/*$scope.reloadList();//重新加载*/

                    $scope.findByParentId($scope.parentId);//重新加载

				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		itemCatService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					/*$scope.reloadList();*///刷新列表
                    $scope.findByParentId($scope.parentId);
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象


	//搜索
	$scope.search=function(page,rows){
        $scope.searchEntity.parentId = $scope.parentId;
		itemCatService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	//通过上级id查找下级
	$scope.findByParentId = function (parentId) {

		$scope.parentId = parentId;

		itemCatService.findByParentId(parentId).success(function (response) {

			         $scope.list = response


        })


    }
//设置点击下级时的grade的变化
	$scope.grade = 1;
	$scope.setGrade = function (value) {

		$scope.grade = value;

    }
    //读取列表
    // 页面加载时最先在body调用findByParentId(), 此时呈现了一级页面,并填充了entity
    // 初次使用时,面包屑不应该有二级和三级显示, 所以entity_x=null 页面中就取不到{{entity_x.name}}
    // 首次进入二级一定是点击的”查询下级”, 而不是面包屑.  此时会填充 entity_1数据(填充了一级页面的entity数据)
	$scope.selectList = function (p_entity) {
		if($scope.grade == 1){
			$scope.entity_1 = null
            $scope.entity_2 = null
		}
		if($scope.grade == 2){
               $scope.entity_1 = p_entity;
            $scope.entity_2 = null

		}

        if($scope.grade == 3){

            $scope.entity_2 = p_entity;

        }
        $scope.findByParentId(p_entity.id)

    }
    $scope.TypeListOption = {data:[]};
//找到findTypeTemplateList的列表
	$scope.selectOptionList = function () {



		typeTemplateService.selectOption().success(function (response) {
            console.log(response)
            $scope.TypeListOption = {data:response};
        })
    }


    
});	