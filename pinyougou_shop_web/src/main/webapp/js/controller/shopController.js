app.controller('shopController',function ($scope,shopService) {
    //��ʾ��ǰ��¼��
    $scope.shopLoginName=function () {
        shopService.shopName().success(
            function (response) {
                $scope.shopName=response.shopName;
            }
        );
}
});