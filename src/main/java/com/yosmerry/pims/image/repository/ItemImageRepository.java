package com.yosmerry.pims.image.repository;

import com.yosmerry.pims.image.entity.ItemImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemImageRepository extends JpaRepository<ItemImage, Long> {

  boolean existsByInventoryItemCodeAndMarkForDeleteFalse(
      String inventoryItemCode);

  Optional<ItemImage> findByCodeAndMarkForDeleteFalse(String code);
}
