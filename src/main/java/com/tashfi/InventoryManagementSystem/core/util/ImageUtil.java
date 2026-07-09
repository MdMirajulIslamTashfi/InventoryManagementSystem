package com.tashfi.InventoryManagementSystem.core.util;

import com.tashfi.InventoryManagementSystem.core.exception.ValidationException;
import net.coobird.thumbnailator.Thumbnails;
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
import java.util.UUID;

@Component
public class ImageUtil {

    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp", "image/heic", "image/heif"
    );

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    // target: output is ~25% of original file size
    // achieved by scaling dimensions to 70% + quality 0.6
    private static final double DIMENSION_SCALE = 0.70;
    private static final double OUTPUT_QUALITY   = 0.60;
    private static final long   MAX_BYTES        = 5L * 1024 * 1024; // 5 MB hard limit (all types)

    private static final int THUMBNAIL_WIDTH  = 400;
    private static final int THUMBNAIL_HEIGHT = 300;

    private final HeifConverter heifConverter;

    public ImageUtil(HeifConverter heifConverter) {
        this.heifConverter = heifConverter;
    }

    /**
     * CMS-style processing: validate, (convert HEIF→JPEG if needed), compress the original and
     * optionally produce a fixed-size thumbnail. Returns the bytes + generated file names so the
     * caller (a storage service) only has to persist them.
     */
    public ProcessedImage process(byte[] rawBytes, String contentType, String originalFilename,
                                  String identifier, boolean generateThumbnail) throws IOException {

        // 1. Validate content type
        validateContentType(contentType);

        // 2. Hard 5MB limit — rejected before any conversion/compression
        if (rawBytes.length > MAX_BYTES)
            throw new ValidationException("Image must not exceed 5MB. Received: "
                    + (rawBytes.length / 1024) + "KB");

        String safeFilename = originalFilename != null ? sanitize(originalFilename) : "image.jpg";
        String extension    = getExtension(safeFilename);

        // 3. HEIF/HEIC → convert to JPEG up front, then treat everything downstream as jpeg
        byte[] workingBytes = rawBytes;
        if (isHeif(contentType, extension)) {
            workingBytes = heifConverter.toJpeg(rawBytes);
            extension = "jpg";
        }

        String baseName = String.valueOf(UUID.randomUUID());

        // 4. Compress original (70% dimensions + 0.6 quality → ~25% of raw size)
        byte[] originalBytes    = compress(workingBytes, extension, null, null, DIMENSION_SCALE);
        String originalFileName = baseName + "." + extension;

        // 5. Thumbnail variant — 400×300 (only if requested)
        byte[] thumbnailBytes    = null;
        String thumbnailFileName = null;
        if (generateThumbnail) {
            thumbnailBytes    = compress(workingBytes, extension, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, null);
            thumbnailFileName = baseName + "_thumb." + extension;
        }

        String resolvedContentType = switch (extension.toLowerCase()) {
            case "png"  -> "image/png";
            case "webp" -> "image/webp";
            default     -> "image/jpeg";
        };

        return ProcessedImage.builder()
                .originalBytes(originalBytes)
                .originalFileName(originalFileName)
                .thumbnailBytes(thumbnailBytes)
                .thumbnailFileName(thumbnailFileName)
                .contentType(resolvedContentType)
                .build();
    }

    /**
     * Validate, compress and save an image to the given directory (legacy batch path used by
     * the product module's inline image upload).
     *
     * @return a public URL path fragment, e.g. {@code /uploads/products/<filename>}
     */
    public String saveImage(MultipartFile file, String productIdentifier, String uploadDir)
            throws IOException {

        String contentType = file.getContentType() != null ? file.getContentType() : "";

        if (!ALLOWED_TYPES.contains(contentType))
            throw new ValidationException("Only JPEG, PNG, WEBP and HEIF/HEIC images are allowed");

        if (file.getSize() > MAX_BYTES)
            throw new ValidationException("Image must not exceed 5MB. Received: "
                    + ((double) file.getSize() / (1024 * 1024)) + "MB");

        String originalFilename = file.getOriginalFilename() != null
                ? sanitize(file.getOriginalFilename())
                : "image";

        String extension  = getExtension(originalFilename);
        String fileName   = String.valueOf(UUID.randomUUID());
        byte[] compressed = compressBytes(file.getBytes(), extension);

        Path dirPath = Paths.get(uploadDir);
        Files.createDirectories(dirPath);
        Files.write(dirPath.resolve(fileName), compressed);

        return "/uploads/products/" + fileName;
    }

    public byte[] compressBytes(byte[] bytes, String extension) throws IOException {
        // HEIF/HEIC not readable by Thumbnailator — convert to JPEG first
        if (extension.equalsIgnoreCase("heic") || extension.equalsIgnoreCase("heif")) {
            bytes = heifConverter.toJpeg(bytes);
            extension = "jpg";
        }

        // webp not supported by Thumbnailator output — validate size only
        if (extension.equalsIgnoreCase("webp")) {
            if (bytes.length > MAX_BYTES)
                throw new ValidationException("WEBP image must not exceed 5MB");
            return bytes;
        }

        return compress(bytes, extension, null, null, DIMENSION_SCALE);
    }

    // Shared compression: scale-down (original) or fixed-size (thumbnail). Mirrors CMS ImageUtil.
    private byte[] compress(byte[] bytes, String extension, Integer width, Integer height, Double scale)
            throws IOException {

        // WebP: Thumbnailator cannot encode WebP, pass through as-is
        if (extension.equalsIgnoreCase("webp"))
            return bytes;

        String format = extension.equalsIgnoreCase("png") ? "png" : "jpeg";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        var builder = Thumbnails.of(new ByteArrayInputStream(bytes));

        if (width != null && height != null) {
            builder.size(width, height).keepAspectRatio(true);
        } else {
            // PNG gets more aggressive dimension scaling since it can't use lossy quality
            double effectiveScale = extension.equalsIgnoreCase("png") ? 0.50 : scale;
            builder.scale(effectiveScale);
        }

        builder.outputFormat(format)
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
        if (filename == null || !filename.contains(".")) return "jpg";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private void validateContentType(String contentType) {
        String ct = contentType != null ? contentType.toLowerCase() : "";
        if (!ALLOWED_TYPES.contains(ct))
            throw new ValidationException(
                    "Only JPEG, PNG, WEBP and HEIF/HEIC images are allowed. Received: " + ct);
    }

    private boolean isHeif(String contentType, String extension) {
        String ct = contentType != null ? contentType.toLowerCase() : "";
        return ct.contains("heic") || ct.contains("heif")
                || extension.equalsIgnoreCase("heic") || extension.equalsIgnoreCase("heif");
    }

    private String sanitize(String input) {
        return input.replaceAll("[^a-zA-Z0-9._-]", "_").toLowerCase();
    }

    private String stripExtension(String filename) {
        if (!filename.contains(".")) return filename;
        return filename.substring(0, filename.lastIndexOf('.'));
    }
}
