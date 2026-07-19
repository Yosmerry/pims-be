package com.yosmerry.pims.category.repository;

import com.yosmerry.pims.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  List<Category> findAllByUserCodeAndMarkForDeleteFalseOrderByNameAsc(
      String userCode);

  Optional<Category> findByCodeAndUserCodeAndMarkForDeleteFalse(
      String code,
      String userCode);
}
