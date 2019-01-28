package com.sao.controller;


import com.google.gson.Gson;
import com.sao.domain.User;
import com.sao.model.dto.QQDTO;
import com.sao.model.dto.QQOpenidDTO;
import com.sao.properties.OAuthProperties;
import com.sao.service.UserService;
import com.sao.util.EmailUtils;
import com.sao.util.HttpsUtils;
import com.sao.util.MD5Utils;
import com.ucpaas.restDemo.client.JsonReqClient;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;

import org.apache.shiro.web.util.SavedRequest;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;

import javax.mail.MessagingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class MainController {

    @Autowired
    private UserService userService;
    @Autowired
    private OAuthProperties oauth;

    private static final String KEY = "wiliwili"; // KEY为自定义秘钥

    private Logger logger = LoggerFactory.getLogger(getClass());

    @GetMapping("/")
    public String root(){
        return "index";
    }
    @GetMapping("/index")
    public String index(){
        return "redirect:/";
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
     * @return
     */
    @PostMapping("/login")
    @ResponseBody
    public Map<String, Object> login( HttpServletRequest request,String username, String password, boolean rememberMe){
        System.out.println(username+" "+password+" "+rememberMe);
        Map<String, Object> resultMap = new HashMap<>();
        //判断用户名和密码为空
        if (StringUtils.isEmpty(username)||StringUtils.isEmpty(password)){
            resultMap.put("status",500);
            resultMap.put("msg","用户名和密码不能为空！");
            return resultMap;
        }
        UsernamePasswordToken token = new UsernamePasswordToken(username,password,rememberMe);

        //开始登录
        try {
            SecurityUtils.getSubject().login(token);
            resultMap.put("status",200);
            resultMap.put("msg","登录成功！");
            SavedRequest savedRequest = WebUtils.getSavedRequest(request);
            String url = "/";
            if (savedRequest != null) {
                url = savedRequest.getRequestUrl();
            }else {
                url="/index";
            }
            resultMap.put("url",url);
            return resultMap;
        }catch (UnknownAccountException e){
            //用户名不存在
            resultMap.put("status",500);
            resultMap.put("msg","用户名或密码错误！");
            return resultMap;
        }catch (IncorrectCredentialsException e){
            //密码错误
            resultMap.put("status",500);
            resultMap.put("msg","用户名或密码错误！");
            return resultMap;
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
    public Map<String, Object> registerByEmail(@RequestBody Map<String,Object> requestMap,User user,HttpServletRequest request) {
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
                SavedRequest savedRequest = WebUtils.getSavedRequest(request);
                String url = "/";
                if (savedRequest != null) {
                    url = savedRequest.getRequestUrl();
                }else {
                    url="/index";
                }
                resultMap.put("url",url);
               // System.out.println(url);
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
     * 发送手机验证码
     * @param requestMap
     * @return
     */
    @RequestMapping(value = "/sendSMS", method = RequestMethod.POST, headers = "Accept=application/json")
    @ResponseBody
    public Map<String, Object> sendSMS(@RequestBody Map<String,Object> requestMap){
        System.out.println("sendSMS---");
        Map<String, Object> resultMap = new HashMap<>();
        String phone = requestMap.get("phone").toString();
        String randomNum = String.valueOf(new Random().nextInt(899999) + 100000);//生成验证码

        String sid = "e5467d928cb364098ba9dffc0faa9212";//用户的账号唯一标识“Account Sid”
        String token = "c381f80d3730d30f25ce203e844a2740";//用户密钥“Auth Token”
        String appid = "5e0866a7ca40460a8ab3fd0408515f71";//创建应用时系统分配的唯一标示
        String templateid = "427742";//可在后台短信产品→选择接入的应用→短信模板-模板ID，查看该模板ID
        String param = randomNum;//模板中的替换参数（验证码）
        String mobile = phone;//接收的单个手机号，暂仅支持国内号码
        String uid = "";//用户透传ID，随状态报告返回

        try {
            String result=new JsonReqClient().sendSms(sid, token, appid, templateid, param, mobile, uid);
            //System.out.println("Response content is: " + result);
        } catch (Exception e) {
            return resultMap;
        }
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, 5);
        String currentTime = sf.format(c.getTime());// 生成5分钟后时间，用户校验是否过期
        String hash =  MD5Utils.getMD5Code(KEY + "@" + currentTime + "@" + randomNum);//生成MD5值
        resultMap.put("hash", hash);
        resultMap.put("tamp", currentTime);
        return resultMap; //将hash值和tamp时间返回给前端
    }
    /**
     * 用手机号注册
     * @param requestMap
     * @return
     */
    @RequestMapping(value = "/registerByPhone", method = RequestMethod.POST, headers = "Accept=application/json")
    @ResponseBody
    public Map<String, Object> registerByPhone(@RequestBody Map<String,Object> requestMap,User user,HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<>();
        String requestHash = requestMap.get("hash").toString();
        String tamp = requestMap.get("tamp").toString();
        String inputCode = requestMap.get("inputCode").toString();
        String nick=requestMap.get("nick").toString();
        String phone=requestMap.get("phone").toString();
        String password=requestMap.get("password").toString();
        String hash = MD5Utils.getMD5Code(KEY + "@" + tamp + "@" + inputCode);
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
        Calendar c = Calendar.getInstance();
        String currentTime = sf.format(c.getTime());
        if (tamp.compareTo(currentTime) > 0) {
            if (hash.equalsIgnoreCase(requestHash)){
                //校验成功
                user.setNick(nick);
                user.setPhone(phone);
                user.setPassword(password);
                userService.registerUser(user);
                resultMap.put("status",200);
                resultMap.put("msg","注册成功！");
                SavedRequest savedRequest = WebUtils.getSavedRequest(request);
                String url = "/";
                if (savedRequest != null) {
                    url = savedRequest.getRequestUrl();
                }else {
                    url="/index";
                }
                resultMap.put("url",url);
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

    /**
     * 手机号是否存在
     * @param phone
     * @return
     */
    @PostMapping("/checkPhone")
    @ResponseBody
    public Map<String, Object> checkPhone(@RequestParam(value = "phone") String phone){
        Map<String, Object> resultMap = new HashMap<>();
        User user = userService.getUserByPhone(phone);
        if(user==null){
            resultMap.put("valid",true);
            return resultMap;
        }else {
            resultMap.put("valid",false);
            return resultMap;
        }
    }

    /**
     * 获取已登录的用户信息
     * @return
     */
    @PostMapping("/getUserInfo")
    @ResponseBody
    public User getUserInfo(){
        User user = (User)SecurityUtils.getSubject().getPrincipal();
        return user;
    }

    //QQ登陆对外接口，只需将该接口放置html的a标签href中即可
    @GetMapping("/login/qq")
    public void loginQQ(HttpServletResponse response) {
        try {
            response.sendRedirect(oauth.getQQ().getCode_callback_uri() + //获取code码地址
                    "?client_id=" + oauth.getQQ().getClient_id()//appid
                    + "&state=" + UUID.randomUUID() + //这个说是防攻击的，就给个随机uuid吧
                    "&redirect_uri=" + oauth.getQQ().getRedirect_uri() +//这个很重要，这个是回调地址，即就收腾讯返回的code码
                    "&response_type=code");//授权模式，授权码模式
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    //接收回调地址带过来的code码
    @GetMapping("/authorize/qq")
    public String authorizeQQ(HttpServletRequest request, Map<String, String> msg, String code) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("code", code);
        params.put("grant_type", "authorization_code");
        params.put("redirect_uri", oauth.getQQ().getRedirect_uri());
        params.put("client_id", oauth.getQQ().getClient_id());
        params.put("client_secret", oauth.getQQ().getClient_secret());
        //获取access_token如：access_token=9724892714FDF1E3ED5A4C6D074AF9CB&expires_in=7776000&refresh_token=9E0DE422742ACCAB629A54B3BFEC61FF
        String result = HttpsUtils.doGet(oauth.getQQ().getAccess_token_callback_uri(), params);
        //对拿到的数据进行切割字符串
        String[] strings = result.split("&");
        //切割好后放进map
        Map<String, String> reulsts = new HashMap<>();
        for (String str : strings) {
            String[] split = str.split("=");
            if (split.length > 1) {
                reulsts.put(split[0], split[1]);
            }
        }
        //到这里access_token已经处理好了
        //下一步获取openid，只有拿到openid才能拿到用户信息
        String openidContent = HttpsUtils.doGet(oauth.getQQ().getOpenid_callback_uri() + "?access_token=" + reulsts.get("access_token"));
        //接下来对openid进行处理
        //截取需要的那部分json字符串
        String openid = openidContent.substring(openidContent.indexOf("{"), openidContent.indexOf("}") + 1);
        Gson gson = new Gson();
        //将返回的openid转换成DTO
        QQOpenidDTO qqOpenidDTO = gson.fromJson(openid, QQOpenidDTO.class);

        //接下来说说获取用户信息部分
        //登陆的时候去数据库查询用户数据对于openid是存在，如果存在的话，就不用拿openid获取用户信息了，而是直接从数据库拿用户数据直接认证用户，
        // 否则就拿openid去腾讯服务器获取用户信息，并存入数据库，再去认证用户
        //下面关于怎么获取用户信息，并登陆
        params.clear();
        params.put("access_token", reulsts.get("access_token"));//设置access_token
        params.put("openid", qqOpenidDTO.getOpenid());//设置openid
        params.put("oauth_consumer_key", qqOpenidDTO.getClient_id());//设置appid
        //获取用户信息
        String userInfo = HttpsUtils.doGet(oauth.getQQ().getUser_info_callback_uri(), params);
        QQDTO qqDTO = gson.fromJson(userInfo,QQDTO.class);
        //判断是否第一次登录

        String nick=qqDTO.getNickname();
        String openId=qqOpenidDTO.getOpenid();
        String avatar=qqDTO.getFigureurl_qq_1();
        String sex=qqDTO.getGender();
        User getUser=userService.getUserByOpenId(openId);
        if(getUser==null){
           User user=new User(nick,openId,"","",sex,avatar,"","",openId);
           User user1=userService.registerQQUser(user);
            System.out.println(user1);
            try {
                //这里拿openid作为用户名，openid作为密码（正常情况下，在开发时候用openid作为用户名，再自己定义个密码就可以了）
                SecurityUtils.getSubject().login(new UsernamePasswordToken(user1.getOpenId(), user1.getOpenId()));
            }catch (Exception e){
                msg.put("msg","第三方登陆失败,请联系管理！");
                logger.error(e.getMessage());
                return "login";
            }
        }else {
            System.out.println(getUser);
            SecurityUtils.getSubject().login(new UsernamePasswordToken(getUser.getOpenId(),getUser.getOpenId() ));
        }
        SavedRequest savedRequest = WebUtils.getSavedRequest(request);
        String url = "/";
        if (savedRequest != null) {
            url = savedRequest.getRequestUrl();
        }else {
            url="/index";
        }
        //System.out.println(savedRequest.getRequestUrl());
        return "redirect:"+url;
    }

}
