package com.assess.backend.controller;

import com.assess.backend.model.Product;
import com.assess.backend.service.ProductImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/products/import")
public class ProductImportController {

    private final ProductImportService productImportService;

    @Autowired
    public ProductImportController(ProductImportService productImportService) {
        this.productImportService = productImportService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> importProducts(@RequestBody String jsonContent) {
        try {

            List<Product> importedProducts = productImportService.importProductsFromJsonString(jsonContent);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", importedProducts.size());
            response.put("message", "Successfully imported " + importedProducts.size() + " products");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
