package com.yosmerry.pims.location.service;

import com.yosmerry.pims.auth.security.CurrentUserProvider;
import com.yosmerry.pims.location.dto.LocationResponse;
import com.yosmerry.pims.location.dto.CreateLocationRequest;
import com.yosmerry.pims.location.dto.UpdateLocationRequest;
import com.yosmerry.pims.location.entity.Location;
import com.yosmerry.pims.location.repository.LocationRepository;
import com.yosmerry.pims.common.enums.ActiveStatus;
import com.yosmerry.pims.common.enums.CodeType;
import com.yosmerry.pims.common.exception.ApiResourceNotFoundException;
import com.yosmerry.pims.common.request.PagingRequest;
import com.yosmerry.pims.common.response.PageResponse;
import com.yosmerry.pims.common.util.CodeGenerator;
import com.yosmerry.pims.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LocationService {

  private static final String LOCATION_FIELD = "location";

  private final LocationRepository locationRepository;
  private final CurrentUserProvider currentUserProvider;
  private final CodeGenerator codeGenerator;

  @Transactional
  public LocationResponse create(CreateLocationRequest request) {
    User user = currentUserProvider.requireActiveUser();

    Location location = new Location();
    location.setCode(codeGenerator.next(CodeType.LOCATION));
    location.setUserCode(user.getCode());
    location.setName(request.name().trim());
    location.setDescription(normalizeDescription(request.description()));
    location.setStatus(ActiveStatus.ACTIVE);
    location.setCreatedBy(user.getEmail());
    location.setUpdatedBy(user.getEmail());

    return toResponse(locationRepository.save(location));
  }

  @Transactional(readOnly = true)
  public PageResponse<LocationResponse> findAll(PagingRequest pagingRequest) {
    User user = currentUserProvider.requireActiveUser();
    Pageable pageable = PageRequest.of(
        pagingRequest.getPage(),
        pagingRequest.getSize(),
        Sort.by("name").ascending());

    return PageResponse.from(locationRepository
        .findAllByUserCodeAndMarkForDeleteFalse(user.getCode(), pageable)
        .map(this::toResponse));
  }

  @Transactional(readOnly = true)
  public LocationResponse findByCode(String code) {
    User user = currentUserProvider.requireActiveUser();
    return toResponse(requireLocation(code, user.getCode()));
  }

  @Transactional
  public LocationResponse update(
      String code,
      UpdateLocationRequest request) {
    User user = currentUserProvider.requireActiveUser();
    Location location = requireLocation(code, user.getCode());

    location.setName(request.name().trim());
    location.setDescription(normalizeDescription(request.description()));
    location.setStatus(ActiveStatus.valueOf(request.status()));
    location.setUpdatedBy(user.getEmail());

    return toResponse(locationRepository.save(location));
  }

  @Transactional
  public void delete(String code) {
    User user = currentUserProvider.requireActiveUser();
    Location location = requireLocation(code, user.getCode());

    location.setMarkForDelete(true);
    location.setUpdatedBy(user.getEmail());
    locationRepository.save(location);
  }

  private Location requireLocation(String code, String userCode) {
    return locationRepository
        .findByCodeAndUserCodeAndMarkForDeleteFalse(code, userCode)
        .orElseThrow(() -> new ApiResourceNotFoundException(LOCATION_FIELD));
  }

  private String normalizeDescription(String description) {
    if (description == null || description.isBlank()) {
      return null;
    }
    return description.trim();
  }

  private LocationResponse toResponse(Location location) {
    return LocationResponse.builder()
        .code(location.getCode())
        .name(location.getName())
        .description(location.getDescription())
        .status(location.getStatus().name())
        .createdDate(location.getCreatedDate())
        .updatedDate(location.getUpdatedDate())
        .build();
  }
}
