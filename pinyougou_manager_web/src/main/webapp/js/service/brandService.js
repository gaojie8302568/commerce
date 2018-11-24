//Ʒ�Ʒ���
app.service("brandService",function ($http) {
    //��ѯ
    this.findAll=function () {
        return  $http.get('../brand/findAll.do');
    }
    //��ҳ
    this.findPage =function (page, size) {
        return $http.get('../brand/findPage.do?page=' + page + '&size=' + size);
    }
    //��ѯʵ��
    this.findOne =function (id) {
        return $http.get('../brand/findOne.do?id=' + id);
    }
    //����
    this.add=function(entity){
        return $http.post('../brand/add.do', entity);
    }
    //�޸�
    this.update=function (entity) {
        return $http.post('../brand/update.do', entity);
    }
    //ɾ��
    this.dele=function (ids) {
        return $http.get('../brand/delete.do?ids=' + ids);
    }
    //����
    this.search=function (page,size,searchEntity) {
        return  $http.post('../brand/search.do?page=' + page + '&size=' + size, searchEntity);
    }
    //�����б�����
    this.selectOptionList=function () {
        return $http.get('../brand/selectOptionList.do');
    }
});