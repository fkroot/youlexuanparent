//服务层
app.service('uploadService',function($http){

	this.uploadImg=function(){
		var formData = new FormData();
		formData.append("file", file.files[0]);
		/*
			anjularjs对于post和get请求默认的Content-Type heander是application/json
			通过设置为undefined, 使浏览器帮我们把Content-Type设置为multipart/form-data
			通过设置transformRequest:angular.identity 将序列化formdata object
		 */
		return $http({
			method:'POST',
			url:"../upload.do",
			data:formData,
			headers:{'Content-Type':undefined},
			transformRequest:angular.identity
		});
	}

});