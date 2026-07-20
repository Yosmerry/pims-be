package com.yosmerry.pims.image.service;

import com.yosmerry.pims.auth.security.CurrentUserProvider;
import com.yosmerry.pims.common.enums.ActiveStatus;
import com.yosmerry.pims.common.enums.CodeType;
import com.yosmerry.pims.common.exception.ApiResourceNotFoundException;
import com.yosmerry.pims.common.exception.ApiValidationException;
import com.yosmerry.pims.common.util.CodeGenerator;
import com.yosmerry.pims.image.config.ImageProperties;
import com.yosmerry.pims.image.dto.ImageResponse;
import com.yosmerry.pims.image.entity.ItemImage;
import com.yosmerry.pims.image.model.ImageContent;
import com.yosmerry.pims.image.repository.ItemImageRepository;
import com.yosmerry.pims.inventory.entity.InventoryItem;
import com.yosmerry.pims.inventory.repository.InventoryItemRepository;
import com.yosmerry.pims.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

  private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
  private static final int MAX_IMAGES_PER_ITEM = 5;

  @TempDir
  private Path temporaryDirectory;

  @Mock
  private ItemImageRepository itemImageRepository;

  @Mock
  private InventoryItemRepository inventoryItemRepository;

  @Mock
  private CurrentUserProvider currentUserProvider;

  @Mock
  private CodeGenerator codeGenerator;

  private ImageService imageService;

  @BeforeEach
  void setUp() {
    imageService = new ImageService(
        itemImageRepository,
        inventoryItemRepository,
        currentUserProvider,
        codeGenerator,
        new ImageProperties(
            temporaryDirectory,
            MAX_FILE_SIZE,
            MAX_IMAGES_PER_ITEM));
  }

  @Test
  void shouldUploadFirstImageAsPrimary() {
    when(currentUserProvider.requireActiveUser()).thenReturn(currentUser());
    when(inventoryItemRepository.findByCodeAndUserCodeAndMarkForDeleteFalse(
        "ITM000001",
        "USR000001"))
        .thenReturn(Optional.of(inventoryItem()));
    when(codeGenerator.next(CodeType.ITEM_IMAGE)).thenReturn("IMG000001");
    when(itemImageRepository
        .countByInventoryItemCodeAndMarkForDeleteFalse("ITM000001"))
        .thenReturn(0L);
    when(itemImageRepository.save(any(ItemImage.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    ImageResponse response = imageService.upload(
        "ITM000001",
        pngFile());

    assertThat(response.code()).isEqualTo("IMG000001");
    assertThat(response.originalFilename()).isEqualTo("laptop.png");
    assertThat(response.primary()).isTrue();
    assertThat(response.url()).isEqualTo("/api/v1/images/IMG000001");

    ArgumentCaptor<ItemImage> imageCaptor = ArgumentCaptor.forClass(ItemImage.class);
    verify(itemImageRepository).save(imageCaptor.capture());
    ItemImage savedImage = imageCaptor.getValue();
    assertThat(savedImage.getStoredFilename()).endsWith(".png");
    assertThat(temporaryDirectory.resolve(savedImage.getStoragePath()))
        .exists()
        .isRegularFile();
  }

  @Test
  void shouldRejectUnsupportedFileType() {
    when(currentUserProvider.requireActiveUser()).thenReturn(currentUser());
    when(inventoryItemRepository.findByCodeAndUserCodeAndMarkForDeleteFalse(
        "ITM000001",
        "USR000001"))
        .thenReturn(Optional.of(inventoryItem()));
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "notes.txt",
        "text/plain",
        "not an image".getBytes(StandardCharsets.UTF_8));

    assertThatThrownBy(() -> imageService.upload("ITM000001", file))
        .isInstanceOf(ApiValidationException.class)
        .satisfies(exception -> assertThat(
            ((ApiValidationException) exception).getErrors())
            .containsEntry("file", List.of("UnsupportedFileType")));
  }

  @Test
  void shouldRejectImageWhenInventoryItemReachedImageLimit() {
    when(currentUserProvider.requireActiveUser()).thenReturn(currentUser());
    when(inventoryItemRepository.findByCodeAndUserCodeAndMarkForDeleteFalse(
        "ITM000001",
        "USR000001"))
        .thenReturn(Optional.of(inventoryItem()));
    when(itemImageRepository
        .countByInventoryItemCodeAndMarkForDeleteFalse("ITM000001"))
        .thenReturn((long) MAX_IMAGES_PER_ITEM);

    assertThatThrownBy(() -> imageService.upload("ITM000001", pngFile()))
        .isInstanceOf(ApiValidationException.class)
        .satisfies(exception -> assertThat(
            ((ApiValidationException) exception).getErrors())
            .containsEntry("file", List.of("Maximum5")));
  }

  @Test
  void shouldRejectFileLargerThanConfiguredLimit() {
    when(currentUserProvider.requireActiveUser()).thenReturn(currentUser());
    when(inventoryItemRepository.findByCodeAndUserCodeAndMarkForDeleteFalse(
        "ITM000001",
        "USR000001"))
        .thenReturn(Optional.of(inventoryItem()));
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "large.png",
        "image/png",
        new byte[(int) MAX_FILE_SIZE + 1]);

    assertThatThrownBy(() -> imageService.upload("ITM000001", file))
        .isInstanceOf(ApiValidationException.class)
        .satisfies(exception -> assertThat(
            ((ApiValidationException) exception).getErrors())
            .containsEntry("file", List.of("FileTooLarge")));
  }

  @Test
  void shouldRejectInvalidImageSignature() {
    when(currentUserProvider.requireActiveUser()).thenReturn(currentUser());
    when(inventoryItemRepository.findByCodeAndUserCodeAndMarkForDeleteFalse(
        "ITM000001",
        "USR000001"))
        .thenReturn(Optional.of(inventoryItem()));
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "fake.png",
        "image/png",
        "not a png".getBytes(StandardCharsets.UTF_8));

    assertThatThrownBy(() -> imageService.upload("ITM000001", file))
        .isInstanceOf(ApiValidationException.class)
        .satisfies(exception -> assertThat(
            ((ApiValidationException) exception).getErrors())
            .containsEntry("file", List.of("Invalid")));
  }

  @Test
  void shouldRejectInventoryItemNotOwnedByCurrentUser() {
    when(currentUserProvider.requireActiveUser()).thenReturn(currentUser());
    when(inventoryItemRepository.findByCodeAndUserCodeAndMarkForDeleteFalse(
        "ITM000002",
        "USR000001"))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> imageService.upload("ITM000002", pngFile()))
        .isInstanceOf(ApiResourceNotFoundException.class)
        .hasMessage("NotFound");
  }

  @Test
  void shouldGetImagesForOwnedInventoryItem() {
    ItemImage primaryImage = itemImage();
    ItemImage secondaryImage = secondaryItemImage();

    when(currentUserProvider.requireActiveUser()).thenReturn(currentUser());
    when(inventoryItemRepository.findByCodeAndUserCodeAndMarkForDeleteFalse(
        "ITM000001",
        "USR000001"))
        .thenReturn(Optional.of(inventoryItem()));
    when(itemImageRepository
        .findAllByInventoryItemCodeAndMarkForDeleteFalseOrderByPrimaryDescCreatedDateAsc(
            "ITM000001"))
        .thenReturn(List.of(primaryImage, secondaryImage));

    List<ImageResponse> response = imageService.findAll("ITM000001");

    assertThat(response)
        .extracting(ImageResponse::code)
        .containsExactly("IMG000001", "IMG000002");
    assertThat(response.getFirst().primary()).isTrue();
    assertThat(response.getFirst().url())
        .isEqualTo("/api/v1/images/IMG000001");
  }

  @Test
  void shouldGetImageContentForOwnedInventoryItem() throws IOException {
    Path imagePath = temporaryDirectory.resolve("ITM000001/stored.png");
    Files.createDirectories(imagePath.getParent());
    Files.write(imagePath, pngBytes());
    ItemImage itemImage = itemImage();

    when(currentUserProvider.requireActiveUser()).thenReturn(currentUser());
    when(itemImageRepository.findByCodeAndMarkForDeleteFalse("IMG000001"))
        .thenReturn(Optional.of(itemImage));
    when(inventoryItemRepository.findByCodeAndUserCodeAndMarkForDeleteFalse(
        "ITM000001",
        "USR000001"))
        .thenReturn(Optional.of(inventoryItem()));

    ImageContent response = imageService.findContent("IMG000001");

    assertThat(response.contentType()).isEqualTo("image/png");
    assertThat(response.originalFilename()).isEqualTo("laptop.png");
    assertThat(response.resource().exists()).isTrue();
  }

  @Test
  void shouldDeletePrimaryImageAndPromoteNextImage() throws IOException {
    Path imagePath = temporaryDirectory.resolve("ITM000001/stored.png");
    Files.createDirectories(imagePath.getParent());
    Files.write(imagePath, pngBytes());
    ItemImage itemImage = itemImage();
    ItemImage nextImage = secondaryItemImage();

    when(currentUserProvider.requireActiveUser()).thenReturn(currentUser());
    when(itemImageRepository.findByCodeAndMarkForDeleteFalse("IMG000001"))
        .thenReturn(Optional.of(itemImage));
    when(inventoryItemRepository.findByCodeAndUserCodeAndMarkForDeleteFalse(
        "ITM000001",
        "USR000001"))
        .thenReturn(Optional.of(inventoryItem()));
    when(itemImageRepository
        .findFirstByInventoryItemCodeAndCodeNotAndMarkForDeleteFalseOrderByCreatedDateAsc(
            "ITM000001",
            "IMG000001"))
        .thenReturn(Optional.of(nextImage));

    imageService.delete("IMG000001");

    assertThat(itemImage.getMarkForDelete()).isTrue();
    assertThat(nextImage.getPrimary()).isTrue();
    assertThat(nextImage.getUpdatedBy()).isEqualTo("yos@example.com");
    assertThat(imagePath).doesNotExist();
    verify(itemImageRepository).saveAll(List.of(itemImage, nextImage));
  }

  @Test
  void shouldDeleteAllImagesForDeletedInventoryItem() throws IOException {
    Path firstPath = temporaryDirectory.resolve("ITM000001/stored.png");
    Path secondPath = temporaryDirectory.resolve("ITM000001/stored-2.png");
    Files.createDirectories(firstPath.getParent());
    Files.write(firstPath, pngBytes());
    Files.write(secondPath, pngBytes());
    ItemImage firstImage = itemImage();
    ItemImage secondImage = secondaryItemImage();

    when(itemImageRepository
        .findAllByInventoryItemCodeAndMarkForDeleteFalse("ITM000001"))
        .thenReturn(List.of(firstImage, secondImage));

    imageService.deleteAllForInventoryItem("ITM000001", "yos@example.com");

    assertThat(firstImage.getMarkForDelete()).isTrue();
    assertThat(secondImage.getMarkForDelete()).isTrue();
    assertThat(firstImage.getUpdatedBy()).isEqualTo("yos@example.com");
    assertThat(secondImage.getUpdatedBy()).isEqualTo("yos@example.com");
    assertThat(firstPath).doesNotExist();
    assertThat(secondPath).doesNotExist();
    verify(itemImageRepository).saveAll(List.of(firstImage, secondImage));
  }

  private MockMultipartFile pngFile() {
    return new MockMultipartFile(
        "file",
        "laptop.png",
        "image/png",
        pngBytes());
  }

  private byte[] pngBytes() {
    return new byte[] {
        (byte) 0x89, 0x50, 0x4E, 0x47,
        0x0D, 0x0A, 0x1A, 0x0A,
        0x00, 0x01
    };
  }

  private User currentUser() {
    User user = new User();
    user.setCode("USR000001");
    user.setEmail("yos@example.com");
    user.setStatus(ActiveStatus.ACTIVE);
    return user;
  }

  private InventoryItem inventoryItem() {
    InventoryItem inventoryItem = new InventoryItem();
    inventoryItem.setCode("ITM000001");
    inventoryItem.setUserCode("USR000001");
    return inventoryItem;
  }

  private ItemImage itemImage() {
    ItemImage itemImage = new ItemImage();
    itemImage.setCode("IMG000001");
    itemImage.setInventoryItemCode("ITM000001");
    itemImage.setOriginalFilename("laptop.png");
    itemImage.setStoredFilename("stored.png");
    itemImage.setContentType("image/png");
    itemImage.setFileSize((long) pngBytes().length);
    itemImage.setStoragePath("ITM000001/stored.png");
    itemImage.setPrimary(true);
    return itemImage;
  }

  private ItemImage secondaryItemImage() {
    ItemImage itemImage = new ItemImage();
    itemImage.setCode("IMG000002");
    itemImage.setInventoryItemCode("ITM000001");
    itemImage.setOriginalFilename("laptop-2.png");
    itemImage.setStoredFilename("stored-2.png");
    itemImage.setContentType("image/png");
    itemImage.setFileSize((long) pngBytes().length);
    itemImage.setStoragePath("ITM000001/stored-2.png");
    itemImage.setPrimary(false);
    return itemImage;
  }
}
