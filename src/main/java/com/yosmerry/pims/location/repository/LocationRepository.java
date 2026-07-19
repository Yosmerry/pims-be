package com.yosmerry.pims.location.repository;

import com.yosmerry.pims.location.entity.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {

  Page<Location> findAllByUserCodeAndMarkForDeleteFalse(
      String userCode,
      Pageable pageable);

  Optional<Location> findByCodeAndUserCodeAndMarkForDeleteFalse(
      String code,
      String userCode);
}
