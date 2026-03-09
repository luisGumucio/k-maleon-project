package com.kmaleon.controller;

import com.kmaleon.security.Roles;
import com.kmaleon.service.StorageService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/attachments")
@PreAuthorize(Roles.ADMIN_OR_SUPER)
public class AttachmentController {

    private final StorageService storageService;

    public AttachmentController(StorageService storageService) {
        this.storageService = storageService;
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
}
