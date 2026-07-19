package com.yosmerry.pims.inventory.entity;

import com.yosmerry.pims.common.constant.TableNames;
import com.yosmerry.pims.common.entity.BaseEntity;
import com.yosmerry.pims.inventory.enums.InventoryCondition;
import com.yosmerry.pims.inventory.enums.InventoryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = TableNames.INVENTORY_ITEMS)
public class InventoryItem extends BaseEntity {

  @Column(nullable = false, unique = true, length = 20)
  private String code;

  @Column(name = "user_code", nullable = false, length = 20)
  private String userCode;

  @Column(name = "category_code", nullable = false, length = 20)
  private String categoryCode;

  @Column(name = "location_code", length = 20)
  private String locationCode;

  @Column(nullable = false, length = 150)
  private String name;

  @Column(length = 1000)
  private String description;

  @Column(nullable = false)
  private Integer quantity;

  @Column(name = "purchase_price", precision = 19, scale = 2)
  private BigDecimal purchasePrice;

  @Column(name = "purchase_date")
  private LocalDate purchaseDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private InventoryCondition condition;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private InventoryStatus status;

  @Column(length = 1000)
  private String notes;
}
