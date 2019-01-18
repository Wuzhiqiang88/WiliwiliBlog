package com.sao.config.shiro;

import at.pollux.thymeleaf.shiro.dialect.ShiroDialect;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ShiroConf {

    //在thymeleaf中使用shiro的自定义tag
    @Bean
    public ShiroDialect shiroDialect() {
        return new ShiroDialect();

    }
    //注入shiro过滤器
    @Bean("shiroFilterFactoryBean")
    public ShiroFilterFactoryBean shiroFilter(WebSecurityManager securityManager) {
        /*anon 匿名过滤器
        authc 如果继续操作，需要做对应的表单验证否则不能通过
        authcBasic 基本http验证过滤，如果不通过，跳转屋登录页面
        logout 登录退出过滤器
        noSessionCreation 没有session创建过滤器
        perms 权限过滤器
        port 端口过滤器，可以设置是否是指定端口如果不是跳转到登录页面
        rest http方法过滤器，可以指定如post不能进行访问等
        roles 角色过滤器，判断当前用户是否指定角色
        ssl	请求需要通过ssl，如果不是跳转回登录页
        user 如果访问一个已知用户，比如记住我功能，走这个过滤器*/
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        // 必须设置 SecurityManager
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        shiroFilterFactoryBean.setLoginUrl("/login");  // 如果不设置默认会自动寻找Web工程根目录下的"/login.jsp"页面
        Map<String, String> chains = new LinkedHashMap<>();
        chains.put("/logout", "logout");//登出
        shiroFilterFactoryBean.setSuccessUrl("/index");// 登录成功后要跳转的链接
        //chains.put("/login", "anon");//anon表示可以匿名访问
       // chains.put("/u/**", "authc");//表示需要认证，才能访问
        chains.put("/u/**", "user");//表示需要认证或记住我都能访问
        shiroFilterFactoryBean.setFilterChainDefinitionMap(chains);
        return shiroFilterFactoryBean;
    }


    //安全管理
    @Bean
    public WebSecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        // 设置realm.
        securityManager.setRealm(shiroRealm());

        securityManager.setRememberMeManager(rememberMeManager());
        return securityManager;
    }

    //会话管理器
    @Bean
    public SessionManager sessionManager() {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        sessionManager.setSessionIdUrlRewritingEnabled(false);
        sessionManager.setGlobalSessionTimeout(1 * 60 * 60 * 1000);//session过期时间
        sessionManager.setDeleteInvalidSessions(true);//是否删除过期session
        sessionManager.setSessionIdCookie(rememberMeCookie());
        return sessionManager;
    }

    //Realm，里面是自己实现的认证和授权业务逻辑
    @Bean
    public ShiroRealm shiroRealm() {
        ShiroRealm shiroRealm = new ShiroRealm();
        shiroRealm.setCredentialsMatcher(hashedCredentialsMatcher());
        return shiroRealm;
    }


    //密码管理
    @Bean
    public HashedCredentialsMatcher hashedCredentialsMatcher() {
        HashedCredentialsMatcher credentialsMatcher = new HashedCredentialsMatcher();
        credentialsMatcher.setHashAlgorithmName("md5"); //散列算法使用md5
        credentialsMatcher.setHashIterations(2);        //散列次数，2表示md5加密两次
        credentialsMatcher.setStoredCredentialsHexEncoded(true);
        return credentialsMatcher;
    }

    //cookie管理
    @Bean
    public SimpleCookie rememberMeCookie() {
        SimpleCookie cookie = new SimpleCookie("rememberMe");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(1 * 60 * 60);
        return cookie;
    }

    //记住我
    @Bean
    public CookieRememberMeManager rememberMeManager() {
        CookieRememberMeManager cookieRememberMeManager = new CookieRememberMeManager();
        cookieRememberMeManager.setCookie(rememberMeCookie());
        //这个地方有点坑，不是所有的base64编码都可以用，长度过大过小都不行，没搞明白，官网给出的要么0x开头十六进制，要么base64
        cookieRememberMeManager.setCipherKey(Base64.decode("4AvVhmFLUs0KTA3Kprsdag=="));
        return cookieRememberMeManager;
    }

}
