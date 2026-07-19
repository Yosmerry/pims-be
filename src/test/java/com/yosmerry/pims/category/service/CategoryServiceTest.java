package com.yosmerry.pims.category.service;

import com.yosmerry.pims.auth.security.CurrentUserProvider;
import com.yosmerry.pims.category.dto.CategoryResponse;
import com.yosmerry.pims.category.dto.CreateCategoryRequest;
import com.yosmerry.pims.category.dto.UpdateCategoryRequest;
import com.yosmerry.pims.category.entity.Category;
import com.yosmerry.pims.category.repository.CategoryRepository;
import com.yosmerry.pims.common.enums.ActiveStatus;
import com.yosmerry.pims.common.enums.CodeType;
import com.yosmerry.pims.common.exception.ApiResourceNotFoundException;
import com.yosmerry.pims.common.util.CodeGenerator;
import com.yosmerry.pims.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

  @Mock
  private CategoryRepository categoryRepository;

  @Mock
  private CurrentUserProvider currentUserProvider;

  @Mock
  private CodeGenerator codeGenerator;

  private CategoryService categoryService;

  @BeforeEach
  void setUp() {
    categoryService = new CategoryService(
        categoryRepository,
        currentUserProvider,
        codeGenerator);
  }

  @Test
  void shouldCreateCategoryForCurrentUser() {
    User user = currentUser();
    when(currentUserProvider.requireActiveUser()).thenReturn(user);
    when(codeGenerator.next(CodeType.CATEGORY)).thenReturn("CAT000001");
    when(categoryRepository.save(any(Category.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    CategoryResponse response = categoryService.create(
        new CreateCategoryRequest(
            " Electronics ",
            " Electronic devices "));

    assertThat(response.code()).isEqualTo("CAT000001");
    assertThat(response.name()).isEqualTo("Electronics");
    assertThat(response.description()).isEqualTo("Electronic devices");
    assertThat(response.status()).isEqualTo("ACTIVE");

    ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
    verify(categoryRepository).save(categoryCaptor.capture());
    assertThat(categoryCaptor.getValue().getUserCode()).isEqualTo("USR000001");
    assertThat(categoryCaptor.getValue().getCreatedBy()).isEqualTo("yos@example.com");
  }

  @Test
  void shouldGetCurrentUserCategories() {
    User user = currentUser();
    when(currentUserProvider.requireActiveUser()).thenReturn(user);
    when(categoryRepository
        .findAllByUserCodeAndMarkForDeleteFalseOrderByNameAsc("USR000001"))
        .thenReturn(List.of(category()));

    List<CategoryResponse> response = categoryService.findAll();

    assertThat(response).hasSize(1);
    assertThat(response.getFirst().code()).isEqualTo("CAT000001");
    verify(categoryRepository)
        .findAllByUserCodeAndMarkForDeleteFalseOrderByNameAsc("USR000001");
  }

  @Test
  void shouldGetCategoryByCode() {
    User user = currentUser();
    Category category = category();
    when(currentUserProvider.requireActiveUser()).thenReturn(user);
    when(categoryRepository.findByCodeAndUserCodeAndMarkForDeleteFalse(
        "CAT000001",
        "USR000001"))
        .thenReturn(Optional.of(category));

    CategoryResponse response = categoryService.findByCode("CAT000001");

    assertThat(response.name()).isEqualTo("Electronics");
  }

  @Test
  void shouldUpdateCategory() {
    User user = currentUser();
    Category category = category();
    when(currentUserProvider.requireActiveUser()).thenReturn(user);
    when(categoryRepository.findByCodeAndUserCodeAndMarkForDeleteFalse(
        "CAT000001",
        "USR000001"))
        .thenReturn(Optional.of(category));
    when(categoryRepository.save(category)).thenReturn(category);

    CategoryResponse response = categoryService.update(
        "CAT000001",
        new UpdateCategoryRequest(
            " Gadgets ",
            " Personal gadgets ",
            "INACTIVE"));

    assertThat(response.name()).isEqualTo("Gadgets");
    assertThat(response.description()).isEqualTo("Personal gadgets");
    assertThat(response.status()).isEqualTo("INACTIVE");
    assertThat(category.getUpdatedBy()).isEqualTo("yos@example.com");
  }

  @Test
  void shouldSoftDeleteCategory() {
    User user = currentUser();
    Category category = category();
    when(currentUserProvider.requireActiveUser()).thenReturn(user);
    when(categoryRepository.findByCodeAndUserCodeAndMarkForDeleteFalse(
        "CAT000001",
        "USR000001"))
        .thenReturn(Optional.of(category));

    categoryService.delete("CAT000001");

    assertThat(category.getMarkForDelete()).isTrue();
    assertThat(category.getUpdatedBy()).isEqualTo("yos@example.com");
    verify(categoryRepository).save(category);
  }

  @Test
  void shouldRejectCategoryNotOwnedByCurrentUser() {
    User user = currentUser();
    when(currentUserProvider.requireActiveUser()).thenReturn(user);
    when(categoryRepository.findByCodeAndUserCodeAndMarkForDeleteFalse(
        "CAT000002",
        "USR000001"))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> categoryService.findByCode("CAT000002"))
        .isInstanceOf(ApiResourceNotFoundException.class)
        .hasMessage("NotFound");
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
    category.setName("Electronics");
    category.setDescription("Electronic devices");
    category.setStatus(ActiveStatus.ACTIVE);
    category.setCreatedBy("yos@example.com");
    category.setUpdatedBy("yos@example.com");
    return category;
  }
}
