app.controller('baseController',function ($scope) {
    //��ҳ�ؼ�����  currentPage ��ǰҳ��
    //totalItems �ܼ�¼��  itemsPerPage ÿҳ�ļ�¼��
    //perPageOptions ��ҳѡ��  onChange ��ҳ�������Զ������ķ���
    $scope.paginationConf = {
        currentPage: 1,
        totalItems: 10,
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function () {
            $scope.reloadList();//���¼���
        }
    };
    //ˢ���б�
    $scope.reloadList = function () {
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    }
    //�û���ѡ��ѡ��
    $scope.selectIds = [];//�û���ѡ��ID����

    $scope.updateSelection = function ($event, id) {
        if ($event.target.checked) {
            $scope.selectIds.push(id);//push�򼯺����Ԫ��
        } else {
            var index = $scope.selectIds.indexOf(id);//����ֵ��λ��
            $scope.selectIds.splice(index, 1);//����1 �Ƴ���λ�� ����2 �Ƴ��ĸ���
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

});