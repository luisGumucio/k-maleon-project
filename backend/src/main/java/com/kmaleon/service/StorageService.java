package com.kmaleon.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class StorageService {

    private static final Logger log = LoggerFactory.getLogger(StorageService.class);

    public static final String BUCKET_FINANCIAL = "financial-docs";
    public static final String BUCKET_CONTAINER = "container-docs";

    private final RestClient restClient;
    private final String supabaseUrl;

    public StorageService(
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.service-key}") String serviceKey) {
        this.supabaseUrl = supabaseUrl;
        this.restClient = RestClient.builder()
                .baseUrl(supabaseUrl + "/storage/v1")
                .defaultHeader("Authorization", "Bearer " + serviceKey)
                .defaultHeader("apikey", serviceKey)
                .build();
    }

    public String upload(MultipartFile file, String bucket) {
        String extension = getExtension(file.getOriginalFilename());
        String path = UUID.randomUUID() + extension;

        try {
            restClient.post()
                    .uri("/object/" + bucket + "/" + path)
                    .contentType(MediaType.parseMediaType(
                            file.getContentType() != null ? file.getContentType() : "application/octet-stream"))
                    .body(file.getBytes())
                    .retrieve()
                    .toBodilessEntity();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file", e);
        }

        String publicUrl = supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + path;
        log.info("File uploaded to Supabase Storage bucket={}: {}", bucket, publicUrl);
        return publicUrl;
    }

    public void delete(String publicUrl, String bucket) {
        if (publicUrl == null || publicUrl.isBlank()) return;

        String prefix = supabaseUrl + "/storage/v1/object/public/" + bucket + "/";
        if (!publicUrl.startsWith(prefix)) {
            log.warn("Skipping delete — URL does not belong to bucket {}: {}", bucket, publicUrl);
            return;
        }

        String path = publicUrl.substring(prefix.length());

        try {
            restClient.delete()
                    .uri("/object/" + bucket + "/" + path)
                    .retrieve()
                    .toBodilessEntity();
            log.info("File deleted from Supabase Storage bucket={}: {}", bucket, path);
        } catch (Exception e) {
            log.error("Failed to delete file from Supabase Storage bucket={}: {}", bucket, path, e);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }
}
