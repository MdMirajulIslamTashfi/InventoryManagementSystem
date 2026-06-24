package com.tashfi.InventoryManagementSystem.content.adapter.out;

import com.tashfi.InventoryManagementSystem.content.application.port.out.ContentServiceClientPort;
import com.tashfi.InventoryManagementSystem.content.application.port.out.dto.ContentImageBatchResponseDto;
import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

// Talks to the contentmanagementsystem (CMS) microservice over HTTP.
// Only registered when "content" is one of the active profiles —
// run with e.g. --spring.profiles.active=local,content
@Component
@Profile("content")
public class ContentServiceClientAdapter implements ContentServiceClientPort {

    private final WebClient webClient;

    public ContentServiceClientAdapter(
            @Value("${cms.base-url}") String cmsBaseUrl,
            @Value("${cms.connect-timeout-ms:5000}") int connectTimeoutMs,
            @Value("${cms.response-timeout-ms:15000}") long responseTimeoutMs) {

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
                .responseTimeout(Duration.ofMillis(responseTimeoutMs));

        this.webClient = WebClient.builder()
                .baseUrl(cmsBaseUrl + "/api/content")
                .clientConnector(new org.springframework.http.client.reactive.ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Override
    public Mono<ContentImageBatchResponseDto> uploadImages(UUID productId,
                                                           String productName,
                                                           String productSku,
                                                           String createdBy,
                                                           Boolean isThumbnail,
                                                           List<MultipartFile> files) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("productName", productName);
        if (productSku != null) builder.part("productSku", productSku);
        if (createdBy != null) builder.part("createdBy", createdBy);
        if (isThumbnail != null) builder.part("isThumbnail", isThumbnail.toString());

        try {
            for (MultipartFile file : files) {
                String contentType = file.getContentType() != null ? file.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
                builder.part("files", new ByteArrayResource(file.getBytes()) {
                            @Override
                            public String getFilename() {
                                return file.getOriginalFilename();
                            }
                        })
                        .contentType(MediaType.parseMediaType(contentType));
            }
        } catch (IOException e) {
            return Mono.error(new RuntimeException("Failed to read image bytes for CMS upload", e));
        }

        return webClient.post()
                .uri("/products/{productId}/images", productId)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), response -> response.bodyToMono(String.class)
                        .flatMap(body -> Mono.error(new RuntimeException("CMS upload failed: " + body))))
                .bodyToMono(ContentImageBatchResponseDto.class);
    }

    @Override
    public Mono<ContentImageBatchResponseDto> findAllByProductId(UUID productId) {
        return webClient.get()
                .uri("/products/{productId}/images", productId)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), response -> response.bodyToMono(String.class)
                        .flatMap(body -> Mono.error(new RuntimeException("CMS fetch failed: " + body))))
                .bodyToMono(ContentImageBatchResponseDto.class);
    }

    @Override
    public Mono<Void> deleteImage(UUID productId, UUID imageId) {
        return webClient.delete()
                .uri("/products/{productId}/images/{imageId}", productId, imageId)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), response -> response.bodyToMono(String.class)
                        .flatMap(body -> Mono.error(new RuntimeException("CMS delete failed: " + body))))
                .toBodilessEntity()
                .then();
    }

    @Override
    public Mono<Void> deleteAllByProductId(UUID productId) {
        return webClient.delete()
                .uri("/products/{productId}/images", productId)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), response -> response.bodyToMono(String.class)
                        .flatMap(body -> Mono.error(new RuntimeException("CMS delete-all failed: " + body))))
                .toBodilessEntity()
                .then();
    }
}