package com.assess.backend.service;

import com.assess.backend.model.Product;
import com.assess.backend.repository.ProductRepository;

import java.util.*;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.search.SearchPath.fieldPath;
import static java.util.Arrays.asList;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.search.FieldSearchPath;
import com.mongodb.client.model.search.VectorSearchOptions;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;
import com.mongodb.client.MongoClient;
import org.springframework.web.multipart.MultipartFile;


@Service
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final TogetherAiService togetherAiService;
    private final ImageEmbeddingService imageEmbeddingService;

    private final MongoClient mongoClient;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    public ProductService(ProductRepository productRepository,
                          TogetherAiService togetherAiService, ImageEmbeddingService imageEmbeddingService, MongoClient mongoClient) {
        this.productRepository = productRepository;
        this.togetherAiService = togetherAiService;
        this.imageEmbeddingService = imageEmbeddingService;
        this.mongoClient = mongoClient;
    }

    // Create product with embedding
    public Product createProduct(Product product) {
        String text=product.getType();
        // Generate embedding from product text
        String textForEmbedding = product.getTitle() + ". " + product.getDescription() + text ;
        List<Double> embedding = togetherAiService.generateEmbeddings(textForEmbedding);
        product.setVectorData(embedding);

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            try {
                List<Double> imageEmbedding = imageEmbeddingService.generateImageEmbeddings(product.getImageUrl());
                product.setImageVectorData(imageEmbedding);
            } catch (Exception e) {
                log.warn("Failed to generate image embeddings for product: {}", product.getTitle(), e);
                // Continue without image embeddings rather than failing the whole product creation
            }
        }

        return productRepository.save(product);
    }

    // Update product with embedding
    public Product updateProduct(String id, Product productDetails) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // Update product fields
        existingProduct.setTitle(productDetails.getTitle());
        existingProduct.setDescription(productDetails.getDescription());
        existingProduct.setPrice(productDetails.getPrice());
        existingProduct.setImageUrl(productDetails.getImageUrl());
        existingProduct.setCategories(productDetails.getCategories());
//        existingProduct.setBrand(productDetails.getBrand());
//        existingProduct.setColor(productDetails.getColor());
//        existingProduct.setSize(productDetails.getSize());
//        existingProduct.setInStock(productDetails.isInStock());
//        existingProduct.setStockQuantity(productDetails.getStockQuantity());

        // Update embedding
        String textForEmbedding = existingProduct.getTitle() + ". " + existingProduct.getDescription();
        List<Double> embedding = togetherAiService.generateEmbeddings(textForEmbedding);
        existingProduct.setVectorData(embedding);

        return productRepository.save(existingProduct);
    }

//     Semantic search using vector search
//    public List<Product> semanticSearch(String query, int limit) {
//        System.out.println("Searching for: " + query);
//        // Generate embedding for the query
//        List<Double> queryEmbedding = togetherAiService.generateEmbeddings(query);
//        System.out.println("Embedding size: " + queryEmbedding.size());
//
//        // Create vector search stage
//        Document vectorSearchStage = new Document("$vectorSearch",
//                new Document("index", "vector_index")
//                        .append("path", "vectorData")
//                        .append("queryVector", queryEmbedding)
//                        .append("numCandidates", 100)
//                        .append("limit", limit));
//        System.out.println("Vector search stage: " + vectorSearchStage.toJson());
//
//        // Execute aggregation
//        List<Product> results = mongoTemplate.aggregate(
//                Aggregation.newAggregation(
//                        context -> vectorSearchStage,
//                        Aggregation.project("id", "title", "description", "price", "imageUrl",
//                                        "brand", "color", "size", "inStock", "stockQuantity", "vectorData")
//                                .and(metaVectorSearchScore("score").toString())
//                ),
//                "products",
//                Product.class
//        ).getMappedResults();
//
//        System.out.println("Results count: " + results.size());
//        if (!results.isEmpty()) {
//            System.out.println("First result title: " + results.get(0).getTitle());
//            System.out.println("First result score: " + results.get(0).getScore());
//        }
//
//        return results;
//    }

