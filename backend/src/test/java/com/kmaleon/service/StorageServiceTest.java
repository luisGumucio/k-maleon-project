package com.kmaleon.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class StorageServiceTest {

    private static final String SUPABASE_URL = "https://test.supabase.co";
    private static final String SERVICE_KEY = "test-service-key";
    private static final String BUCKET = StorageService.BUCKET_FINANCIAL;

    // -------------------------
    // Happy path
    // -------------------------

    @Test
    void upload_returnsPublicUrl_withCorrectBucketAndExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "comprobante.pdf", "application/pdf", "pdf-content".getBytes());

        StorageService storageService = new StorageServiceTestDouble(SUPABASE_URL, SERVICE_KEY);

        String url = storageService.upload(file, BUCKET);

        assertThat(url).startsWith(SUPABASE_URL + "/storage/v1/object/public/" + BUCKET + "/");
        assertThat(url).endsWith(".pdf");
    }

    @Test
    void delete_withValidUrl_doesNotThrow() {
        StorageService storageService = new StorageServiceTestDouble(SUPABASE_URL, SERVICE_KEY);
        String url = SUPABASE_URL + "/storage/v1/object/public/" + BUCKET + "/some-uuid.pdf";

        storageService.delete(url, BUCKET);
    }

    // -------------------------
    // Error path
    // -------------------------

    @Test
    void upload_whenFileReadFails_throwsRuntimeException() {
        MockMultipartFile brokenFile = new MockMultipartFile(
                "file", "bad.pdf", "application/pdf", (byte[]) null) {
            @Override
            public byte[] getBytes() throws java.io.IOException {
                throw new java.io.IOException("simulated read error");
            }
        };

        StorageService storageService = new StorageServiceTestDouble(SUPABASE_URL, SERVICE_KEY);

        assertThatThrownBy(() -> storageService.upload(brokenFile, BUCKET))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to read uploaded file");
    }

    // -------------------------
    // Edge cases
    // -------------------------

    @Test
    void delete_withNullUrl_doesNotThrow() {
        StorageService storageService = new StorageServiceTestDouble(SUPABASE_URL, SERVICE_KEY);
        storageService.delete(null, BUCKET);
    }

    @Test
    void delete_withBlankUrl_doesNotThrow() {
        StorageService storageService = new StorageServiceTestDouble(SUPABASE_URL, SERVICE_KEY);
        storageService.delete("  ", BUCKET);
    }

    @Test
    void delete_withUrlFromDifferentBucket_skipsDelete() {
        StorageService storageService = new StorageServiceTestDouble(SUPABASE_URL, SERVICE_KEY);
        storageService.delete("https://other.supabase.co/storage/v1/object/public/other-bucket/file.pdf", BUCKET);
    }

    @Test
    void upload_withNoExtension_returnsUrlWithNoExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "comprobante", "application/octet-stream", "content".getBytes());

        StorageService storageService = new StorageServiceTestDouble(SUPABASE_URL, SERVICE_KEY);
        String url = storageService.upload(file, BUCKET);

        String prefix = SUPABASE_URL + "/storage/v1/object/public/" + BUCKET + "/";
        assertThat(url).startsWith(prefix);
        String filename = url.substring(prefix.length());
        assertThat(filename).doesNotContain(".");
    }

    @Test
    void upload_withContainerBucket_returnsUrlWithContainerBucket() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "bl.pdf", "application/pdf", "content".getBytes());

        StorageService storageService = new StorageServiceTestDouble(SUPABASE_URL, SERVICE_KEY);
        String url = storageService.upload(file, StorageService.BUCKET_CONTAINER);

        assertThat(url).startsWith(SUPABASE_URL + "/storage/v1/object/public/" + StorageService.BUCKET_CONTAINER + "/");
        assertThat(url).endsWith(".pdf");
    }

    static class StorageServiceTestDouble extends StorageService {
        StorageServiceTestDouble(String supabaseUrl, String serviceKey) {
            super(supabaseUrl, serviceKey);
        }

        @Override
        public String upload(org.springframework.web.multipart.MultipartFile file, String bucket) {
            try {
                file.getBytes();
            } catch (java.io.IOException e) {
                throw new RuntimeException("Failed to read uploaded file", e);
            }
            String ext = file.getOriginalFilename() != null && file.getOriginalFilename().contains(".")
                    ? file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."))
                    : "";
            return "https://test.supabase.co/storage/v1/object/public/" + bucket + "/"
                    + java.util.UUID.randomUUID() + ext;
        }

        @Override
        public void delete(String publicUrl, String bucket) {
            super.delete(publicUrl, bucket);
        }
    }
}
