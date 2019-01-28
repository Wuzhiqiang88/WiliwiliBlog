package com.sao.util;

/**
 * 我的授权码icfpvsnwofegeaef
 */

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * 邮件工具类
 */
public class EmailUtils {
    /**
     * 发送邮件
     * @param to 给谁发
     * @param verifyCode 验证码
     */
    public static void send_mail(String to,String verifyCode) throws MessagingException {
        //创建连接对象 连接到邮件服务器
        Properties properties = new Properties();
        //设置发送邮件的基本参数
        //发送邮件服务器(注意，此处根据你的服务器来决定，如果使用的是QQ服务器，请填写smtp.qq.com)
        properties.setProperty("mail.smtp.host", "smtp.qq.com");
        //发送端口（根据实际情况填写，一般均为25）
        properties.setProperty("mail.smtp.port", "25");
        properties.setProperty("mail.smtp.auth","true");//是否开启权限控制

        //设置发送邮件的账号和授权码
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                //两个参数分别是发送邮件的账户和密码(注意，如果配置后不生效，请检测是否开启了 POP3/SMTP 服务，QQ邮箱对应设置位置在: [设置-账户-POP3/IMAP/SMTP/Exchange/CardDAV/CalDAV服务])
                return new PasswordAuthentication("2440239053@qq.com","icfpvsnwofegeaef");
            }
        });

        //创建邮件对象
        Message message = new MimeMessage(session);
        //设置发件人
        message.setFrom(new InternetAddress("2440239053@qq.com"));
        //设置收件人
        message.setRecipient(Message.RecipientType.TO,new InternetAddress(to));
        //设置主题
        message.setSubject("【Wiliwili博客】验证码");
        //设置邮件正文  第二个参数是邮件发送的类型
        message.setContent("尊敬的用户，您好，您正在注册帐号，本次请求的邮件验证码是： <b>"+verifyCode+"</b> <br>本验证码 10 分钟内有效，请及时输入。如非本人操作，请忽略该邮件。","text/html;charset=UTF-8");
        //发送一封邮件
        Transport.send(message);
    }

   /* public static void main(String[] args) {
        long startTime=System.currentTimeMillis();   //获取开始时间
        try {
            EmailUtils.send_mail("635354107@qq.com", "333323");
            System.out.println("邮件发送成功!");
            long endTime=System.currentTimeMillis();  //获取结束时间
            System.out.println("程序运行时间："+(endTime-startTime)+"ms");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }*/
}