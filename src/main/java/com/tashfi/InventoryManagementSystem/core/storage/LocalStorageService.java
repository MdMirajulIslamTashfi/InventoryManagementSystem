package com.tashfi.InventoryManagementSystem.core.storage;

import com.tashfi.InventoryManagementSystem.core.util.ImageUtil;
import com.tashfi.InventoryManagementSystem.core.util.ProcessedImage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
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
    public Mono<UploadedImageUrls> uploadImage(FilePart file, String identifier, boolean generateThumbnail) {
        String contentType = file.headers().getContentType() != null
                ? file.headers().getContentType().toString()
                : "image/jpeg";

        return DataBufferUtils.join(file.content())
                .flatMap(dataBuffer -> {
                    byte[] rawBytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(rawBytes);
                    DataBufferUtils.release(dataBuffer);

                    return Mono.fromCallable(() -> {
                        ProcessedImage processed =
                                imageUtil.process(rawBytes, contentType, file.filename(), identifier, generateThumbnail);

                        Path dirPath = Paths.get(uploadPath);
                        Files.createDirectories(dirPath);

                        Files.write(dirPath.resolve(processed.getOriginalFileName()), processed.getOriginalBytes());
                        String imageUrl = "/uploads/products/" + processed.getOriginalFileName();

                        String thumbnailUrl = null;
                        if (processed.getThumbnailBytes() != null) {
                            Files.write(dirPath.resolve(processed.getThumbnailFileName()), processed.getThumbnailBytes());
                            thumbnailUrl = "/uploads/products/" + processed.getThumbnailFileName();
                        }

                        return UploadedImageUrls.builder()
                                .imageUrl(imageUrl)
                                .imageName(processed.getOriginalFileName())
                                .thumbnailUrl(thumbnailUrl)
                                .build();
                    });
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
