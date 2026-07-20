package com.yosmerry.pims.image.repository;

import com.yosmerry.pims.image.entity.ItemImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemImageRepository extends JpaRepository<ItemImage, Long> {

  boolean existsByInventoryItemCodeAndMarkForDeleteFalse(
      String inventoryItemCode);

  Optional<ItemImage> findByCodeAndMarkForDeleteFalse(String code);

  List<ItemImage>
      findAllByInventoryItemCodeAndMarkForDeleteFalseOrderByPrimaryDescCreatedDateAsc(
      String inventoryItemCode);

  List<ItemImage> findAllByInventoryItemCodeAndMarkForDeleteFalse(
      String inventoryItemCode);

  Optional<ItemImage>
      findFirstByInventoryItemCodeAndCodeNotAndMarkForDeleteFalseOrderByCreatedDateAsc(
          String inventoryItemCode,
          String excludedCode);
}
