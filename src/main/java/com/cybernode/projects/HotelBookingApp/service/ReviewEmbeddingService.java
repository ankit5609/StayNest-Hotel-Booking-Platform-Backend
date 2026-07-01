package com.cybernode.projects.HotelBookingApp.service;

import com.cybernode.projects.HotelBookingApp.entity.Review;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Keeps the pgvector store in sync with Review rows.
 * Best-effort, like NotificationService -- never breaks the review transaction.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewEmbeddingService {

    private final VectorStore vectorStore;

    @Value("${review.qa.enabled}")
    private boolean enabled;

    public void indexReview(Review review) {
        if (!enabled || review.getComment() == null || review.getComment().isBlank()) {
            return;
        }
        try {
            Document doc = new Document(
                    review.getId().toString(),
                    review.getComment(),
                    Map.of(
                            "reviewId", review.getId(),
                            "hotelId", review.getHotel().getId(),
                            "rating", review.getRating()
                    )
            );
            vectorStore.add(List.of(doc));
        } catch (Exception e) {
            log.warn("Failed to index review {} for RAG search: {}", review.getId(), e.getMessage());
        }
    }

    public void deleteReview(Long reviewId) {
        if (!enabled) return;
        try {
            vectorStore.delete(List.of(reviewId.toString()));
        } catch (Exception e) {
            log.warn("Failed to remove review {} from vector store: {}", reviewId, e.getMessage());
        }
    }
}
