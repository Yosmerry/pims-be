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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

  private static final String CATEGORY_FIELD = "category";

  private final CategoryRepository categoryRepository;
  private final CurrentUserProvider currentUserProvider;
  private final CodeGenerator codeGenerator;

  @Transactional
  public CategoryResponse create(CreateCategoryRequest request) {
    User user = currentUserProvider.requireActiveUser();

    Category category = new Category();
    category.setCode(codeGenerator.next(CodeType.CATEGORY));
    category.setUserCode(user.getCode());
    category.setName(request.name().trim());
    category.setDescription(normalizeDescription(request.description()));
    category.setStatus(ActiveStatus.ACTIVE);
    category.setCreatedBy(user.getEmail());
    category.setUpdatedBy(user.getEmail());

    return toResponse(categoryRepository.save(category));
  }

  @Transactional(readOnly = true)
  public List<CategoryResponse> findAll() {
    User user = currentUserProvider.requireActiveUser();

    return categoryRepository
        .findAllByUserCodeAndMarkForDeleteFalseOrderByNameAsc(user.getCode())
        .stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public CategoryResponse findByCode(String code) {
    User user = currentUserProvider.requireActiveUser();
    return toResponse(requireCategory(code, user.getCode()));
  }

  @Transactional
  public CategoryResponse update(
      String code,
      UpdateCategoryRequest request) {
    User user = currentUserProvider.requireActiveUser();
    Category category = requireCategory(code, user.getCode());

    category.setName(request.name().trim());
    category.setDescription(normalizeDescription(request.description()));
    category.setStatus(ActiveStatus.valueOf(request.status()));
    category.setUpdatedBy(user.getEmail());

    return toResponse(categoryRepository.save(category));
  }

  @Transactional
  public void delete(String code) {
    User user = currentUserProvider.requireActiveUser();
    Category category = requireCategory(code, user.getCode());

    category.setMarkForDelete(true);
    category.setUpdatedBy(user.getEmail());
    categoryRepository.save(category);
  }

  private Category requireCategory(String code, String userCode) {
    return categoryRepository
        .findByCodeAndUserCodeAndMarkForDeleteFalse(code, userCode)
        .orElseThrow(() -> new ApiResourceNotFoundException(CATEGORY_FIELD));
  }

  private String normalizeDescription(String description) {
    if (description == null || description.isBlank()) {
      return null;
    }
    return description.trim();
  }

  private CategoryResponse toResponse(Category category) {
    return CategoryResponse.builder()
        .code(category.getCode())
        .name(category.getName())
        .description(category.getDescription())
        .status(category.getStatus().name())
        .createdDate(category.getCreatedDate())
        .updatedDate(category.getUpdatedDate())
        .build();
  }
}
