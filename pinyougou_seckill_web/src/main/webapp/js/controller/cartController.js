app.controller('cartController', function ($scope, cartService) {
    $scope.findCartList = function () {
     cartService.findCartList().success(
         function (response) {
             $scope.cartList=response;
           $scope.totalValue= cartService.sum($scope.cartList);
         }
     );
    }
    
    //数量加减
    $scope.addGoodsToCartList=function (itemId,num) {
        cartService.addGoodsToCartList(itemId,num).success(
            function (response) {
                if(response.success){//如果成功
                    $scope.findCartList();//刷新列表
                }else {
                    alert(response.message);
                }
            }
        );
    };


    //获取当前登陆用户的地址列表
    $scope.findAddressList=function () {
        cartService.findAddressList().success(
            function (response) {
                $scope.addressList=response;
            }
        );
    }

    //选择地址
    $scope.selectAddress=function(address){
        $scope.address=address;
    }

    //判断是否是当前选中的地址
    $scope.isSelectedAddress=function(address){
        if(address==$scope.address){
            return true;
        }else{
            return false;
        }
    };

    $scope.order={payment_type:'1'};//订单对象
   //选择支付类型
    $scope.selectPayType=function (type) {
        $scope.order.payment_type=type;

    }

   //保存订单
    $scope.submitOrder=function () {
        $scope.order.receiverAreaName=$scope.address.address;//地址
        $scope.order.receiverMobile=$scope.address.mobile;//手机
        $scope.order.receiver=$scope.address.contact;//联系人
          cartService.submitOrder($scope.order).success(
            function (response) {
               if(response.success){
                 //页面跳转
                   if($scope.order.payment_type=='1'){//如果是微信付款,就跳转到支付页面
                     location.href="pay.html";
                   }else {//如果是货到付款,跳转到提示页面
                       location.href="paysuccess.html";
                   }
               }else {
                 alert(response.message);
               }
            }
        );
    }

});