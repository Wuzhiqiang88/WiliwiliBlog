package com.sao.config.shiro;

import com.sao.domain.User;
import com.sao.service.UserService;
import com.sao.util.MD5Utils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;

public class ShiroRealm extends AuthorizingRealm {

    @Autowired
    private UserService userService;

    //执行授权逻辑
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        return null;
    }

    //执行认证逻辑
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        //获取用户名
        String username = (String) token.getPrincipal();
        System.out.println("认证的用户名： "+username);

        User emailUser=userService.getUserByEmail(username);
        User phoneUser=userService.getUserByPhone(username);
        User openIdUser=userService.getUserByOpenId(username);
        if(emailUser==null&&phoneUser==null&&openIdUser==null){
            System.out.println("用户名错误");
            throw new UnknownAccountException();
        }
        User user =null;
        if(emailUser!=null){
            user=emailUser;
            System.out.println(user.toString());
        }
        if(phoneUser!=null){
            user=phoneUser;
        }
        if(openIdUser!=null){
            user=openIdUser;
            user.setPassword( MD5Utils.getEncryptedPassword(user.getSalt(),user.getOpenId()));
            return  new SimpleAuthenticationInfo(user, user.getPassword() ,ByteSource.Util.bytes(user.getSalt()), getName());
        }
        return  new SimpleAuthenticationInfo(user, user.getPassword(), ByteSource.Util.bytes(user.getSalt()), getName());

    }
}
