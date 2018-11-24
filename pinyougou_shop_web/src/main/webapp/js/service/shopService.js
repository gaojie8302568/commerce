app.service('shopService',function ($http) {

    this.shopName=function () {
       return $http.get('../shop/name.do');
    }

});