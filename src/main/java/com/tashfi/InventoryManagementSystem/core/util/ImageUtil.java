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

    // target: output is ~25% of original file size
    // achieved by scaling dimensions to 70% + quality 0.6
    private static final double DIMENSION_SCALE = 0.70;
    private static final double OUTPUT_QUALITY   = 0.60;
    private static final long   MAX_BYTES        = 1024 * 1024; // 1 MB hard limit

    /**
     * Validate, compress and save an image to the given directory.
     *
     * @param file              the uploaded multipart file
     * @param productIdentifier a stable slug used in the file name
     * @param uploadDir         the absolute or relative directory to write into
     *                          (supplied by the calling storage service)
     * @return a public URL path fragment, e.g. {@code /uploads/products/<filename>}
     */
    public String saveImage(MultipartFile file, String productIdentifier, String uploadDir)
            throws IOException {

        String contentType = file.getContentType() != null ? file.getContentType() : "";

        if (!ALLOWED_TYPES.contains(contentType))
            throw new ValidationException("Only JPEG, PNG, and WEBP images are allowed");

        if (file.getSize() > MAX_BYTES)
            throw new ValidationException("Image must not exceed 1MB");

        String originalFilename = file.getOriginalFilename() != null
                ? sanitize(file.getOriginalFilename())
                : "image";

        String extension  = getExtension(originalFilename);
        String fileName   = buildFileName(originalFilename, productIdentifier);
        byte[] compressed = compressBytes(file.getBytes(), extension);

        Path dirPath = Paths.get(uploadDir);
        Files.createDirectories(dirPath);
        Files.write(dirPath.resolve(fileName), compressed);

        return "/uploads/products/" + fileName;
    }

    public byte[] compressBytes(byte[] bytes, String extension) throws IOException {
        // webp not supported by Thumbnailator output — validate size only
        if (extension.equalsIgnoreCase("webp")) {
            if (bytes.length > MAX_BYTES)
                throw new ValidationException("WEBP image must not exceed 1MB");
            return bytes;
        }

        String format = extension.equalsIgnoreCase("png") ? "png" : "jpeg";

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Thumbnails.of(new ByteArrayInputStream(bytes))
                .scale(DIMENSION_SCALE)
                .outputFormat(format)
                .outputQuality(OUTPUT_QUALITY)
                .toOutputStream(out);

        return out.toByteArray();
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