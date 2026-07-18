package com.yosmerry.pims.common.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BaseEntityTest {

  @Test
  void shouldSetCreatedAndUpdatedDatesBeforeInsert() {
    TestEntity entity = new TestEntity();

    entity.onCreate();

    assertThat(entity.getId()).isNotNull();
    assertThat(entity.getCreatedDate()).isNotNull();
    assertThat(entity.getUpdatedDate()).isEqualTo(entity.getCreatedDate());
  }

  @Test
  void shouldSetUpdatedDateBeforeUpdate() {
    TestEntity entity = new TestEntity();
    entity.setCreatedDate(1L);
    entity.setUpdatedDate(1L);

    entity.onUpdate();

    assertThat(entity.getCreatedDate()).isEqualTo(1L);
    assertThat(entity.getUpdatedDate()).isGreaterThan(1L);
  }

  private static final class TestEntity extends BaseEntity {
  }
}
