package com.tashfi.InventoryManagementSystem.productimage.adapter.in.handler;

import com.tashfi.InventoryManagementSystem.core.exception.ValidationException;
import com.tashfi.InventoryManagementSystem.productimage.application.port.in.ProductImageUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductImageHandler {

    private final ProductImageUseCase productImageUseCase;

    public Mono<ServerResponse> getImage(ServerRequest request) {
        String name = request.pathVariable("name");
        UUID imageId = UUID.fromString(request.pathVariable("id"));

        return productImageUseCase.findImageById(name, imageId)
                .flatMap(res -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(res));
    }

    public Mono<ServerResponse> updateImage(ServerRequest request) {
        String name = request.pathVariable("name");
        UUID imageId = UUID.fromString(request.pathVariable("id"));

        return request.multipartData().flatMap(multipart -> {
                    // accept either "image" (singular, recommended for this endpoint) or "images"
                    List<Part> parts = multipart.get("image");
                    if (parts == null || parts.isEmpty())
                        parts = multipart.get("images");

                    if (parts == null || parts.isEmpty() || !(parts.get(0) instanceof FilePart filePart))
                        return Mono.<MultipartFile>error(new ValidationException("No image file provided"));

                    return filePart.content()
                            .reduce(new ByteArrayOutputStream(), (baos, dataBuffer) -> {
                                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(bytes);
                                try { baos.write(bytes); }
                                catch (IOException e) { throw new RuntimeException(e); }
                                return baos;
                            })
                            .map(baos -> (MultipartFile) new MockMultipartFile(
                                    "image",
                                    filePart.filename(),
                                    filePart.headers().getContentType() != null
                                            ? filePart.headers().getContentType().toString()
                                            : "image/jpeg",
                                    baos.toByteArray()
                            ));
                })
                .flatMap(image -> productImageUseCase.updateImage(name, imageId, image))
                .flatMap(res -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(res));
    }

    public Mono<ServerResponse> deleteImage(ServerRequest request) {
        String name = request.pathVariable("name");
        UUID imageId = UUID.fromString(request.pathVariable("id"));

        return productImageUseCase.deleteImage(name, imageId)
                .then(ServerResponse.noContent().build());
    }
}