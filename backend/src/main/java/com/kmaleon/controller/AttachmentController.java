package com.kmaleon.controller;

import com.kmaleon.dto.ParseSwiftRequest;
import com.kmaleon.dto.SwiftMetadataResponse;
import com.kmaleon.service.StorageService;
import com.kmaleon.service.SwiftPdfParser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {

    private final StorageService storageService;
    private final SwiftPdfParser swiftPdfParser;

    public AttachmentController(StorageService storageService, SwiftPdfParser swiftPdfParser) {
        this.storageService = storageService;
        this.swiftPdfParser = swiftPdfParser;
    }

    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }
        String url = storageService.upload(file, StorageService.BUCKET_FINANCIAL);
        return Map.of("url", url);
    }

    @PostMapping("/parse-swift")
    public SwiftMetadataResponse parseSwift(@Valid @RequestBody ParseSwiftRequest request) {
        try (InputStream stream = URI.create(request.getUrl()).toURL().openStream()) {
            return swiftPdfParser.parse(stream);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se pudo acceder o leer el PDF: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "URL inválida: " + e.getMessage());
        }
    }
}
