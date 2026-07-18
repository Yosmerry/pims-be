package com.yosmerry.pims.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

  @Id
  private Long id;

  @Column(name = "created_date", nullable = false)
  private Long createdDate;

  @Column(name = "created_by", nullable = false, length = 100)
  private String createdBy;

  @Column(name = "updated_date", nullable = false)
  private Long updatedDate;

  @Column(name = "updated_by", nullable = false, length = 100)
  private String updatedBy;

  @Version
  @Column(nullable = false)
  private Long version = 0L;

  @Column(name = "mark_for_delete", nullable = false)
  private Boolean markForDelete = false;
}
