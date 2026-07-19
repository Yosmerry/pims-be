package com.yosmerry.pims.inventory.repository;

import com.yosmerry.pims.inventory.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface InventoryItemRepository
    extends JpaRepository<InventoryItem, Long>,
    JpaSpecificationExecutor<InventoryItem> {

  Optional<InventoryItem> findByCodeAndUserCodeAndMarkForDeleteFalse(
      String code,
      String userCode);
}
