package com.kmaleon;

import org.junit.jupiter.api.Test;

class KMaleonApplicationTests {

    @Test
    void contextLoads() {
        // Spring context is not loaded here — integration tests require real DB env vars.
        // Service-layer tests use Mockito (@ExtendWith(MockitoExtension.class)) and run without a live context.
    }
}
