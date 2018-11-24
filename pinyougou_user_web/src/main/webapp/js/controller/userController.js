 //控制层 
app.controller('userController' ,function($scope,$controller   ,userService){	
   //注册用户
    $scope.reg=function () {

        if($scope.password!=$scope.entity.password){
            alert("两次输入的密码不一致,请重新输入");
            $scope.password="";
            $scope.entity.password="";
            return;
        }
        userService.add($scope.entity,$scope.smscode).success(
            function (response) {
                alert(response.message);
            }
        );
    }
  //手机验证码发送
    $scope.sendCode=function () {
        if($scope.entity.phone ==null || $scope.entity.phone ==""){
            alert("请填写手机号码")
            return;
        }
        userService.sendCode($scope.entity.phone).success(
            function (response) {
              alert(response.message);
            }
        );
    }
    
});	
