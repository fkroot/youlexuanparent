 //控制层 
app.controller('goodsController' ,function($scope,$controller ,goodsService,uploadService,itemCatService,typeTemplateService,$location){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(){

        // 此种写法目的: 可使用如下形式的网络请求进行触发
        // http://localhost:9102/admin/goods_edit.html#?id=149187842867969
        // 注意问号前加#
        // 由于商品编辑也页面布局在另一个html文件中goods_edit.html
                        var id =   $location.search()['id'];//获取参数值
                        if(id == null){
                        	return;
						}



        goodsService.findOne(id).success(

			function(response){
				$scope.entity= response;
				editor.html($scope.entity.goodsDesc.introduction)
                $scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);

                //显示扩展属性
                //只改此处不可以, 因为读取出来的值被覆盖了，还需要改写下面的代码, 添加判断，当用户没有传递id参数时再执行此逻辑

                    $scope.entity.goodsDesc.customAttributeItems = JSON.parse( $scope.entity.goodsDesc.customAttributeItems);//扩展属性
                    $scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);
                         
                    for (var i = 0;i<$scope.entity.itemList.length;i++){
                    	$scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec)
					}

			}
		);				
	}

        //保存
        $scope.save=function(){

		        $scope.entity.goodsDesc.introduction  =  editor.html();
            var serviceObject;//服务层对象
		if($scope.entity.goods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        /*	$scope.reloadList();//重新加载*/
					alert(response.message);

					$scope.entity = {goodsDesc:{itemImages:[],specificationItem:[]}}
					$scope.entity ={};
					editor.html('')
					location.href="goods.html";//修改点击保存成功之后跳转到goog.html页面

				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	//保存
	$scope.add = function () {
        // 提取kindeditor编辑器的内容
        $scope.entity.goodsDesc.introduction=editor.html();
goodsService.add($scope.entity).success(function (response) {
    console.log(entity);
	if(response.success){
        editor.html('');
	/*	console.log(response.message)*/
	}else{
		alert("保存失败")
	}

})


    }
//上传图片
    $scope.uploadImg = function () {
        uploadService.uploadImg().success(function (response) {
            if(response.success){
                      $scope.image_entity.url  = response.message;
            }else{
                alert(response.message)
            }


        }).error(function () {
            alert("上传图片的过程中发生错误")
        })
    }
    //定义保存数据的实体类
	$scope.entity = {goods:{},goodsDesc:{itemImages:[]}};
    //保存图片
    $scope.add_image_entity = function () {

		$scope.entity.goodsDesc.itemImages.push($scope.image_entity)
    }

    //删除图片
   $scope.remove_iamge_inList = function (index) {
       $scope.entity.goodsDesc.itemImages.splice(index,1)

   }
//显示一级分类

	$scope.selectIteamCat1List = function () {
		itemCatService.findByParentId(0).success(function (response) {

			$scope.itemCat1List = response;


        })


    };
    //显示二级分类
	$scope.$watch('entity.goods.category1Id',function (newValue,oldValue) {

		//判断一级分类是否改变，
		if(newValue){
			itemCatService.findByParentId(newValue).success(function (response) {

				                                   $scope.itemCat2List =  response

            })


		}


    });
//显示三级分类

	$scope.$watch('entity.goods.category2Id',function (newValue,oldValue) {

		if(newValue){
			itemCatService.findByParentId(newValue).success(function (response) {

				$scope.itemCat3List = response;

            })
		}



    });
//显示模板id
              $scope.$watch('entity.goods.category3Id',function (newValue,oldValue) {
              	if(newValue){
              		itemCatService.findOne(newValue).success(function (response) {
                        $scope.entity.goods.typeTemplateId   = response.typeId;

                    })
				}
})
//模板id更新之后，更新品牌的下拉列表，数据表typetemplate

	$scope.$watch('entity.goods.typeTemplateId',function (newValue,oldValue) {
		if(newValue){
			typeTemplateService.findOne(newValue).success(function (response) {
				$scope.typeTemplate = response;
                $scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds)
				if($location.search()['id'] == null) {

                    $scope.typeTemplate.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems)
                }



            })

			typeTemplateService.findSpecList(newValue).success(function (response) {

				                        $scope.specList  = response;



            })



		}


    })



    $scope.searchObjectByKey = function (list,key,keyValue) {
        for(var i = 0;i<list.length;i++) {

            if (list[i][key] == keyValue) {
                return list[i];
            }
        }
        return null;
    }



    //将选中的规格选项填充进数据库的goodsDesc.specfication字段中
	$scope.entity = {
              	goodsDesc:{itemImages:[],specificationItems:[]}
              };


    $scope.entity={ goodsDesc:{itemImages:[],specificationItems:[]}  };

    $scope.updateSpecAttribute=function($event,name,value){
        var object= $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems ,'attributeName', name);
        if(object!=null){
            if($event.target.checked ){
                object.attributeValue.push(value);
            }else{
                //取消勾选
                object.attributeValue.splice( object.attributeValue.indexOf(value ) ,1);//移除选项
                //如果选项都取消了，将此条记录移除
                if(object.attributeValue.length==0){
                    $scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(object),1);
                }
            }
        }else{
            $scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]});
        }
    }


    //创建SKU列表
    $scope.createItemList=function(){
        $scope.entity.itemList=[{spec:{},price:0,num:99999,status:'0',isDefault:'0' } ];//初始
        var items=  $scope.entity.goodsDesc.specificationItems;
        for(var i=0;i< items.length;i++){
            $scope.entity.itemList = addColumn( $scope.entity.itemList,items[i].attributeName,items[i].attributeValue );
        }
    }
    //添加列值
    addColumn=function(list,columnName,conlumnValues){
        var newList=[];//新的集合
        for(var i=0;i<list.length;i++){
            var oldRow= list[i];
            for(var j=0;j<conlumnValues.length;j++){
                var newRow= JSON.parse( JSON.stringify( oldRow )  );//深克隆
                newRow.spec[columnName]=conlumnValues[j];
                newList.push(newRow);
            }
        }
        return newList;
    }

    //根据规格名称和选项名称返回是否被勾选
    $scope.checkAttributeValue=function(specName,optionName){
        var items= $scope.entity.goodsDesc.specificationItems;
        var object= $scope.searchObjectByKey(items,'attributeName',specName);
        if(object==null){
            return false;
        }else{
            if(object.attributeValue.indexOf(optionName)>=0){
                return true;
            }else{
                return false;
            }
        }
    }
    //添加一个数组用来显示状态
	$scope.statu =['未审核','已经审核','审核通过','审核未通过']

    //显示分级类目名称
	$scope.itemCatList = [];

    $scope.findItemList = function () {
   itemCatService.findAll().success(function (response) {
 for (var i = 0;i<response.length;i++){
                         $scope.itemCatList[response[i].id]  =  response[i].name;
 }



   })

    }


})

