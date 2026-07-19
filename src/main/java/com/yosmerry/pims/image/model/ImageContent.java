package com.yosmerry.pims.image.model;

import org.springframework.core.io.Resource;

public record ImageContent(
    Resource resource,
    String contentType,
    String originalFilename,
    long fileSize) {
}
