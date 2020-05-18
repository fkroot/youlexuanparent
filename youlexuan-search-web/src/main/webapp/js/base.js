var app=angular.module('youlexuan', []);//定义无分页模块

//sce过滤服务器

app.filter('trustHtml',['$sce',function ($sce) {
return function (data) {
    return $sce.trustAsHtml(data);
}

    
}])