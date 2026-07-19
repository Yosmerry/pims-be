package com.yosmerry.pims.inventory.repository;

import com.yosmerry.pims.inventory.dto.InventoryItemFilter;
import com.yosmerry.pims.inventory.entity.InventoryItem;
import com.yosmerry.pims.inventory.enums.InventoryCondition;
import com.yosmerry.pims.inventory.enums.InventoryStatus;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InventoryItemSpecifications {

  public static Specification<InventoryItem> from(
      String userCode,
      InventoryItemFilter filter) {
    return (root, criteriaQuery, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();
      predicates.add(criteriaBuilder.equal(root.get("userCode"), userCode));
      predicates.add(criteriaBuilder.isFalse(root.get("markForDelete")));

      if (hasText(filter.getSearch())) {
        String search = "%"
            + filter.getSearch().trim().toLowerCase(Locale.ROOT)
            + "%";
        predicates.add(criteriaBuilder.or(
            criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), search),
            criteriaBuilder.like(
                criteriaBuilder.lower(root.get("description")),
                search),
            criteriaBuilder.like(criteriaBuilder.lower(root.get("notes")), search)));
      }

      if (hasText(filter.getCategoryCode())) {
        predicates.add(criteriaBuilder.equal(
            root.get("categoryCode"),
            filter.getCategoryCode().trim()));
      }

      if (hasText(filter.getLocationCode())) {
        predicates.add(criteriaBuilder.equal(
            root.get("locationCode"),
            filter.getLocationCode().trim()));
      }

      if (hasText(filter.getCondition())) {
        predicates.add(criteriaBuilder.equal(
            root.get("condition"),
            InventoryCondition.valueOf(filter.getCondition())));
      }

      if (hasText(filter.getStatus())) {
        predicates.add(criteriaBuilder.equal(
            root.get("status"),
            InventoryStatus.valueOf(filter.getStatus())));
      }

      return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
    };
  }

  private static boolean hasText(String value) {
    if (value == null) {
      return false;
    }
    return !value.isBlank();
  }
}
