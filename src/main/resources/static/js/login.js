$(function () {
    <!--数据验证-->
    $("#loginForm").bootstrapValidator({
        message: 'This value is not valid',
//            定义未通过验证的状态图标
        feedbackIcons: {
            /*输入框不同状态，显示图片的样式*/
            valid: 'glyphicon glyphicon-ok',
            invalid: 'glyphicon glyphicon-remove',
            validating: 'glyphicon glyphicon-refresh'
        },
//            字段验证
        fields: {
//                密码
            password: {
                message: '密码非法',
                validators: {
                    notEmpty: {
                        message: '密码不能为空'
                    },
//                        限制字符串长度
                    stringLength: {
                        min: 3,
                        max: 20,
                        message: '密码长度必须位于3到20之间'
                    },
//                        基于正则表达是的验证
                    regexp: {
                        regexp: /^[a-zA-Z0-9_\.]+$/,
                        message: '密码由数字字母下划线和.组成'
                    }
                }
            },

//                用户名
            username: {
                validators: {
                    notEmpty: {
                        message: '手机号或邮箱不能为空'
                    }
                }
            },
        }
    })

})

/**
 * 登录按钮
 */
function toLogin() {
    /*手动验证表单，当是普通按钮时。*/
    $('#loginForm').data('bootstrapValidator').validate();//启用验证
    var flag = $('#loginForm').data('bootstrapValidator').isValid()//验证是否通过true/false
    if (flag) {
        $.ajax({
            url: '/login', // 登录接口
            type: 'POST',
            data: $('#loginForm').serialize(),
            success: function (data) {
                console.log("success");
                //业务处理
                if (data.status != 200) {
                    bootoast({
                        message: data.msg,
                        type: 'danger',
                        position: 'top',
                        timeout: 2
                    });
                } else {
                    window.location.href =  data.url;
                }
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
                console.log("error")
                console.log(XMLHttpRequest.status);
                console.log(XMLHttpRequest.readyState);
                console.log(textStatus);
            }
        });
    }
}


