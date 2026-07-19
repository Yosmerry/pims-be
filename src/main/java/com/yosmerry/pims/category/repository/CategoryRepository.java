package com.yosmerry.pims.category.repository;

import com.yosmerry.pims.category.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  Page<Category> findAllByUserCodeAndMarkForDeleteFalse(
      String userCode,
      Pageable pageable);

  Optional<Category> findByCodeAndUserCodeAndMarkForDeleteFalse(
      String code,
      String userCode);
}
