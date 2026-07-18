package com.yosmerry.pims.auth.entity;

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
@Table(name = TableNames.REFRESH_TOKENS)
public class RefreshToken extends BaseEntity {

  @Column(nullable = false, unique = true, length = 20)
  private String code;

  @Column(name = "user_code", nullable = false, length = 20)
  private String userCode;

  @Column(name = "token_hash", nullable = false, unique = true, length = 255)
  private String tokenHash;

  @Column(name = "expires_date", nullable = false)
  private Long expiresDate;

  @Column(name = "revoked_date")
  private Long revokedDate;

  @Column(name = "replaced_by_code", length = 20)
  private String replacedByCode;

  @Column(name = "device_info", length = 255)
  private String deviceInfo;
}
