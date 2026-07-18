package com.yosmerry.pims.user.repository;

import com.yosmerry.pims.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

  boolean existsByEmailIgnoreCaseAndMarkForDeleteFalse(String email);

  Optional<User> findByEmailIgnoreCaseAndMarkForDeleteFalse(String email);

  Optional<User> findByCodeAndMarkForDeleteFalse(String code);
}
