package com.sao.config.shiro;

import com.sao.domain.User;
import com.sao.service.UserService;
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
        System.out.println(username);
        if(userService.getUserByEmail(username)==null&&userService.getUserByPhone(username)==null){
            throw new UnknownAccountException();
        }
        User user =null;
        if(userService.getUserByEmail(username)!=null){
            user=userService.getUserByEmail(username);
            System.out.println(user.toString());

        }
        if(userService.getUserByPhone(username)!=null){
            user=userService.getUserByPhone(username);

        }
        return  new SimpleAuthenticationInfo(user, user.getPassword(), ByteSource.Util.bytes(user.getSalt()), getName());

    }
}
