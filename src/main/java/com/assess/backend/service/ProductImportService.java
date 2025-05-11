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

    public ProductImportService(ProductService productService) {
        this.productService = productService;
    }


    public List<Product> importProductsFromJsonString(String jsonContent) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonContent);
        JsonNode productsNode = rootNode.get("products");

        List<Product> importedProducts = new ArrayList<>();

        if (productsNode.isArray()) {
            for (JsonNode productNode : productsNode) {
                Product product = new Product();
                product.setBrand("MissPrint Jaipur");
                product.setTitle(productNode.get("title").asText());
                product.setDescription(productNode.get("description").asText());
                product.setGender("Women");
                product.setType("Ethnic wear");


                // Extract price removing currency symbol and converting to BigDecimal
                String priceStr = productNode.get("price").asText().replace("₹ ", "").replace(",", "").trim();
                product.setPrice(new BigDecimal(priceStr));

                product.setUrl(productNode.get("url").asText());
                product.setImageUrl(productNode.get("imageUrl").asText());

                // Add category based on product title/description

                // Use your existing createProduct method to generate embeddings and save
                Product savedProduct = productService.createProduct(product);
                importedProducts.add(savedProduct);
            }
        }

        return importedProducts;
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

}
