package com.example.paperless.backend;

import io.minio.MinioClient;
import io.minio.errors.MinioException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This test uses Spring Boot's test framework to load the application context,
 * including the MinioConfig bean definitions. We provide test property values
 * via @SpringBootTest properties.
 */
@SpringBootTest(properties = {
        "minio.url=http://localhost:9000",
        "minio.accessKey=testAccessKey",
        "minio.secretKey=testSecretKey"
})
public class MinioConfigTest {

    @Autowired
    private MinioClient minioClient;

    @Test
    public void testMinioClientBeanCreation() throws MinioException {
        // The minioClient bean should be injected by Spring
        Assertions.assertNotNull(minioClient, "MinioClient bean should not be null");

        // There's no direct accessor for endpoint/credentials in the current MinioClient builder,
        // but we can try invoking a simple method or rely on the fact that if it was null,
        // the bean creation would have failed.
        // For deeper verification, you'd need a different approach or rely on the assumptions
        // that if minioClient is built successfully, it used the provided values.

        // Since MinioClient doesn't provide direct getters for endpoint or credentials,
        // we mainly check that the bean is created successfully without throwing exceptions.

        // If needed, you could try a mock server or a more elaborate integration test.
        Assertions.assertDoesNotThrow(() -> {
            // Attempt a no-op operation like minioClient.toString() just to ensure it's functional
            minioClient.toString();
        }, "MinioClient should be functional and not throw exceptions on basic operations");
    }
}

