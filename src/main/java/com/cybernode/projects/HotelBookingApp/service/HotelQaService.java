package com.cybernode.projects.HotelBookingApp.service;

import com.cybernode.projects.HotelBookingApp.dto.HotelQaResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotelQaService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    @Value("${review.qa.enabled}")
    private boolean enabled;

    @Value("${review.qa.top-k}")
    private int topK;

    @Value("${review.qa.similarity-threshold}")
    private double threshold;

    private static final String SYSTEM_PROMPT = """
            You are a hotel Q&A assistant. Answer the guest's question using ONLY the
            review excerpts provided below. Do not use outside knowledge.
            If the reviews don't cover the question, say so plainly -- do not guess.
            Keep the answer to 2-3 sentences.
            """;

    public HotelQaResponseDto ask(Long hotelId, String question) {
        if (!enabled) {
            return HotelQaResponseDto.builder()
                    .answer("This feature is currently unavailable.")
                    .sourceReviewIds(List.of())
                    .build();
        }

        try {
            var filter = new FilterExpressionBuilder().eq("hotelId", hotelId).build();

            SearchRequest request = SearchRequest.builder()
                    .query(question)
                    .topK(topK)
                    .similarityThreshold(threshold)
                    .filterExpression(filter)
                    .build();

            List<Document> matches = vectorStore.similaritySearch(request);

            if (matches.isEmpty()) {
                return HotelQaResponseDto.builder()
                        .answer("Not enough review data to answer that yet.")
                        .sourceReviewIds(List.of())
                        .build();
            }

            String context = matches.stream()
                    .map(d -> "- " + d.getText())
                    .collect(Collectors.joining("\n"));

            String answer = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user("Reviews:\n" + context + "\n\nQuestion: " + question)
                    .call()
                    .content();

            List<Long> sourceIds = matches.stream()
                    .map(d -> Long.valueOf(d.getId()))
                    .toList();

            return HotelQaResponseDto.builder()
                    .answer(answer)
                    .sourceReviewIds(sourceIds)
                    .build();

        } catch (Exception e) {
            log.warn("Hotel Q&A failed for hotel {}: {}", hotelId, e.getMessage());
            return HotelQaResponseDto.builder()
                    .answer("Something went wrong answering that -- please try again.")
                    .sourceReviewIds(List.of())
                    .build();
        }
    }
}
