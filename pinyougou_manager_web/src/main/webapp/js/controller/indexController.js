app.controller('indexController',function ($scope,loginService) {

    //��ʾ��ǰ�û���
    $scope.showLoginName=function () {
        loginService.loginName().success(
            function (response) {
               $scope.loginName=response.loginName;
            }
        );

    }
});