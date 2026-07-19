package com.yosmerry.pims.location.repository;

import com.yosmerry.pims.location.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {

  List<Location> findAllByUserCodeAndMarkForDeleteFalseOrderByNameAsc(
      String userCode);

  Optional<Location> findByCodeAndUserCodeAndMarkForDeleteFalse(
      String code,
      String userCode);
}
