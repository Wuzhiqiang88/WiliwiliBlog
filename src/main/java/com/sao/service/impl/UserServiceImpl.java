package com.sao.service.impl;


import com.sao.domain.User;
import com.sao.repository.UserRepository;
import com.sao.service.UserService;
import com.sao.util.MD5Utils;
import com.sao.util.SaltUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Transactional
    @Override
    public User saveOrUpateUser(User user) {
        return userRepository.save(user);
    }

    /**
     * 注册用户
     * @param user
     * @return
     */
    @Transactional
    @Override
    public User registerUser(User user) {
        String salt=SaltUtils.getSalt();
        String encryptedPassword=MD5Utils.getEncryptedPassword(salt,user.getPassword());
        user.setPassword(encryptedPassword);
        user.setSalt(salt);
        user.setSex("男");
        user.setAvatar("/image/avatar-defualt.jpg");
        return userRepository.save(user);
    }

    @Transactional
    @Override
    public void removeUser(Long id) {

    }

    @Override
    public User getUserById(Long id) {
        return null;
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    @Override
    public User getUserByNick(String nick) {
        return userRepository.findUserByNick(nick);
    }

    @Override
    public User getUserByPhone(String phone) {
        return userRepository.findUserByPhone(phone);
    }


}
