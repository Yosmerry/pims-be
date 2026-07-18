package com.yosmerry.pims.auth.repository;

import com.yosmerry.pims.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  Optional<RefreshToken> findByTokenHashAndMarkForDeleteFalse(String tokenHash);
}
