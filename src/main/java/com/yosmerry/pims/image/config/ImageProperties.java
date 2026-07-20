package com.yosmerry.pims.image.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "pims.image")
public record ImageProperties(
    Path storageDirectory,
    long maxFileSize,
    int maxImagesPerItem) {
}
