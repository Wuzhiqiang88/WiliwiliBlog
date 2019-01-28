package com.sao.repository;

import com.sao.domain.User;
import org.springframework.data.repository.CrudRepository;

/**
 * 用户仓库.
 */
public interface UserRepository extends CrudRepository<User,Long> {

    User findUserByEmail(String email);

    User findUserByNick(String nick);

    User findUserByPhone(String phone);

    User findUserByOpenId(String openId);

}
