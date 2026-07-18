package com.yosmerry.pims.common.util;

import com.yosmerry.pims.common.enums.CodeType;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostgresCodeGenerator implements CodeGenerator {

  private final EntityManager entityManager;

  @Override
  public String next(CodeType type) {
    long value = ((Number) entityManager.createNativeQuery(
        "SELECT nextval(CAST(:sequenceName AS regclass))")
        .setParameter("sequenceName", type.sequenceName())
        .getSingleResult()).longValue();

    return "%s%06d".formatted(type.prefix(), value);
  }
}
