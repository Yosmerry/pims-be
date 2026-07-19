package com.yosmerry.pims.inventory.service;

import com.yosmerry.pims.auth.security.CurrentUserProvider;
import com.yosmerry.pims.category.entity.Category;
import com.yosmerry.pims.category.repository.CategoryRepository;
import com.yosmerry.pims.common.constant.ErrorCodes;
import com.yosmerry.pims.common.enums.ActiveStatus;
import com.yosmerry.pims.common.enums.CodeType;
import com.yosmerry.pims.common.exception.ApiResourceNotFoundException;
import com.yosmerry.pims.common.exception.ApiValidationException;
import com.yosmerry.pims.common.response.PageResponse;
import com.yosmerry.pims.common.util.CodeGenerator;
import com.yosmerry.pims.inventory.dto.CreateInventoryItemRequest;
import com.yosmerry.pims.inventory.dto.InventoryItemFilter;
import com.yosmerry.pims.inventory.dto.InventoryItemResponse;
import com.yosmerry.pims.inventory.dto.UpdateInventoryItemRequest;
import com.yosmerry.pims.inventory.entity.InventoryItem;
import com.yosmerry.pims.inventory.enums.InventoryCondition;
import com.yosmerry.pims.inventory.enums.InventoryStatus;
import com.yosmerry.pims.inventory.repository.InventoryItemRepository;
import com.yosmerry.pims.inventory.repository.InventoryItemSpecifications;
import com.yosmerry.pims.location.entity.Location;
import com.yosmerry.pims.location.repository.LocationRepository;
import com.yosmerry.pims.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InventoryService {

  private static final String INVENTORY_ITEM_FIELD = "inventoryItem";
  private static final String CATEGORY_CODE_FIELD = "categoryCode";
  private static final String LOCATION_CODE_FIELD = "locationCode";

  private final InventoryItemRepository inventoryItemRepository;
  private final CategoryRepository categoryRepository;
  private final LocationRepository locationRepository;
  private final CurrentUserProvider currentUserProvider;
  private final CodeGenerator codeGenerator;

  @Transactional
  public InventoryItemResponse create(CreateInventoryItemRequest request) {
    User user = currentUserProvider.requireActiveUser();
    String categoryCode = request.categoryCode().trim();
    String locationCode = normalizeOptionalText(request.locationCode());

    requireActiveCategory(categoryCode, user.getCode());
    requireActiveLocation(locationCode, user.getCode());

    InventoryItem inventoryItem = new InventoryItem();
    inventoryItem.setCode(codeGenerator.next(CodeType.INVENTORY_ITEM));
    inventoryItem.setUserCode(user.getCode());
    inventoryItem.setCategoryCode(categoryCode);
    inventoryItem.setLocationCode(locationCode);
    applyCreateRequest(inventoryItem, request);
    inventoryItem.setStatus(InventoryStatus.OWNED);
    inventoryItem.setCreatedBy(user.getEmail());
    inventoryItem.setUpdatedBy(user.getEmail());

    return toResponse(inventoryItemRepository.save(inventoryItem));
  }

  @Transactional(readOnly = true)
  public PageResponse<InventoryItemResponse> findAll(InventoryItemFilter filter) {
    User user = currentUserProvider.requireActiveUser();
    Sort sort = resolveSort(filter.getSortBy());
    Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

    return PageResponse.from(inventoryItemRepository
        .findAll(InventoryItemSpecifications.from(user.getCode(), filter), pageable)
        .map(this::toResponse));
  }

  @Transactional(readOnly = true)
  public InventoryItemResponse findByCode(String code) {
    User user = currentUserProvider.requireActiveUser();
    return toResponse(requireInventoryItem(code, user.getCode()));
  }

  @Transactional
  public InventoryItemResponse update(
      String code,
      UpdateInventoryItemRequest request) {
    User user = currentUserProvider.requireActiveUser();
    InventoryItem inventoryItem = requireInventoryItem(code, user.getCode());
    String categoryCode = request.categoryCode().trim();
    String locationCode = normalizeOptionalText(request.locationCode());

    requireActiveCategory(categoryCode, user.getCode());
    requireActiveLocation(locationCode, user.getCode());

    inventoryItem.setCategoryCode(categoryCode);
    inventoryItem.setLocationCode(locationCode);
    inventoryItem.setName(request.name().trim());
    inventoryItem.setDescription(normalizeOptionalText(request.description()));
    inventoryItem.setQuantity(request.quantity());
    inventoryItem.setPurchasePrice(request.purchasePrice());
    inventoryItem.setPurchaseDate(request.purchaseDate());
    inventoryItem.setCondition(InventoryCondition.valueOf(request.condition()));
    inventoryItem.setStatus(InventoryStatus.valueOf(request.status()));
    inventoryItem.setNotes(normalizeOptionalText(request.notes()));
    inventoryItem.setUpdatedBy(user.getEmail());

    return toResponse(inventoryItemRepository.save(inventoryItem));
  }

  @Transactional
  public void delete(String code) {
    User user = currentUserProvider.requireActiveUser();
    InventoryItem inventoryItem = requireInventoryItem(code, user.getCode());

    inventoryItem.setMarkForDelete(true);
    inventoryItem.setUpdatedBy(user.getEmail());
    inventoryItemRepository.save(inventoryItem);
  }

  private void applyCreateRequest(
      InventoryItem inventoryItem,
      CreateInventoryItemRequest request) {
    inventoryItem.setName(request.name().trim());
    inventoryItem.setDescription(normalizeOptionalText(request.description()));
    inventoryItem.setQuantity(request.quantity());
    inventoryItem.setPurchasePrice(request.purchasePrice());
    inventoryItem.setPurchaseDate(request.purchaseDate());
    inventoryItem.setCondition(InventoryCondition.valueOf(request.condition()));
    inventoryItem.setNotes(normalizeOptionalText(request.notes()));
  }

  private InventoryItem requireInventoryItem(String code, String userCode) {
    return inventoryItemRepository
        .findByCodeAndUserCodeAndMarkForDeleteFalse(code, userCode)
        .orElseThrow(() -> new ApiResourceNotFoundException(INVENTORY_ITEM_FIELD));
  }

  private void requireActiveCategory(String categoryCode, String userCode) {
    Category category = categoryRepository
        .findByCodeAndUserCodeAndMarkForDeleteFalse(categoryCode, userCode)
        .orElseThrow(() -> invalidRelation(CATEGORY_CODE_FIELD));

    if (category.getStatus() != ActiveStatus.ACTIVE) {
      throw invalidRelation(CATEGORY_CODE_FIELD);
    }
  }

  private void requireActiveLocation(String locationCode, String userCode) {
    if (locationCode == null) {
      return;
    }

    Location location = locationRepository
        .findByCodeAndUserCodeAndMarkForDeleteFalse(locationCode, userCode)
        .orElseThrow(() -> invalidRelation(LOCATION_CODE_FIELD));

    if (location.getStatus() != ActiveStatus.ACTIVE) {
      throw invalidRelation(LOCATION_CODE_FIELD);
    }
  }

  private ApiValidationException invalidRelation(String field) {
    return new ApiValidationException(Map.of(field, List.of(ErrorCodes.INVALID)));
  }

  private Sort resolveSort(String sortBy) {
    String[] parts = sortBy.split(":");
    Sort.Direction direction = Sort.Direction.ASC;
    if ("desc".equals(parts[1])) {
      direction = Sort.Direction.DESC;
    }
    return Sort.by(direction, parts[0]);
  }

  private String normalizeOptionalText(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }

  private InventoryItemResponse toResponse(InventoryItem inventoryItem) {
    return InventoryItemResponse.builder()
        .code(inventoryItem.getCode())
        .categoryCode(inventoryItem.getCategoryCode())
        .locationCode(inventoryItem.getLocationCode())
        .name(inventoryItem.getName())
        .description(inventoryItem.getDescription())
        .quantity(inventoryItem.getQuantity())
        .purchasePrice(inventoryItem.getPurchasePrice())
        .purchaseDate(inventoryItem.getPurchaseDate())
        .condition(inventoryItem.getCondition().name())
        .status(inventoryItem.getStatus().name())
        .notes(inventoryItem.getNotes())
        .createdDate(inventoryItem.getCreatedDate())
        .updatedDate(inventoryItem.getUpdatedDate())
        .build();
  }
}
