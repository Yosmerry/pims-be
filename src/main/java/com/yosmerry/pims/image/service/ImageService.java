package com.yosmerry.pims.image.service;

import com.yosmerry.pims.auth.security.CurrentUserProvider;
import com.yosmerry.pims.common.constant.BasePathNames;
import com.yosmerry.pims.common.constant.ErrorCodes;
import com.yosmerry.pims.common.enums.CodeType;
import com.yosmerry.pims.common.exception.ApiFileStorageException;
import com.yosmerry.pims.common.exception.ApiResourceNotFoundException;
import com.yosmerry.pims.common.exception.ApiValidationException;
import com.yosmerry.pims.common.util.CodeGenerator;
import com.yosmerry.pims.image.config.ImageProperties;
import com.yosmerry.pims.image.dto.ImageResponse;
import com.yosmerry.pims.image.entity.ItemImage;
import com.yosmerry.pims.image.model.ImageContent;
import com.yosmerry.pims.image.repository.ItemImageRepository;
import com.yosmerry.pims.inventory.repository.InventoryItemRepository;
import com.yosmerry.pims.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

  private static final String IMAGE_FIELD = "image";
  private static final String INVENTORY_ITEM_FIELD = "inventoryItem";
  private static final String FILE_FIELD = "file";
  private static final String JPEG_CONTENT_TYPE = "image/jpeg";
  private static final String PNG_CONTENT_TYPE = "image/png";
  private static final byte[] JPEG_SIGNATURE = {
      (byte) 0xFF, (byte) 0xD8, (byte) 0xFF
  };
  private static final byte[] PNG_SIGNATURE = {
      (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
  };

  private final ItemImageRepository itemImageRepository;
  private final InventoryItemRepository inventoryItemRepository;
  private final CurrentUserProvider currentUserProvider;
  private final CodeGenerator codeGenerator;
  private final ImageProperties imageProperties;

  @Transactional
  public ImageResponse upload(
      String inventoryItemCode,
      MultipartFile file) {
    User user = currentUserProvider.requireActiveUser();
    requireOwnedInventoryItem(inventoryItemCode, user.getCode());
    long imageCount = requireImageCapacity(inventoryItemCode);
    validateFile(file);

    String contentType = file.getContentType();
    String extension = resolveExtension(contentType);
    String originalFilename = resolveOriginalFilename(file.getOriginalFilename());
    String storedFilename = UUID.randomUUID() + extension;
    Path relativePath = Path.of(inventoryItemCode, storedFilename);
    Path storedPath = store(file, relativePath);

    try {
      ItemImage itemImage = new ItemImage();
      itemImage.setCode(codeGenerator.next(CodeType.ITEM_IMAGE));
      itemImage.setInventoryItemCode(inventoryItemCode);
      itemImage.setOriginalFilename(originalFilename);
      itemImage.setStoredFilename(storedFilename);
      itemImage.setContentType(contentType);
      itemImage.setFileSize(file.getSize());
      itemImage.setStoragePath(relativePath.toString());
      itemImage.setPrimary(imageCount == 0);
      itemImage.setCreatedBy(user.getEmail());
      itemImage.setUpdatedBy(user.getEmail());

      return toResponse(itemImageRepository.save(itemImage));
    } catch (RuntimeException exception) {
      deleteQuietly(storedPath);
      throw exception;
    }
  }

  @Transactional(readOnly = true)
  public List<ImageResponse> findAll(String inventoryItemCode) {
    User user = currentUserProvider.requireActiveUser();
    requireOwnedInventoryItem(inventoryItemCode, user.getCode());

    return itemImageRepository
        .findAllByInventoryItemCodeAndMarkForDeleteFalseOrderByPrimaryDescCreatedDateAsc(
            inventoryItemCode)
        .stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public ImageContent findContent(String imageCode) {
    User user = currentUserProvider.requireActiveUser();
    ItemImage itemImage = itemImageRepository
        .findByCodeAndMarkForDeleteFalse(imageCode)
        .orElseThrow(() -> new ApiResourceNotFoundException(IMAGE_FIELD));

    if (!isOwnedInventoryItem(itemImage.getInventoryItemCode(), user.getCode())) {
      throw new ApiResourceNotFoundException(IMAGE_FIELD);
    }

    Path filePath = resolveStoragePath(Path.of(itemImage.getStoragePath()));
    if (!Files.isRegularFile(filePath) || !Files.isReadable(filePath)) {
      throw new ApiFileStorageException(
          new IOException("Stored image file is unavailable"));
    }

    try {
      Resource resource = new UrlResource(filePath.toUri());
      return new ImageContent(
          resource,
          itemImage.getContentType(),
          itemImage.getOriginalFilename(),
          itemImage.getFileSize());
    } catch (MalformedURLException exception) {
      throw new ApiFileStorageException(exception);
    }
  }

  @Transactional
  public void delete(String imageCode) {
    User user = currentUserProvider.requireActiveUser();
    ItemImage itemImage = itemImageRepository
        .findByCodeAndMarkForDeleteFalse(imageCode)
        .orElseThrow(() -> new ApiResourceNotFoundException(IMAGE_FIELD));

    if (!isOwnedInventoryItem(itemImage.getInventoryItemCode(), user.getCode())) {
      throw new ApiResourceNotFoundException(IMAGE_FIELD);
    }

    List<ItemImage> imagesToSave = new ArrayList<>();
    itemImage.setMarkForDelete(true);
    itemImage.setUpdatedBy(user.getEmail());
    imagesToSave.add(itemImage);

    if (Boolean.TRUE.equals(itemImage.getPrimary())) {
      itemImageRepository
          .findFirstByInventoryItemCodeAndCodeNotAndMarkForDeleteFalseOrderByCreatedDateAsc(
              itemImage.getInventoryItemCode(),
              itemImage.getCode())
          .ifPresent(nextPrimary -> {
            nextPrimary.setPrimary(true);
            nextPrimary.setUpdatedBy(user.getEmail());
            imagesToSave.add(nextPrimary);
          });
    }

    itemImageRepository.saveAll(imagesToSave);
    deleteFilesAfterCommit(List.of(
        resolveStoragePath(Path.of(itemImage.getStoragePath()))));
  }

  @Transactional
  public void deleteAllForInventoryItem(
      String inventoryItemCode,
      String updatedBy) {
    List<ItemImage> itemImages = itemImageRepository
        .findAllByInventoryItemCodeAndMarkForDeleteFalse(inventoryItemCode);
    if (itemImages.isEmpty()) {
      return;
    }

    List<Path> filePaths = new ArrayList<>();
    for (ItemImage itemImage : itemImages) {
      itemImage.setMarkForDelete(true);
      itemImage.setUpdatedBy(updatedBy);
      filePaths.add(resolveStoragePath(Path.of(itemImage.getStoragePath())));
    }

    itemImageRepository.saveAll(itemImages);
    deleteFilesAfterCommit(filePaths);
  }

  private void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw fileValidationError(ErrorCodes.BLANK);
    }
    if (file.getSize() > imageProperties.maxFileSize()) {
      throw fileValidationError(ErrorCodes.FILE_TOO_LARGE);
    }

    String contentType = file.getContentType();
    if (!JPEG_CONTENT_TYPE.equals(contentType)
        && !PNG_CONTENT_TYPE.equals(contentType)) {
      throw fileValidationError(ErrorCodes.UNSUPPORTED_FILE_TYPE);
    }

    if (!hasValidSignature(file, contentType)) {
      throw fileValidationError(ErrorCodes.INVALID);
    }
  }

  private long requireImageCapacity(String inventoryItemCode) {
    long imageCount = itemImageRepository
        .countByInventoryItemCodeAndMarkForDeleteFalse(inventoryItemCode);
    if (imageCount >= imageProperties.maxImagesPerItem()) {
      throw fileValidationError(ErrorCodes.MAXIMUM_5);
    }
    return imageCount;
  }

  private boolean hasValidSignature(MultipartFile file, String contentType) {
    try (InputStream inputStream = file.getInputStream()) {
      byte[] header = inputStream.readNBytes(PNG_SIGNATURE.length);
      if (JPEG_CONTENT_TYPE.equals(contentType)) {
        return startsWith(header, JPEG_SIGNATURE);
      }
      return startsWith(header, PNG_SIGNATURE);
    } catch (IOException exception) {
      throw new ApiFileStorageException(exception);
    }
  }

  private boolean startsWith(byte[] value, byte[] prefix) {
    if (value.length < prefix.length) {
      return false;
    }
    for (int index = 0; index < prefix.length; index++) {
      if (value[index] != prefix[index]) {
        return false;
      }
    }
    return true;
  }

  private String resolveExtension(String contentType) {
    if (JPEG_CONTENT_TYPE.equals(contentType)) {
      return ".jpg";
    }
    return ".png";
  }

  private String resolveOriginalFilename(String originalFilename) {
    if (originalFilename == null || originalFilename.isBlank()) {
      throw fileValidationError(ErrorCodes.BLANK);
    }

    String normalized = originalFilename.replace('\\', '/');
    String filename;
    try {
      Path filenamePath = Path.of(normalized).getFileName();
      if (filenamePath == null) {
        throw fileValidationError(ErrorCodes.INVALID);
      }
      filename = filenamePath.toString();
    } catch (InvalidPathException exception) {
      throw fileValidationError(ErrorCodes.INVALID);
    }
    if (".".equals(filename) || "..".equals(filename)) {
      throw fileValidationError(ErrorCodes.INVALID);
    }
    if (filename.length() > 255) {
      throw fileValidationError(ErrorCodes.CHARACTER_MORE_THAN_255);
    }
    return filename;
  }

  private Path store(MultipartFile file, Path relativePath) {
    Path target = resolveStoragePath(relativePath);
    try {
      Files.createDirectories(target.getParent());
      try (InputStream inputStream = file.getInputStream()) {
        Files.copy(inputStream, target);
      }
      return target;
    } catch (IOException exception) {
      deleteQuietly(target);
      throw new ApiFileStorageException(exception);
    }
  }

  private Path resolveStoragePath(Path relativePath) {
    Path storageRoot = imageProperties.storageDirectory()
        .toAbsolutePath()
        .normalize();
    Path resolvedPath = storageRoot.resolve(relativePath).normalize();
    if (!resolvedPath.startsWith(storageRoot)) {
      throw fileValidationError(ErrorCodes.INVALID);
    }
    return resolvedPath;
  }

  private void requireOwnedInventoryItem(
      String inventoryItemCode,
      String userCode) {
    if (!isOwnedInventoryItem(inventoryItemCode, userCode)) {
      throw new ApiResourceNotFoundException(INVENTORY_ITEM_FIELD);
    }
  }

  private boolean isOwnedInventoryItem(
      String inventoryItemCode,
      String userCode) {
    return inventoryItemRepository
        .findByCodeAndUserCodeAndMarkForDeleteFalse(inventoryItemCode, userCode)
        .isPresent();
  }

  private ApiValidationException fileValidationError(String errorCode) {
    return new ApiValidationException(Map.of(FILE_FIELD, List.of(errorCode)));
  }

  private void deleteFilesAfterCommit(List<Path> filePaths) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(
          new TransactionSynchronization() {
            @Override
            public void afterCommit() {
              filePaths.forEach(ImageService.this::deleteQuietly);
            }
          });
      return;
    }
    filePaths.forEach(this::deleteQuietly);
  }

  private void deleteQuietly(Path filePath) {
    try {
      Files.deleteIfExists(filePath);
    } catch (IOException exception) {
      log.warn("Failed to delete stored image file: {}", filePath, exception);
    }
  }

  private ImageResponse toResponse(ItemImage itemImage) {
    return ImageResponse.builder()
        .code(itemImage.getCode())
        .inventoryItemCode(itemImage.getInventoryItemCode())
        .originalFilename(itemImage.getOriginalFilename())
        .contentType(itemImage.getContentType())
        .fileSize(itemImage.getFileSize())
        .primary(itemImage.getPrimary())
        .url(BasePathNames.IMAGES + "/" + itemImage.getCode())
        .createdDate(itemImage.getCreatedDate())
        .build();
  }
}
