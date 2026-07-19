package com.yosmerry.pims.location.entity;

import com.yosmerry.pims.common.constant.TableNames;
import com.yosmerry.pims.common.entity.BaseEntity;
import com.yosmerry.pims.common.enums.ActiveStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = TableNames.LOCATIONS)
public class Location extends BaseEntity {

  @Column(nullable = false, unique = true, length = 20)
  private String code;

  @Column(name = "user_code", nullable = false, length = 20)
  private String userCode;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(length = 500)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ActiveStatus status;
}
