package com.tashfi.InventoryManagementSystem.core.storage;

import com.tashfi.InventoryManagementSystem.core.util.ImageUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@Profile("local")
public class LocalStorageService implements StorageService {

    private final ImageUtil imageUtil;
    private final String uploadPath;

    public LocalStorageService(
            ImageUtil imageUtil,
            @Value("${app.upload.path}") String uploadPath) {
        this.imageUtil  = imageUtil;
        this.uploadPath = uploadPath;
    }

    @Override
    public Mono<List<String>> uploadImages(List<MultipartFile> files, String productIdentifier) {
        return Mono.fromCallable(() -> {
            List<String> paths = new ArrayList<>();
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    paths.add(imageUtil.saveImage(file, productIdentifier, uploadPath));
                }
            }
            return paths;
        });
    }

    @Override
    public Mono<Void> deleteImage(String imageUrl) {
        return Mono.fromRunnable(() -> {
            if (imageUrl == null || imageUrl.isBlank())
                return;

            String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
            Path filePath = Paths.get(uploadPath).resolve(fileName);

            try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // log and swallow — cleanup failure shouldn't fail the user's request
                System.err.println("Failed to delete local image file: " + filePath + " — " + e.getMessage());
            }
        });
    }
}