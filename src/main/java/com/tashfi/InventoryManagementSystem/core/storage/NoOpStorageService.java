package com.tashfi.InventoryManagementSystem.core.storage;

import org.springframework.context.annotation.Profile;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Profile("test")
public class NoOpStorageService implements StorageService {

    @Override
    public Mono<List<String>> uploadImages(List<MultipartFile> files, String productIdentifier) {
        return Mono.just(List.of());
    }

    @Override
    public Mono<UploadedImageUrls> uploadImage(FilePart file, String identifier, boolean generateThumbnail) {
        return Mono.just(UploadedImageUrls.builder()
                .imageUrl("")
                .imageName("")
                .thumbnailUrl(null)
                .build());
    }

    @Override
    public Mono<Void> deleteImage(String imageUrl) {
        return Mono.empty();
    }
}