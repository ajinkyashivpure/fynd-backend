package com.assess.backend.controller;

import com.assess.backend.model.Product;
import com.assess.backend.service.ProductService;
import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return new ResponseEntity<>(productService.createProduct(product), HttpStatus.CREATED);
    }

    @GetMapping("/search")
    public List<Document> semanticSearch(
            @RequestParam String query,
            @RequestParam(defaultValue = "20") int limit) {
        return productService.semanticSearch(query, limit);
    }

    @GetMapping("/hybrid-search")
    public List<Document> hybridSearch(
            @RequestParam String query,
            @RequestParam(defaultValue = "20") int limit) {
        return productService.hybridSearch(query, limit);
    }

    @GetMapping("/image")
    public ResponseEntity<List<Document>> imageSearch(
            @RequestParam("image") MultipartFile imageFile,
            @RequestParam(defaultValue = "10") int limit) {
        if (imageFile.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<Document> results = productService.imageSearch(imageFile, limit);
        return ResponseEntity.ok(results);
    }




    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable String id, @RequestBody Product product) {
        return ResponseEntity.ok(productService.updateProduct(id, product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }



//    @GetMapping("/category/{category}")
//    public ResponseEntity<List<Product>> getByCategory(@PathVariable String category) {
//        return ResponseEntity.ok(productService.searchByCategory(category));
//    }
//
//    @GetMapping("/brand/{brand}")
//    public ResponseEntity<List<Product>> getByBrand(@PathVariable String brand) {
//        return ResponseEntity.ok(productService.searchByBrand(brand));
//    }
}