//    public List<Product> semanticSearch(String query , int limit){
//        List<Double> queryEmbedding = togetherAiService.generateEmbeddings(query);
//
//        // Create the aggregation pipeline
//        Aggregation aggregation = Aggregation.newAggregation(
//                new AggregationOperation() {
//                    @Override
//                    public Document toDocument(AggregationOperationContext context) {
//                        return new Document("$vectorSearch",
//                                new Document("index", "vector_index")
//                                        .append("path", "vectorData")
//                                        .append("queryVector", queryEmbedding)
//                                        .append("numCandidates", 100)
//                                        .append("limit", limit));
//                    }
//                },
//                // This is crucial - it explicitly maps the score from the metadata
//                Aggregation.project()
//                        .andInclude("id", "title", "description", "price", "imageUrl",
//                                "brand", "color", "size", "inStock", "stockQuantity", "vectorData")
//                        .and(context -> new Document("$meta", "searchScore")).as("score")
//        );
//
//        // Execute the aggregation directly with Product class
//        AggregationResults<Product> results = mongoTemplate.aggregate(
//                aggregation, "products", Product.class);
//
//        // Convert to a list
//        List<Product> productList = results.getMappedResults();
//
//        // Print results for debugging
//        for (Product p : productList) {
//            System.out.println(p.getTitle() + " - Score: " + p.getScore());
//        }
//
//        return productList;
//    }

    public List<Document> semanticSearch(String query , int limit){
        List<Double> embedding = togetherAiService.generateEmbeddings(query);
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("products");
        System.out.println(embedding);

        String indexName= "vector_index";
        FieldSearchPath fieldSearchPath = fieldPath("vectorData");
        long numCandidates = 100;

        List<Bson> pipeline = asList(

                Aggregates.vectorSearch(fieldSearchPath, embedding, indexName, limit, VectorSearchOptions.approximateVectorSearchOptions(numCandidates)),
                Aggregates.project(fields(
                        Projections.excludeId(),
                        Projections.include("title", "price", "imageUrl", "color", "size", "inStock", "stockQuantity"),
                        Projections.computed("score", new Document("$meta", "vectorSearchScore"))
                ))
        );

        List<Document> results = new ArrayList<>();
        collection.aggregate(pipeline).into(results);
        return results;

    }


    public List<Document> hybridSearch(String query, int limit) {
        // Generate embeddings for vector search
        List<Double> embedding = togetherAiService.generateEmbeddings(query);
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("products");

        // First run the vector search
        List<Bson> vectorPipeline = Arrays.asList(
                new Document("$vectorSearch",
                        new Document("index", "vector_index")  // Use your existing vector index
                                .append("path", "vectorData")
                                .append("queryVector", embedding)  // This uses the syntax for $vectorSearch
                                .append("numCandidates", 100)
                                .append("limit", limit * 2)
                ),
                Aggregates.project(fields(
                        Projections.include("_id", "title", "price", "imageUrl", "color", "size", "inStock", "stockQuantity"),
                        Projections.computed("vectorScore", new Document("$meta", "vectorSearchScore"))
                ))
        );

        // Then run text search with regular text index
        // Make sure you have created a text index first:
        // db.products.createIndex({ title: "text", description: "text", categories: "text" })

        // Get vector search results
        List<Document> vectorResults = new ArrayList<>();
        collection.aggregate(vectorPipeline).into(vectorResults);

        // Get IDs from vector results
        Set<Object> ids = vectorResults.stream()
                .map(doc -> doc.get("_id"))
                .collect(Collectors.toSet());

        // Run text search on those IDs
        List<Document> finalResults = new ArrayList<>();
        collection.find(
                Filters.and(
                        Filters.in("_id", new ArrayList<>(ids)),
                        Filters.text(query)
                )
        ).sort(
                Sorts.descending("score")
        ).limit(limit).into(finalResults);

        // If we don't have enough results, add more from vector search
        if (finalResults.size() < limit) {
            Set<Object> foundIds = finalResults.stream()
                    .map(doc -> doc.get("_id"))
                    .collect(Collectors.toSet());

            int needed = limit - finalResults.size();
            vectorResults.stream()
                    .filter(doc -> !foundIds.contains(doc.get("_id")))
                    .limit(needed)
                    .forEach(finalResults::add);
        }

        return finalResults;
    }

