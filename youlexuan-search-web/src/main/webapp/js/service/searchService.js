app.service("searchService",function($http){
    this.search=function(searchMap){
        return $http.post('./search/find.do',searchMap);
    }
});
