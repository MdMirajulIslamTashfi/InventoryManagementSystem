package com.tashfi.InventoryManagementSystem.core.util;

import com.tashfi.InventoryManagementSystem.core.exception.ValidationException;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ImageUtil {

    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    @Value("${app.upload.path}")
    private String uploadPath;

    // used by LocalStorageService — saves to disk
    public String saveImage(MultipartFile file, String productIdentifier) throws IOException {
        String contentType = file.getContentType() != null ? file.getContentType() : "";

        if (!ALLOWED_TYPES.contains(contentType))
            throw new ValidationException("Only JPEG, PNG, and WEBP images are allowed");

        String originalFilename = file.getOriginalFilename() != null
                ? sanitize(file.getOriginalFilename())
                : "image";

        String extension    = getExtension(originalFilename);
        String timestamp    = LocalDateTime.now().format(FORMATTER);
        String identifier   = sanitize(productIdentifier);
        String fileName     = identifier + "_" + stripExtension(originalFilename)
                + "_" + timestamp + "." + extension;

        byte[] compressed = compressBytes(file.getBytes(), extension);

        Path dirPath = Paths.get(uploadPath);
        Files.createDirectories(dirPath);
        Files.write(dirPath.resolve(fileName), compressed);

        return "/uploads/products/" + fileName;
    }

    // used by SupabaseStorageService — compresses bytes before uploading
    public byte[] compressBytes(byte[] bytes, String extension) throws IOException {
        if (extension.equalsIgnoreCase("webp")) return bytes;

        String format = extension.equalsIgnoreCase("png") ? "png" : "jpeg";

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Thumbnails.of(new ByteArrayInputStream(bytes))
                .scale(1.0)
                .outputFormat(format)
                .outputQuality(0.5)
                .toOutputStream(out);

        byte[] compressed = out.toByteArray();
        return compressed.length < bytes.length ? compressed : bytes;
    }

    public String buildFileName(String originalFilename, String productIdentifier) {
        String sanitizedOriginal = sanitize(originalFilename != null ? originalFilename : "image");
        String extension         = getExtension(sanitizedOriginal);
        String timestamp         = LocalDateTime.now().format(FORMATTER);
        String identifier        = sanitize(productIdentifier);
        return identifier + "_" + stripExtension(sanitizedOriginal) + "_" + timestamp + "." + extension;
    }

    public String getExtension(String filename) {
        if (!filename.contains(".")) return "jpg";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private String sanitize(String input) {
        return input.replaceAll("[^a-zA-Z0-9._-]", "_").toLowerCase();
    }

    private String stripExtension(String filename) {
        if (!filename.contains(".")) return filename;
        return filename.substring(0, filename.lastIndexOf('.'));
    }
}