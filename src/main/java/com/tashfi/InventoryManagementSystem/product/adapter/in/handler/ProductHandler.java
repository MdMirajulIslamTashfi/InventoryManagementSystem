package com.tashfi.InventoryManagementSystem.product.adapter.in.handler;

import com.tashfi.InventoryManagementSystem.core.enums.ProductStatus;
import com.tashfi.InventoryManagementSystem.product.application.port.in.ProductUseCase;
import com.tashfi.InventoryManagementSystem.product.application.port.in.dto.request.ProductRequestDto;
import com.tashfi.InventoryManagementSystem.product.application.port.in.dto.request.ProductUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductHandler {

    private final ProductUseCase productUseCase;

    public Mono<ServerResponse> getAllProducts(ServerRequest request) {
        return productUseCase.findAllProducts()
                .flatMap(res -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(res));
    }

    public Mono<ServerResponse> searchProducts(ServerRequest request) {
        String name = request.pathVariable("name");
        return productUseCase.searchProductsByName(name)
                .flatMap(res -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(res));
    }

    public Mono<ServerResponse> createProduct(ServerRequest request) {
        return request.multipartData().flatMap(multipart -> {
                    String categoryName = getFormField(multipart, "categoryName");
                    String name         = getFormField(multipart, "name");
                    String description  = getFormField(multipart, "description");
                    String quantityStr  = getFormField(multipart, "quantity");
                    String priceStr     = getFormField(multipart, "price");
                    String sku          = getFormField(multipart, "sku");
                    String statusStr    = getFormField(multipart, "status");

                    ProductRequestDto dto = ProductRequestDto.builder()
                            .categoryName(categoryName)
                            .name(name)
                            .description(description)
                            .quantity(quantityStr != null ? Integer.parseInt(quantityStr) : null)
                            .price(priceStr != null ? new java.math.BigDecimal(priceStr) : null)
                            .sku(sku)
                            .status(statusStr != null ? ProductStatus.valueOf(statusStr.toUpperCase()) : null)
                            .build();

                    // collect ALL file parts — supports both "image" and "images" key names
                    List<FilePart> fileParts = new ArrayList<>();
                    if (multipart.get("images") != null)
                        multipart.get("images").stream()
                                .filter(p -> p instanceof FilePart)
                                .map(p -> (FilePart) p)
                                .forEach(fileParts::add);
                    if (multipart.get("image") != null)
                        multipart.get("image").stream()
                                .filter(p -> p instanceof FilePart)
                                .map(p -> (FilePart) p)
                                .forEach(fileParts::add);

                    if (fileParts.isEmpty())
                        return productUseCase.createProduct(dto, List.of());

                    // collect bytes from all FileParts reactively
                    return Flux.fromIterable(fileParts)
                            .flatMapSequential(filePart ->
                                    filePart.content()
                                            .reduce(new ByteArrayOutputStream(), (baos, dataBuffer) -> {
                                                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                                                dataBuffer.read(bytes);
                                                try { baos.write(bytes); }
                                                catch (IOException e) { throw new RuntimeException(e); }
                                                return baos;
                                            })
                                            .map(baos -> {
                                                String filename = filePart.filename();
                                                String contentType = filePart.headers().getContentType() != null
                                                        ? filePart.headers().getContentType().toString()
                                                        : "image/jpeg";
                                                return (MultipartFile) new MockMultipartFile(
                                                        "images", filename, contentType, baos.toByteArray()
                                                );
                                            })
                            )
                            .collectList()
                            .flatMap(images -> productUseCase.createProduct(dto, images));
                })
                .flatMap(res -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(res));
    }

    public Mono<ServerResponse> updateProduct(ServerRequest request) {
        String name = request.pathVariable("name");

        return request.multipartData().flatMap(multipart -> {
                    String categoryName = getFormField(multipart, "categoryName");
                    String newName      = getFormField(multipart, "name");
                    String description  = getFormField(multipart, "description");
                    String quantityStr  = getFormField(multipart, "quantity");
                    String priceStr     = getFormField(multipart, "price");
                    String sku          = getFormField(multipart, "sku");
                    String statusStr    = getFormField(multipart, "status");

                    ProductUpdateRequestDto dto = ProductUpdateRequestDto.builder()
                            .categoryName(categoryName)
                            .name(newName)
                            .description(description)
                            .quantity(quantityStr != null ? Integer.parseInt(quantityStr) : null)
                            .price(priceStr != null ? new java.math.BigDecimal(priceStr) : null)
                            .sku(sku)
                            .status(statusStr != null ? ProductStatus.valueOf(statusStr.toUpperCase()) : null)
                            .build();

                    // collect new images — optional
                    List<FilePart> fileParts = new ArrayList<>();
                    if (multipart.get("images") != null)
                        multipart.get("images").stream()
                                .filter(p -> p instanceof FilePart)
                                .map(p -> (FilePart) p)
                                .forEach(fileParts::add);
                    if (multipart.get("image") != null)
                        multipart.get("image").stream()
                                .filter(p -> p instanceof FilePart)
                                .map(p -> (FilePart) p)
                                .forEach(fileParts::add);

                    if (fileParts.isEmpty())
                        return productUseCase.updateProduct(name, dto, List.of());

                    return Flux.fromIterable(fileParts)
                            .flatMapSequential(filePart ->
                                    filePart.content()
                                            .reduce(new ByteArrayOutputStream(), (baos, dataBuffer) -> {
                                                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                                                dataBuffer.read(bytes);
                                                try { baos.write(bytes); }
                                                catch (IOException e) { throw new RuntimeException(e); }
                                                return baos;
                                            })
                                            .map(baos -> (MultipartFile) new MockMultipartFile(
                                                    "images",
                                                    filePart.filename(),
                                                    filePart.headers().getContentType() != null
                                                            ? filePart.headers().getContentType().toString()
                                                            : "image/jpeg",
                                                    baos.toByteArray()
                                            ))
                            )
                            .collectList()
                            .flatMap(images -> productUseCase.updateProduct(name, dto, images));
                })
                .flatMap(res -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(res));
    }

    public Mono<ServerResponse> deleteProduct(ServerRequest request) {
        String name = request.pathVariable("name");
        return productUseCase.deleteProduct(name)
                .then(ServerResponse.noContent().build());
    }

    // ── helpers ──────────────────────────────────────────────

    private String getFormField(MultiValueMap<String, Part> multipart, String fieldName) {
        Part part = multipart.getFirst(fieldName);
        if (part instanceof FormFieldPart f)
            return f.value();
        return null;
    }
}