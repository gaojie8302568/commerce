app.controller('baseController',function ($scope) {
    //分页控件配置
    $scope.paginationConf = {
        currentPage: 1,
        totalItems: 10,
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function () {
            $scope.reloadList();//重新加载
        }
    };
    //重新加载列表 数据
    $scope.reloadList = function () {
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    }

    $scope.selectIds = [];//选中的ID集合
//更新复选
    $scope.updateSelection = function ($event, id) {
        if ($event.target.checked) { //如果是被选中,则增加到数组
            $scope.selectIds.push(id);
        } else {
            var index = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(index, 1);
        }
    }
   $scope.jsonToString=function (jsonString,key) {
     var json=  JSON.parse(jsonString);
     var value="";

      for(var i= 0;i<json.length;i++){
          if(i>0){
              value += ",";
          }
          value += json[i][key]
      }
     return value;
   }
  //在list集合中根据某key的值查询对象
   $scope.searchObjectByKey=function (list,key,keyValue) {
        for (var i = 0;i< list.length;i++){
            if(list[i][key]==keyValue){
                return list[i];
            }
        }
        return  null;

   }



});