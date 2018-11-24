app.controller('shopController',function ($scope,shopService) {
    //显示当前登录名
    $scope.shopLoginName=function () {
        shopService.shopName().success(
            function (response) {
                $scope.shopName=response.shopName;
            }
        );
}
});