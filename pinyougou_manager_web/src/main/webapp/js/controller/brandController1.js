
app.controller('brandController', function ($scope,$controller, $http,brandService) {

    $controller('baseController',{$scope:$scope});

    //查询品牌列表
    $scope.findAll = function () {
        brandService.findAll().success(function (response) {
            $scope.list = response;
        });
    }

    //分页
    $scope.findPage = function (page, size) {
        brandService.findPage().success(
            function (response) {
                $scope.list = response.rows;//显示当前页的数据
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }
    //增加
    $scope.save = function () {
        var object = null;
        if ($scope.entity.id != null) {
            object=brandService.update($scope.entity);
        }else {
            object=brandService.add($scope.entity);
        }
        object.success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新
                } else {
                    alert(response.message);//失败返回信息
                }
            }
        );
    }
    //查询实体
    $scope.findOne = function (id) {
        brandService.findOne().success(
            function (response) {
                $scope.entity = response;
            }
        );
    }

    //删除
    $scope.dele = function () {
        brandService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新
                } else {
                    alert(response.message);
                }

            }
        );
    }
    //条件查询
    $scope.searchEntity = {};
    $scope.search = function (page, size) {
        brandService.search(page, size,$scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;//显示当前页的数据
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }
});
