package com.yosmerry.pims.image.entity;

import com.yosmerry.pims.common.constant.TableNames;
import com.yosmerry.pims.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = TableNames.ITEM_IMAGES)
public class ItemImage extends BaseEntity {

  @Column(nullable = false, unique = true, length = 20)
  private String code;

  @Column(name = "inventory_item_code", nullable = false, length = 20)
  private String inventoryItemCode;

  @Column(name = "original_filename", nullable = false, length = 255)
  private String originalFilename;

  @Column(name = "stored_filename", nullable = false, length = 255)
  private String storedFilename;

  @Column(name = "content_type", nullable = false, length = 100)
  private String contentType;

  @Column(name = "file_size", nullable = false)
  private Long fileSize;

  @Column(name = "storage_path", nullable = false, length = 500)
  private String storagePath;

  @Column(name = "is_primary", nullable = false)
  private Boolean primary;
}
