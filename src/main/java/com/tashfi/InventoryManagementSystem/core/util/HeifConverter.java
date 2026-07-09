package com.tashfi.InventoryManagementSystem.core.util;

import com.tashfi.InventoryManagementSystem.core.exception.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * Converts HEIF/HEIC image bytes to JPEG by shelling out to libheif's
 * {@code heif-convert} CLI (installed on the host at /usr/bin/heif-convert).
 *
 * Kept intentionally simple: write the input to a temp file, run heif-convert,
 * read the produced JPEG back, and clean up. The call is blocking, so callers
 * must invoke it from a blocking-safe context (e.g. inside {@code Mono.fromCallable}).
 */
@Component
public class HeifConverter {

    private final String convertBin;

    public HeifConverter(@Value("${app.heif.convert-bin:/usr/bin/heif-convert}") String convertBin) {
        this.convertBin = convertBin;
    }

    public byte[] toJpeg(byte[] heifBytes) {
        Path inputFile = null;
        Path outputFile = null;
        try {
            inputFile = Files.createTempFile("heif-in-", ".heic");
            outputFile = Files.createTempFile("heif-out-", ".jpg");
            Files.write(inputFile, heifBytes);
            // heif-convert refuses to overwrite an existing file, so delete the placeholder first
            Files.deleteIfExists(outputFile);

            Process process = new ProcessBuilder(
                    convertBin, "-q", "90",
                    inputFile.toString(), outputFile.toString())
                    .redirectErrorStream(true)
                    .start();

            boolean finished = process.waitFor(60, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new ValidationException("HEIF conversion timed out");
            }
            if (process.exitValue() != 0)
                throw new ValidationException("HEIF conversion failed (exit " + process.exitValue() + ")");

            if (!Files.exists(outputFile) || Files.size(outputFile) == 0)
                throw new ValidationException("HEIF conversion produced no output");

            return Files.readAllBytes(outputFile);
        } catch (IOException e) {
            throw new ValidationException("Failed to convert HEIF image: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ValidationException("HEIF conversion was interrupted");
        } finally {
            deleteQuietly(inputFile);
            deleteQuietly(outputFile);
        }
    }

    private void deleteQuietly(Path path) {
        if (path == null) return;
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // temp-file cleanup failure is not worth failing the request
        }
    }
}
