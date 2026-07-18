package com.yosmerry.pims.user.repository;

import com.yosmerry.pims.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  boolean existsByEmailIgnoreCaseAndMarkForDeleteFalse(String email);
}
