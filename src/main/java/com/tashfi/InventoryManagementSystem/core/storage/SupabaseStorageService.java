package com.tashfi.InventoryManagementSystem.core.storage;

import com.tashfi.InventoryManagementSystem.core.exception.ValidationException;
import com.tashfi.InventoryManagementSystem.core.util.ImageUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Profile("dev")
public class SupabaseStorageService implements StorageService {

    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );

    private final WebClient webClient;
    private final String supabaseUrl;
    private final String bucket;
    private final ImageUtil imageUtil;

    public SupabaseStorageService(
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.anon-key}") String anonKey,
            @Value("${supabase.storage.bucket}") String bucket,
            ImageUtil imageUtil) {
        this.supabaseUrl = supabaseUrl;
        this.bucket = bucket;
        this.imageUtil = imageUtil;
        this.webClient = WebClient.builder()
                .baseUrl(supabaseUrl + "/storage/v1")
                .defaultHeader("Authorization", "Bearer " + anonKey)
                .defaultHeader("apikey", anonKey)
                .build();
    }

    @Override
    public Mono<List<String>> uploadImages(List<MultipartFile> files, String productIdentifier) {
        return Flux.fromIterable(files)
                .filter(file -> !file.isEmpty())
                .flatMapSequential(file -> uploadSingle(file, productIdentifier))
                .collectList();
    }

    private Mono<String> uploadSingle(MultipartFile file, String productIdentifier) {
        String contentType = file.getContentType() != null ? file.getContentType() : "";

        if (!ALLOWED_TYPES.contains(contentType))
            return Mono.error(new ValidationException("Only JPEG, PNG, and WEBP images are allowed"));

        if (file.getSize() > 5L * 1024 * 1024)
            return Mono.error(new ValidationException("Image must not exceed 5MB"));

        String fileName  = imageUtil.buildFileName(file.getOriginalFilename(), productIdentifier);
        String extension = imageUtil.getExtension(fileName);

        return Mono.fromCallable(() -> imageUtil.compressBytes(file.getBytes(), extension))
                .flatMap(compressed ->
                        webClient.post()
                                .uri("/object/" + bucket + "/" + fileName)
                                .contentType(MediaType.parseMediaType(
                                        contentType.equals("image/jpg") ? "image/jpeg" : contentType))
                                .header("x-upsert", "true")
                                .body(BodyInserters.fromValue(compressed))
                                .retrieve()
                                .onStatus(status -> !status.is2xxSuccessful(), response ->
                                        response.bodyToMono(String.class)
                                                .flatMap(body -> Mono.error(
                                                        new RuntimeException("Supabase upload failed: " + body)))
                                )
                                .bodyToMono(String.class)
                                .map(response ->
                                        supabaseUrl + "/storage/v1/object/public/"
                                                + bucket + "/" + fileName)
                );
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

                    return Mono.fromCallable(() ->
                            imageUtil.process(rawBytes, contentType, file.filename(), identifier, generateThumbnail));
                })
                .flatMap(processed -> {
                    Mono<String> originalUpload =
                            upload(processed.getOriginalBytes(), processed.getOriginalFileName(), processed.getContentType());
                    Mono<String> thumbnailUpload = processed.getThumbnailBytes() != null
                            ? upload(processed.getThumbnailBytes(), processed.getThumbnailFileName(), processed.getContentType())
                            : Mono.just("__NONE__");

                    return Mono.zip(originalUpload, thumbnailUpload)
                            .map(tuple -> UploadedImageUrls.builder()
                                    .imageUrl(tuple.getT1())
                                    .imageName(processed.getOriginalFileName())
                                    .thumbnailUrl(tuple.getT2().equals("__NONE__") ? null : tuple.getT2())
                                    .build());
                });
    }

    private Mono<String> upload(byte[] bytes, String fileName, String contentType) {
        return webClient.post()
                .uri("/object/" + bucket + "/" + fileName)
                .contentType(MediaType.parseMediaType(contentType))
                .header("x-upsert", "true")
                .body(BodyInserters.fromValue(bytes))
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("Supabase upload failed: " + body))))
                .bodyToMono(String.class)
                .map(r -> supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + fileName);
    }

    @Override
    public Mono<Void> deleteImage(String imageUrl) {
        String prefix = supabaseUrl + "/storage/v1/object/public/" + bucket + "/";
        if (imageUrl == null || !imageUrl.startsWith(prefix))
            return Mono.empty(); // not a URL we recognize — nothing to do

        String fileName = imageUrl.substring(prefix.length());

        return webClient.method(org.springframework.http.HttpMethod.DELETE)
                .uri("/object/" + bucket)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(java.util.Map.of("prefixes", List.of(fileName)))
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(
                                        new RuntimeException("Supabase delete failed: " + body)))
                )
                .bodyToMono(String.class)
                .then()
                .onErrorResume(e -> {
                    // log and swallow — a failed cleanup shouldn't fail the user's update/delete request
                    System.err.println("Failed to delete old image from Supabase: " + e.getMessage());
                    return Mono.empty();
                });
    }
}