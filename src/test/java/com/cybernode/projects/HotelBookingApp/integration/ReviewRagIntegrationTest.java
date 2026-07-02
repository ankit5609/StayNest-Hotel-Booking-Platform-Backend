package com.cybernode.projects.HotelBookingApp.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
public class ReviewRagIntegrationTest {

    @TestConfiguration
    static class Config {
        @Bean
        public EmbeddingModel embeddingModel() {
            return new EmbeddingModel() {
                @Override
                public EmbeddingResponse call(EmbeddingRequest request) {
                    float[] vector = new float[1536];
                    vector[0] = 1.0f;
                    return new EmbeddingResponse(List.of(new Embedding(vector, 0)));
                }

                @Override
                public float[] embed(Document document) {
                    float[] vector = new float[1536];
                    vector[0] = 1.0f;
                    return vector;
                }
            };
        }
    }

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("pgvector/pgvector:pg16")
            .withDatabaseName("hotel_db")
            .withUsername("postgres")
            .withPassword("postgres123");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private VectorStore vectorStore;

    @Test
    public void testVectorStoreOperations() {
        String docId = java.util.UUID.randomUUID().toString();
        Document doc = new Document(
                docId,
                "The breakfast buffet was amazing with many options.",
                Map.of("hotelId", 10L)
        );

        vectorStore.add(List.of(doc));

        SearchRequest request = SearchRequest.builder()
                .query("breakfast")
                .topK(1)
                .build();

        List<Document> results = vectorStore.similaritySearch(request);

        assertFalse(results.isEmpty());
        assertEquals(docId, results.get(0).getId());
        assertEquals("The breakfast buffet was amazing with many options.", results.get(0).getText());
    }
}
