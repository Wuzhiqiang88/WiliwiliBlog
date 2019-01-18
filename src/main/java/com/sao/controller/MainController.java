package com.sao.controller;


import com.sao.domain.User;
import com.sao.service.UserService;
import com.sao.util.EmailUtils;
import com.sao.util.MD5Utils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;

import javax.mail.MessagingException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class MainController {

    @Autowired
    private UserService userService;

    private static final String KEY = "wiliwili"; // KEY为自定义秘钥

    @GetMapping("/")
    public String root(){
        return "redirect:/index";
    }
    @GetMapping("/index")
    public String index(){
        return "index";
    }
    @GetMapping("/login")
    public String login(){
        return "login";
    }
    @GetMapping({"/register/email","/register"})
    public String toEmail() {
        return "email";
    }
    @GetMapping("/register/phone")
    public String toPhone() {
        return "phone";
    }

    /**
     * 用户登录
     * @param username
     * @param password
     * @param model
     * @return
     */
    @PostMapping("/login")
    public String login(String username,String password,Model model,boolean rememberMe){
        //判断用户名和密码为空
        if (StringUtils.isEmpty(username)||StringUtils.isEmpty(password)){
            model.addAttribute("msg","用户名和密码不能为空！");
            return "login.html";
        }
        UsernamePasswordToken token = new UsernamePasswordToken(username,password,rememberMe);

        //开始登录
        try {
            SecurityUtils.getSubject().login(token);
            return "index";
        }catch (UnknownAccountException e){
            //用户名不存在
            model.addAttribute("msg","用户名或密码错误！");
            return "login";
        }catch (IncorrectCredentialsException e){
            //密码错误
            model.addAttribute("msg","用户名或密码错误！");
            return "login";
        }
    }

    /**
     * 发送邮件验证码
     * @param requestMap
     * @return
     */
    @RequestMapping(value = "/sendEmail", method = RequestMethod.POST, headers = "Accept=application/json")
    @ResponseBody
    public Map<String, Object> sendEmail(@RequestBody Map<String,Object> requestMap) {
        System.out.println("sendEmail---");
        Map<String, Object> resultMap = new HashMap<>();
        String email = requestMap.get("email").toString();
        String randomNum = String.valueOf(new Random().nextInt(899999) + 100000);//生成验证码
        try {
            EmailUtils.send_mail(email,randomNum); //此处执行发送邮件验证码方法
        } catch (MessagingException e) {
            e.printStackTrace();
            return resultMap;
        }
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, 10);
        String currentTime = sf.format(c.getTime());// 生成10分钟后时间，用户校验是否过期
        String hash =  MD5Utils.getMD5Code(KEY + "@" + currentTime + "@" + randomNum);//生成MD5值
        resultMap.put("hash", hash);
        resultMap.put("tamp", currentTime);
        return resultMap; //将hash值和tamp时间返回给前端
    }

    /**
     * 用邮箱注册
     * @param requestMap
     * @return
     */
    @RequestMapping(value = "/registerByEmail", method = RequestMethod.POST, headers = "Accept=application/json")
    @ResponseBody
    public Map<String, Object> registerByEmail(@RequestBody Map<String,Object> requestMap,User user) {
        Map<String, Object> resultMap = new HashMap<>();
        String requestHash = requestMap.get("hash").toString();
        String tamp = requestMap.get("tamp").toString();
        String inputCode = requestMap.get("inputCode").toString();
        String nick=requestMap.get("nick").toString();
        String email=requestMap.get("email").toString();
        String password=requestMap.get("password").toString();
        String hash = MD5Utils.getMD5Code(KEY + "@" + tamp + "@" + inputCode);
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
        Calendar c = Calendar.getInstance();
        String currentTime = sf.format(c.getTime());
        if (tamp.compareTo(currentTime) > 0) {
            if (hash.equalsIgnoreCase(requestHash)){
                //校验成功
                user.setNick(nick);
                user.setEmail(email);
                user.setPassword(password);
                userService.registerUser(user);
                resultMap.put("status",200);
                resultMap.put("msg","注册成功！");
                return resultMap;
            }else {
                //验证码不正确，校验失败
                resultMap.put("status",500);
                resultMap.put("msg","验证码不正确，注册失败！");
                return resultMap;
            }
        } else {
            // 超时
            resultMap.put("status",503);
            resultMap.put("msg","验证码超时，注册失败！");
            return resultMap;
        }
    }


    /**
     * 昵称是否存在
     * @param nick
     * @return
     */
    @PostMapping("/checkNick")
    @ResponseBody
    public Map<String, Object> checkNick(@RequestParam(value = "nick") String nick) {
        Map<String, Object> resultMap = new HashMap<>();
        User user = userService.getUserByNick(nick);
        if(user==null){
            resultMap.put("valid",true);
            return resultMap;
        }else {
            resultMap.put("valid",false);
            return resultMap;
        }
    }

    /**
     * 邮箱地址是否存在
     * @param email
     * @return
     */
    @PostMapping("/checkEmail")
    @ResponseBody
    public Map<String, Object> checkEmail(@RequestParam(value = "email") String email){
        Map<String, Object> resultMap = new HashMap<>();
        User user = userService.getUserByEmail(email);
        if(user==null){
            resultMap.put("valid",true);
            return resultMap;
        }else {
            resultMap.put("valid",false);
            return resultMap;
        }
    }

}
