//package com.assess.backend.service;
//
//import com.assess.backend.model.KnowledgeBase;
//import com.assess.backend.repository.KnowledgeBaseRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//@Service
//public class KnowledgeBaseService {
//
//    private final KnowledgeBaseRepository knowledgeBaseRepository;
//    private final TogetherAiService togetherAiService;
//
//    @Autowired
//    public KnowledgeBaseService(KnowledgeBaseRepository knowledgeBaseRepository, TogetherAiService togetherAiService) {
//        this.knowledgeBaseRepository = knowledgeBaseRepository;
//        this.togetherAiService = togetherAiService;
//    }
//
//    /**
//     * Process text data, generate embeddings using TogetherAI, and save to MongoDB
//     *
//     * @param textData The text to be embedded and stored
//     * @return The saved KnowledgeBase entity
//     */
//    public KnowledgeBase processAndSaveKnowledgeBase(String textData) {
//        // Generate embeddings using TogetherAI service
//        String[] embeddingResponse = togetherAiService.generateEmbeddings(textData);
//
//        // Parse the embedding response to extract vector data
//        List<Double> vectorData = parseEmbeddingResponse(embeddingResponse[0]);
//
//        // Create and save the KnowledgeBase entity
//        KnowledgeBase knowledgeBase = new KnowledgeBase();
//        knowledgeBase.setTextData(textData);
//        knowledgeBase.setVectorData(vectorData);
//
//        return knowledgeBaseRepository.save(knowledgeBase);
//    }
//
//    /**
//     * Parse the embedding response string to extract vector values
//     * Handles format like: {object=list, data=[{object=embedding, embedding=[0.87,0.98,...], index=0}], model=...}
//     *
//     * @param responseStr The string response from Together AI
//     * @return List of embedding vector values
//     */
//    private List<Double> parseEmbeddingResponse(String responseStr) {
//        List<Double> vectorValues = new ArrayList<>();
//
//        try {
//            // Extract the embedding array using regex
//            Pattern pattern = Pattern.compile("embedding=\\[(.*?)\\]");
//            Matcher matcher = pattern.matcher(responseStr);
//
//            if (matcher.find()) {
//                String embeddingsStr = matcher.group(1);
//                String[] embeddingValues = embeddingsStr.split(", ");
//
//                for (String value : embeddingValues) {
//                    try {
//                        vectorValues.add(Double.parseDouble(value));
//                    } catch (NumberFormatException e) {
//                        // Skip values that can't be parsed as double
//                        System.err.println("Could not parse value: " + value);
//                    }
//                }
//            }
//
//            if (vectorValues.isEmpty()) {
//                throw new RuntimeException("Failed to extract embedding values from response: " + responseStr);
//            }
//
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to parse embedding response: " + e.getMessage(), e);
//        }
//
//        return vectorValues;
//    }
//
//    /**
//     * Find all knowledge base entries
//     *
//     * @return List of all KnowledgeBase entities
//     */
//    public List<KnowledgeBase> findAll() {
//        return knowledgeBaseRepository.findAll();
//    }
//
//    /**
//     * Find a knowledge base entry by ID
//     *
//     * @param id The ID of the knowledge base entry
//     * @return The found KnowledgeBase entity or null
//     */
//    public KnowledgeBase findById(String id) {
//        return knowledgeBaseRepository.findById(id).orElse(null);
//    }
//
//    /**
//     * Delete a knowledge base entry by ID
//     *
//     * @param id The ID of the knowledge base entry to delete
//     */
//    public void deleteById(String id) {
//        knowledgeBaseRepository.deleteById(id);
//    }
//}