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
class LocationServiceTest {

  @Mock
  private LocationRepository locationRepository;

  @Mock
  private CurrentUserProvider currentUserProvider;

  @Mock
  private CodeGenerator codeGenerator;

  private LocationService locationService;

  @BeforeEach
  void setUp() {
    locationService = new LocationService(
        locationRepository,
        currentUserProvider,
        codeGenerator);
  }

  @Test
  void shouldCreateLocationForCurrentUser() {
    User user = currentUser();
    when(currentUserProvider.requireActiveUser()).thenReturn(user);
    when(codeGenerator.next(CodeType.LOCATION)).thenReturn("LOC000001");
    when(locationRepository.save(any(Location.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    LocationResponse response = locationService.create(
        new CreateLocationRequest(
            " Garage ",
            " Items stored "));

    assertThat(response.code()).isEqualTo("LOC000001");
    assertThat(response.name()).isEqualTo("Garage");
    assertThat(response.description()).isEqualTo("Items stored");
    assertThat(response.status()).isEqualTo("ACTIVE");

    ArgumentCaptor<Location> locationCaptor = ArgumentCaptor.forClass(Location.class);
    verify(locationRepository).save(locationCaptor.capture());
    assertThat(locationCaptor.getValue().getUserCode()).isEqualTo("USR000001");
    assertThat(locationCaptor.getValue().getCreatedBy()).isEqualTo("yos@example.com");
  }

  @Test
  void shouldGetCurrentUserLocations() {
    User user = currentUser();
    when(currentUserProvider.requireActiveUser()).thenReturn(user);
    when(locationRepository
        .findAllByUserCodeAndMarkForDeleteFalseOrderByNameAsc("USR000001"))
        .thenReturn(List.of(location()));

    List<LocationResponse> response = locationService.findAll();

    assertThat(response).hasSize(1);
    assertThat(response.getFirst().code()).isEqualTo("LOC000001");
    verify(locationRepository)
        .findAllByUserCodeAndMarkForDeleteFalseOrderByNameAsc("USR000001");
  }

  @Test
  void shouldGetLocationByCode() {
    User user = currentUser();
    Location location = location();
    when(currentUserProvider.requireActiveUser()).thenReturn(user);
    when(locationRepository.findByCodeAndUserCodeAndMarkForDeleteFalse(
        "LOC000001",
        "USR000001"))
        .thenReturn(Optional.of(location));

    LocationResponse response = locationService.findByCode("LOC000001");

    assertThat(response.name()).isEqualTo("Garage");
  }

  @Test
  void shouldUpdateLocation() {
    User user = currentUser();
    Location location = location();
    when(currentUserProvider.requireActiveUser()).thenReturn(user);
    when(locationRepository.findByCodeAndUserCodeAndMarkForDeleteFalse(
        "LOC000001",
        "USR000001"))
        .thenReturn(Optional.of(location));
    when(locationRepository.save(location)).thenReturn(location);

    LocationResponse response = locationService.update(
        "LOC000001",
        new UpdateLocationRequest(
            " Storage Room ",
            " Personal storage items ",
            "INACTIVE"));

    assertThat(response.name()).isEqualTo("Storage Room");
    assertThat(response.description()).isEqualTo("Personal storage items");
    assertThat(response.status()).isEqualTo("INACTIVE");
    assertThat(location.getUpdatedBy()).isEqualTo("yos@example.com");
  }

  @Test
  void shouldSoftDeleteLocation() {
    User user = currentUser();
    Location location = location();
    when(currentUserProvider.requireActiveUser()).thenReturn(user);
    when(locationRepository.findByCodeAndUserCodeAndMarkForDeleteFalse(
        "LOC000001",
        "USR000001"))
        .thenReturn(Optional.of(location));

    locationService.delete("LOC000001");

    assertThat(location.getMarkForDelete()).isTrue();
    assertThat(location.getUpdatedBy()).isEqualTo("yos@example.com");
    verify(locationRepository).save(location);
  }

  @Test
  void shouldRejectLocationNotOwnedByCurrentUser() {
    User user = currentUser();
    when(currentUserProvider.requireActiveUser()).thenReturn(user);
    when(locationRepository.findByCodeAndUserCodeAndMarkForDeleteFalse(
        "LOC000002",
        "USR000001"))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> locationService.findByCode("LOC000002"))
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

  private Location location() {
    Location location = new Location();
    location.setCode("LOC000001");
    location.setUserCode("USR000001");
    location.setName("Garage");
    location.setDescription("Items stored");
    location.setStatus(ActiveStatus.ACTIVE);
    location.setCreatedBy("yos@example.com");
    location.setUpdatedBy("yos@example.com");
    return location;
  }
}
