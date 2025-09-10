package com.assess.backend.service;


import com.assess.backend.model.Product;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ProductImportService {

    private final ProductService productService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ProductImportService(ProductService productService) {
        this.productService = productService;
    }


    public List<Product> importProductsFromJsonString(String jsonString) throws IOException {
        JsonNode root = objectMapper.readTree(jsonString);

        JsonNode items = root.path("item"); // array of products
        if (items.isMissingNode() || !items.isArray()) {
            throw new RuntimeException("No 'item' array found in JSON feed");
        }
        List<Product> products = new ArrayList<>();

        for (JsonNode item : items) {
            Product product = new Product();

            // id
            product.setId(item.path("id").path("__text").asText());

            // title
            product.setTitle(item.path("title").path("__cdata").asText().trim());

            // description
            product.setDescription(item.path("description").path("__cdata").asText().trim());

            // link
            product.setUrl(item.path("link").path("__cdata").asText().trim());

            // image link
            product.setImageUrl(item.path("image_link").path("__cdata").asText().trim());
            product.setGender("female");

            product.setBrand("Yezwe");

            // price -> convert to BigDecimal
            String priceText = item.path("price").path("__text").asText();
            if (priceText != null && !priceText.isEmpty()) {
                // Remove currency (e.g., "1299.00 INR")
                String numericPart = priceText.replaceAll("[^0-9.]", "");
                product.setPrice(new BigDecimal(numericPart));
            }

            // occasion -> categories list
            List<String> categories = new ArrayList<>();
            JsonNode occasionArray = item.path("occasion");
            if (occasionArray.isArray()) {
                for (JsonNode occ : occasionArray) {
                    String occText = occ.path("__cdata").asText().trim();
                    if (!occText.isEmpty()) {
                        categories.add(occText);
                    }
                }
            }
            product.setCategories(categories);

            Product newProduct = productService.createProduct(product);

            products.add(newProduct);
        }

        return products;
    }

    private List<String> extractCategories(String title, String description) {
        List<String> categories = new ArrayList<>();

//         Basic category extraction logic - you can enhance this
        String lowercaseTitle = title.toLowerCase();
        String lowercaseDesc = description.toLowerCase();

        if (lowercaseTitle.contains("leggings") || lowercaseDesc.contains("leggings")) {
            categories.add("leggings");
            categories.add("training and gym");
        }

        if (lowercaseTitle.contains("bra") || lowercaseDesc.contains("bra")) {
            categories.add("sports bra");
            categories.add("training and gym");
        }

        if (lowercaseTitle.contains("shorts") || lowercaseDesc.contains("shorts")) {
            categories.add("shorts");
            categories.add("training and gym");
        }

        if (lowercaseTitle.contains("jacket") || lowercaseDesc.contains("jacket")) {
            categories.add("jacket");
            categories.add("training and gym");
        }

        if (lowercaseTitle.contains("top") || lowercaseDesc.contains("top")) {
            categories.add("top");
            categories.add("training and gym");
        }

        if (lowercaseTitle.contains("sweatshirt") || lowercaseDesc.contains("sweatshirt")) {
            categories.add("sweatshirt");
            categories.add("training and gym");
        }

        if (lowercaseTitle.contains("socks") || lowercaseDesc.contains("socks")) {
            categories.add("socks");
            categories.add("training and gym");
        }

        if (lowercaseTitle.contains("briefs") || lowercaseDesc.contains("briefs")) {
            categories.add("underwear");
            categories.add("training and gym");
        }

        // Add a general category
        categories.add("sports wear");
        return categories;
    }


//    public void importProductsFromXML(String xmlSource, int limit) {
//        XMLProductParser parser = new XMLProductParser();
//        List<Product> products = parser.parseProductsFromXML(xmlSource, limit);
//
//        System.out.println("\n=== STARTING DATABASE IMPORT ===");
//        System.out.println("Source: " + (xmlSource.startsWith("http") ? "URL" : "File"));
//
//        // Save to database
//        int savedCount = 0;
//        for (Product product : products) {
//            try {
//                productService.createProduct(product);
//                savedCount++;
//                System.out.println("✅ Saved product " + savedCount + ": " + product.getTitle());
//            } catch (Exception e) {
//                System.err.println("❌ Error saving product " + product.getId() + ": " + e.getMessage());
//            }
//        }
//
//        System.out.println("\n=== IMPORT COMPLETED ===");
//        System.out.println("Total products processed: " + products.size());
//        System.out.println("Successfully saved: " + savedCount);
//        System.out.println("Failed: " + (products.size() - savedCount));
//    }

    // Test method for gradual testing with URL
//    public void testImportWithLimit(String xmlSource, int limit) {
//        System.out.println("🧪 TESTING IMPORT WITH LIMIT: " + limit);
//        System.out.println("🔗 Source: " + xmlSource);
//        importProductsFromXML(xmlSource, limit);
//    }

    // Convenience method specifically for your Shopify feed
    public void importFromShopifyFeed(int offset, int limit) {
        String shopifyFeedUrl = "https://52fb65-b0.myshopify.com/a/feed/superfeed.xml";

        System.out.println("🛒 SHOPIFY FEED IMPORT");
        System.out.println("📍 Offset: " + offset + " (starting from product " + (offset + 1) + ")");
        System.out.println("📊 Limit: " + limit + " products");
        System.out.println("🎯 Will import products " + (offset + 1) + " to " + (offset + limit));

        XMLProductParser parser = new XMLProductParser();
        List<Product> products = parser.parseProductsFromXML(shopifyFeedUrl, limit, offset);

        System.out.println("\n=== STARTING DATABASE IMPORT ===");

        // Save to database
        int savedCount = 0;
        for (Product product : products) {
            try {
                productService.createProduct(product);
                savedCount++;
                System.out.println("✅ Saved product " + savedCount + ": " + product.getTitle());
            } catch (Exception e) {
                System.err.println("❌ Error saving product " + product.getId() + ": " + e.getMessage());
            }
        }

        System.out.println("\n=== IMPORT COMPLETED ===");
        System.out.println("Total products processed: " + products.size());
        System.out.println("Successfully saved: " + savedCount);
        System.out.println("Failed: " + (products.size() - savedCount));
    }


}
