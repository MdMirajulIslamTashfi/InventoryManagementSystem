package com.tashfi.InventoryManagementSystem.core.storage;

import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.List;

public interface StorageService {
    Mono<List<String>> uploadImages(List<MultipartFile> files, String productIdentifier);

    // Best-effort delete of a single previously-uploaded image.
    // Should not blow up the calling flow if the file is already gone.
    Mono<Void> deleteImage(String imageUrl);
}