//    public List<Document> hybridSearch(String query, int limit, double vectorWeight) {
//        // Generate embeddings for vector search
//        List<Double> embedding = togetherAiService.generateEmbeddings(query);
//        MongoDatabase database = mongoClient.getDatabase(databaseName);
//        MongoCollection<Document> collection = database.getCollection("products");
//
//        String searchIndexName = "products_hybrid_search";
//
//        // Vector search pipeline
//        List<Bson> vectorPipeline = Arrays.asList(
//                new Document("$search",
//                        new Document("index", searchIndexName)
//                                .append("knnBeta",
//                                        new Document("path", "vectorData")
//                                                .append("vector", embedding)
//                                                .append("k", limit * 2))
//                ),
//                Aggregates.project(fields(
//                        Projections.excludeId(),
//                        Projections.include("title", "price", "imageUrl", "color", "size", "inStock", "stockQuantity"),
//                        Projections.computed("vectorScore", new Document("$meta", "searchScore"))
//                ))
//        );
//
//        // Text search pipeline
//        List<Bson> textPipeline = Arrays.asList(
//                new Document("$search",
//                        new Document("index", searchIndexName)
//                                .append("text",
//                                        new Document("query", query)
//                                                .append("path", Arrays.asList("title", "description", "categories")))
//                ),
//                Aggregates.project(fields(
//                        Projections.excludeId(),
//                        Projections.include("title", "price", "imageUrl", "color", "size", "inStock", "stockQuantity"),
//                        Projections.computed("textScore", new Document("$meta", "searchScore"))
//                ))
//        );
//
//        // Execute both queries
//        Map<String, Document> vectorResults = new HashMap<>();
//        Map<String, Document> textResults = new HashMap<>();
//        Map<String, Double> combinedScores = new HashMap<>();
//
//        // Collect vector search results
//        collection.aggregate(vectorPipeline).forEach(doc -> {
//            String title = doc.getString("title");
//            vectorResults.put(title, doc);
//            combinedScores.put(title, doc.getDouble("vectorScore") * vectorWeight);
//        });
//
//        // Collect text search results and combine scores
//        collection.aggregate(textPipeline).forEach(doc -> {
//            String title = doc.getString("title");
//            textResults.put(title, doc);
//            double currentScore = combinedScores.getOrDefault(title, 0.0);
//            combinedScores.put(title, currentScore + doc.getDouble("textScore") * (1.0 - vectorWeight));
//        });
//
//        // Combine results and sort by score
//        List<Map.Entry<String, Double>> sortedResults = new ArrayList<>(combinedScores.entrySet());
//        sortedResults.sort(Map.Entry.<String, Double>comparingByValue().reversed());
//
//        // Create final result list with merged data
//        List<Document> results = new ArrayList<>();
//        for (int i = 0; i < Math.min(limit, sortedResults.size()); i++) {
//            String title = sortedResults.get(i).getKey();
//            Document resultDoc;
//
//            // Use the document from either result set (prefer vector if available)
//            if (vectorResults.containsKey(title)) {
//                resultDoc = vectorResults.get(title);
//            } else {
//                resultDoc = textResults.get(title);
//            }
//
//            // Add the combined score
//            resultDoc.put("score", sortedResults.get(i).getValue());
//            results.add(resultDoc);
//        }
//
//        return results;
//    }

    public List<Document> imageSearch(MultipartFile imageFile, int limit) {
        // Generate image embeddings from uploaded file
        List<Double> imageEmbedding = imageEmbeddingService.generateImageEmbeddingsFromFile(imageFile);

        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection("products");

        String indexName = "image_vector_index";
        FieldSearchPath fieldSearchPath = fieldPath("imageVectorData");
        long numCandidates = 100;

        List<Bson> pipeline = asList(
                Aggregates.vectorSearch(fieldSearchPath, imageEmbedding, indexName, limit,
                        VectorSearchOptions.approximateVectorSearchOptions(numCandidates)),
                Aggregates.project(fields(
                        Projections.excludeId(),
                        Projections.include("title", "price", "imageUrl", "color", "size", "inStock", "stockQuantity"),
                        Projections.computed("score", new Document("$meta", "vectorSearchScore"))
                ))
        );

        List<Document> results = new ArrayList<>();
        collection.aggregate(pipeline).into(results);
        return results;
    }



    // Standard CRUD operations
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    public void deleteProduct(String id) {
        productRepository.deleteById(id);
    }

    // Additional search options (filtering by category, price, etc.)
//    public List<Product> searchByCategory(String category) {
//        return productRepository.findByCategories(category);
//    }
//
//    public List<Product> searchByBrand(String brand) {
//        return productRepository.findByBrand(brand);
//    }
}