$(function () {
    var msg = $('#errorMsg').val();
    if (msg != null&&msg!="") {
        bootoast({
            message: msg,
            type: 'danger',
            position: 'top',
            timeout: 2
        });
    }

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
