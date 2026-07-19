package com.yosmerry.pims.inventory.service;

import com.yosmerry.pims.auth.security.CurrentUserProvider;
import com.yosmerry.pims.category.entity.Category;
import com.yosmerry.pims.category.repository.CategoryRepository;
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
import com.yosmerry.pims.location.entity.Location;
import com.yosmerry.pims.location.repository.LocationRepository;
import com.yosmerry.pims.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

  @Mock
  private InventoryItemRepository inventoryItemRepository;

  @Mock
  private CategoryRepository categoryRepository;

  @Mock
  private LocationRepository locationRepository;

  @Mock
  private CurrentUserProvider currentUserProvider;

  @Mock
  private CodeGenerator codeGenerator;

  private InventoryService inventoryService;

  @BeforeEach
  void setUp() {
    inventoryService = new InventoryService(
        inventoryItemRepository,
        categoryRepository,
        locationRepository,
        currentUserProvider,
        codeGenerator);
  }

  @Test
  void shouldCreateInventoryItemForCurrentUser() {
    User user = currentUser();
    when(currentUserProvider.requireActiveUser()).thenReturn(user);
    when(categoryRepository.findByCodeAndUserCodeAndMarkForDeleteFalse(
        "CAT000001",
        "USR000001"))
        .thenReturn(Optional.of(category()));
    when(locationRepository.findByCodeAndUserCodeAndMarkForDeleteFalse(
        "LOC000001",
        "USR000001"))
        .thenReturn(Optional.of(location()));
    when(codeGenerator.next(CodeType.INVENTORY_ITEM)).thenReturn("ITM000001");
    when(inventoryItemRepository.save(any(InventoryItem.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    InventoryItemResponse response = inventoryService.create(createRequest());

    assertThat(response.code()).isEqualTo("ITM000001");
    assertThat(response.name()).isEqualTo("MacBook Pro");
    assertThat(response.status()).isEqualTo("OWNED");

    ArgumentCaptor<InventoryItem> itemCaptor =
        ArgumentCaptor.forClass(InventoryItem.class);
    verify(inventoryItemRepository).save(itemCaptor.capture());
    assertThat(itemCaptor.getValue().getUserCode()).isEqualTo("USR000001");
    assertThat(itemCaptor.getValue().getCreatedBy()).isEqualTo("yos@example.com");
  }

  @Test
  void shouldRejectCategoryNotOwnedByCurrentUser() {
    when(currentUserProvider.requireActiveUser()).thenReturn(currentUser());
    when(categoryRepository.findByCodeAndUserCodeAndMarkForDeleteFalse(
        "CAT000001",
        "USR000001"))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> inventoryService.create(createRequest()))
        .isInstanceOf(ApiValidationException.class)
        .satisfies(exception -> assertThat(
            ((ApiValidationException) exception).getErrors())
            .containsEntry("categoryCode", List.of("Invalid")));
  }

  @Test
  void shouldRejectInactiveLocation() {
    Location location = location();
    location.setStatus(ActiveStatus.INACTIVE);
    when(currentUserProvider.requireActiveUser()).thenReturn(currentUser());
    when(categoryRepository.findByCodeAndUserCodeAndMarkForDeleteFalse(
        "CAT000001",
        "USR000001"))
        .thenReturn(Optional.of(category()));
    when(locationRepository.findByCodeAndUserCodeAndMarkForDeleteFalse(
        "LOC000001",
        "USR000001"))
        .thenReturn(Optional.of(location));

    assertThatThrownBy(() -> inventoryService.create(createRequest()))
        .isInstanceOf(ApiValidationException.class)
        .satisfies(exception -> assertThat(
            ((ApiValidationException) exception).getErrors())
            .containsEntry("locationCode", List.of("Invalid")));
  }

  @Test
  void shouldFindFilteredInventoryItemsForCurrentUser() {
    InventoryItemFilter filter = new InventoryItemFilter();
    filter.setSearch("macbook");
    filter.setSortBy("name:asc");
    filter.setPage(2);
    filter.setSize(10);
    when(currentUserProvider.requireActiveUser()).thenReturn(currentUser());
    when(inventoryItemRepository.findAll(
        any(Specification.class),
        any(Pageable.class)))
        .thenReturn(new PageImpl<>(
            List.of(inventoryItem()),
            PageRequest.of(2, 10),
            25));

    PageResponse<InventoryItemResponse> response = inventoryService.findAll(filter);

    assertThat(response.content()).hasSize(1);
    assertThat(response.content().getFirst().code()).isEqualTo("ITM000001");
    assertThat(response.page()).isEqualTo(2);
    assertThat(response.totalPages()).isEqualTo(3);

    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    verify(inventoryItemRepository).findAll(
        any(Specification.class),
        pageableCaptor.capture());
    assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(2);
    assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);
    assertThat(pageableCaptor.getValue().getSort().getOrderFor("name").isAscending())
        .isTrue();
  }

  @Test
  void shouldUpdateInventoryItem() {
    InventoryItem inventoryItem = inventoryItem();
    when(currentUserProvider.requireActiveUser()).thenReturn(currentUser());
    when(inventoryItemRepository.findByCodeAndUserCodeAndMarkForDeleteFalse(
        "ITM000001",
        "USR000001"))
        .thenReturn(Optional.of(inventoryItem));
    when(categoryRepository.findByCodeAndUserCodeAndMarkForDeleteFalse(
        "CAT000001",
        "USR000001"))
        .thenReturn(Optional.of(category()));
    when(inventoryItemRepository.save(inventoryItem)).thenReturn(inventoryItem);

    InventoryItemResponse response = inventoryService.update(
        "ITM000001",
        updateRequest());

    assertThat(response.name()).isEqualTo("MacBook Pro M3");
    assertThat(response.status()).isEqualTo("SOLD");
    assertThat(inventoryItem.getLocationCode()).isNull();
    assertThat(inventoryItem.getUpdatedBy()).isEqualTo("yos@example.com");
  }

  @Test
  void shouldSoftDeleteInventoryItem() {
    InventoryItem inventoryItem = inventoryItem();
    when(currentUserProvider.requireActiveUser()).thenReturn(currentUser());
    when(inventoryItemRepository.findByCodeAndUserCodeAndMarkForDeleteFalse(
        "ITM000001",
        "USR000001"))
        .thenReturn(Optional.of(inventoryItem));

    inventoryService.delete("ITM000001");

    assertThat(inventoryItem.getMarkForDelete()).isTrue();
    assertThat(inventoryItem.getUpdatedBy()).isEqualTo("yos@example.com");
    verify(inventoryItemRepository).save(inventoryItem);
  }

  @Test
  void shouldRejectInventoryItemNotOwnedByCurrentUser() {
    when(currentUserProvider.requireActiveUser()).thenReturn(currentUser());
    when(inventoryItemRepository.findByCodeAndUserCodeAndMarkForDeleteFalse(
        "ITM000002",
        "USR000001"))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> inventoryService.findByCode("ITM000002"))
        .isInstanceOf(ApiResourceNotFoundException.class)
        .hasMessage("NotFound");
  }

  private CreateInventoryItemRequest createRequest() {
    return new CreateInventoryItemRequest(
        " CAT000001 ",
        " LOC000001 ",
        " MacBook Pro ",
        " Work laptop ",
        1,
        new BigDecimal("25000000.00"),
        LocalDate.of(2026, 7, 1),
        "NEW",
        " Includes charger ");
  }

  private UpdateInventoryItemRequest updateRequest() {
    return new UpdateInventoryItemRequest(
        "CAT000001",
        " ",
        " MacBook Pro M3 ",
        null,
        1,
        new BigDecimal("23000000.00"),
        LocalDate.of(2026, 7, 1),
        "GOOD",
        "SOLD",
        null);
  }

  private User currentUser() {
    User user = new User();
    user.setCode("USR000001");
    user.setEmail("yos@example.com");
    user.setStatus(ActiveStatus.ACTIVE);
    return user;
  }

  private Category category() {
    Category category = new Category();
    category.setCode("CAT000001");
    category.setUserCode("USR000001");
    category.setStatus(ActiveStatus.ACTIVE);
    return category;
  }

  private Location location() {
    Location location = new Location();
    location.setCode("LOC000001");
    location.setUserCode("USR000001");
    location.setStatus(ActiveStatus.ACTIVE);
    return location;
  }

  private InventoryItem inventoryItem() {
    InventoryItem inventoryItem = new InventoryItem();
    inventoryItem.setCode("ITM000001");
    inventoryItem.setUserCode("USR000001");
    inventoryItem.setCategoryCode("CAT000001");
    inventoryItem.setLocationCode("LOC000001");
    inventoryItem.setName("MacBook Pro");
    inventoryItem.setDescription("Work laptop");
    inventoryItem.setQuantity(1);
    inventoryItem.setPurchasePrice(new BigDecimal("25000000.00"));
    inventoryItem.setPurchaseDate(LocalDate.of(2026, 7, 1));
    inventoryItem.setCondition(InventoryCondition.NEW);
    inventoryItem.setStatus(InventoryStatus.OWNED);
    inventoryItem.setNotes("Includes charger");
    inventoryItem.setCreatedBy("yos@example.com");
    inventoryItem.setUpdatedBy("yos@example.com");
    return inventoryItem;
  }
}
