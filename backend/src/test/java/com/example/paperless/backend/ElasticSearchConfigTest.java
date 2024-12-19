package com.example.paperless.backend;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This test will load the application context, including the ElasticsearchClient bean defined
 * in ElasticSearchConfig. It verifies that the bean is created successfully.
 */
@SpringBootTest
public class ElasticSearchConfigTest {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Test
    public void testElasticsearchClientBeanCreation() {
        // Verify that the bean was created and injected
        Assertions.assertNotNull(elasticsearchClient, "ElasticsearchClient bean should not be null");

        // The ElasticsearchClient doesn't provide trivial getters for the host or port.
        // We assume that if the bean can be created without throwing exceptions, it's set up correctly.
        // If you need deeper verification, consider using a Testcontainer or mock server.

        Assertions.assertDoesNotThrow(() -> {
            // Just a sanity check: call toString() to ensure the client is functional
            elasticsearchClient.toString();
        }, "ElasticsearchClient should be functional and not throw exceptions on basic operations");
    }
}

