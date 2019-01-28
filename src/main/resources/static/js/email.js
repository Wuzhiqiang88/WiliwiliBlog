$(function () {
    <!--数据验证-->
    $("#registerForm").bootstrapValidator({
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
            nick: {
                message: '昵称非法',
                validators: {
                    notEmpty: {
                        message: '昵称不能为空'
                    },
                    //                        限制字符串长度
                    stringLength: {
                        min: 2,
                        max: 6,
                        message: '昵称长度必须位于2到6之间'
                    },
                    remote: {
                        type: "POST",
                        message: '昵称已注册',
                        url: '/checkNick',
                        data: '',//这里默认会传递该验证字段的值到后端,
                        delay: 2000,//每输入一个字符，就发ajax请求，服务器压力还是太大，设置2秒发送一次ajax（默认输入一个字符，提交一次，服务器压力太大）
                    }
                },
            },
//                电子邮箱
            email: {
                validators: {
                    notEmpty: {
                        message: '邮箱地址不能为空'
                    },
                    emailAddress: {
                        message: '请输入正确的邮箱地址'
                    },
                    remote: {
                        type: "POST",
                        message: '邮箱地址已注册',
                        url: '/checkEmail',
                        data: '',//这里默认会传递该验证字段的值到后端,
                        delay: 2000,//每输入一个字符，就发ajax请求，服务器压力还是太大，设置2秒发送一次ajax（默认输入一个字符，提交一次，服务器压力太大）
                    }
                }
            },

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
                        regexp: /^[a-zA-Z0-9_\.-]+$/,
                        message: '密码由数字字母下划线、减号和.组成'
                    }
                },
            },
            tryCode: {
                validators: {
                    notEmpty: {
                        message: '请输入验证码'
                    },
                }
            }
        }
    })

})


var wait = 60; // Email验证码60秒后才可获取下一个
var messageData = null;

/**
 * 获取验证码
 * @param that
 */
function getEmailCode(that) {
    var email = $('#email').val();
    //邮箱格式正则表达式
    var reg = new RegExp("^[a-z0-9]+([._\\-]*[a-z0-9])*@([a-z0-9]+[-a-z0-9]*[a-z0-9]+.){1,63}[a-z0-9]+$");
    if (email != null && email != "") {
        if (reg.test(email)) {
            setButtonStatus(that); // 设置按钮倒计时
            var obj = {
                email: email
            };
            $.ajax({
                url: '/sendEmail', // 后台邮件发送接口
                type: 'POST',
                dataType: 'json',
                contentType: "application/json",
                async: true,
                data: JSON.stringify(obj),
                success: function (result) {
                    if (!$.isEmptyObject(result)) {
                        messageData = result;
                    } else {
                        /*bootoast({
                            message: "邮箱地址不存在！",
                            type: 'danger',
                            position: 'top',
                            timeout: 2
                        });*/
                    }

                },
                error: function (XMLHttpRequest, textStatus, errorThrown) {
                    console.log(XMLHttpRequest.status);
                    console.log(XMLHttpRequest.readyState);
                    console.log(textStatus);
                }
            });
        } else {
            bootoast({
                message: "邮箱地址格式不对！",
                type: 'danger',
                position: 'top',
                timeout: 2
            });
        }
    } else {
        bootoast({
            message: "邮箱地址不能为空！",
            type: 'danger',
            position: 'top',
            timeout: 2
        });
    }

}

/**
 * 设置按钮状态
 */
function setButtonStatus(that) {
    if (wait == 0) {
        that.removeAttribute("disabled");
        that.value = "获取验证码";
        wait = 60;
    } else {
        that.setAttribute("disabled", true);
        that.value = "重新发送(" + wait + ")";
        wait--;
        setTimeout(function () {
            setButtonStatus(that)
        }, 1000)
    }
}

/**
 * 注册按钮
 */
function toRegister() {
    /*手动验证表单，当是普通按钮时。*/
    $('#registerForm').data('bootstrapValidator').validate();//启用验证
    var flag = $('#registerForm').data('bootstrapValidator').isValid()//验证是否通过true/false
    if (flag) {
        if (messageData == null) {
            bootoast({
                message: "验证码不正确，注册失败！",
                type: 'danger',
                position: 'top',
                timeout: 2
            });
        } else {
            var data = {
                inputCode: $('#tryCode').val(),
                tamp: messageData.tamp,
                hash: messageData.hash,
                nick: $('#nick').val(),
                password: $('#password').val(),
                email: $('#email').val(),
            };

            $.ajax({
                url: '/registerByEmail', // 验证接口
                type: 'POST',
                dataType: 'json',
                contentType: "application/json",
                data: JSON.stringify(data),
                async: false, //false 同步
                success: function (data) {
                    //业务处理
                    if (data.status != 200) {
                        bootoast({
                            message: data.msg,
                            type: 'danger',
                            position: 'top',
                            timeout: 2
                        });
                    } else {
                        //注册成功
                        bootoast({
                            message: data.msg,
                            type: 'success',
                            position: 'top',
                            timeout: 2
                        });
                        var url=data.url;
                        //登录
                        $.ajax({
                            url: '/login', // 登录接口
                            type: 'POST',
                            data:{
                                password: $('#password').val(),
                                username: $('#email').val(),
                                rememberMe:false,
                            },
                            success: function (data) {
                                //业务处理
                                setTimeout(function(){  //使用  setTimeout（）方法设定定时2000毫秒
                                   window.location.href = url;
                                },2000);

                            },
                            error: function (XMLHttpRequest, textStatus, errorThrown) {
                                console.log("error")
                                console.log(XMLHttpRequest.status);
                                console.log(XMLHttpRequest.readyState);
                                console.log(textStatus);
                            }
                        });
                    }

                },
                error: function (XMLHttpRequest, textStatus, errorThrown) {
                    console.log(XMLHttpRequest.status);
                    console.log(XMLHttpRequest.readyState);
                    console.log(textStatus);
                }
            });
        }
    }

}
