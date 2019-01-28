$(document).ready(function() {
    $.ajax({
        url: '/getUserInfo', // 获取登录的用户信息
        type: 'POST',
        success: function (data) {
            console.log("success")
            //业务处理
            if (data != null && data != "") {
                $('#avatar').attr("src",data.avatar);
                $('#avatar').attr("title",data.nick);
            }
        }
    });


});