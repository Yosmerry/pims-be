package com.yosmerry.pims.user.repository;

import com.yosmerry.pims.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmailIgnoreCaseAndMarkForDeleteFalse(String email);

    @Query(value = "SELECT 'USR' || LPAD(nextval('users_code_seq')::TEXT, 6, '0')", nativeQuery = true)
    String nextCode();
}
