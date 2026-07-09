package com.tashfi.InventoryManagementSystem.core.storage;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.List;

public interface StorageService {

    // Legacy batch upload used by the product module's inline image creation.
    Mono<List<String>> uploadImages(List<MultipartFile> files, String productIdentifier);

    // CMS-style single-file upload: processes (compress + optional thumbnail) and stores one image,
    // returning the resulting URLs. Used by the productimage module.
    Mono<UploadedImageUrls> uploadImage(FilePart file, String identifier, boolean generateThumbnail);

    // Best-effort delete of a single previously-uploaded image.
    // Should not blow up the calling flow if the file is already gone.
    Mono<Void> deleteImage(String imageUrl);
}
