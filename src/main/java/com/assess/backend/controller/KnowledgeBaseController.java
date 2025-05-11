//package com.assess.backend.controller;
//
//import com.assess.backend.model.KnowledgeBase;
//import com.assess.backend.service.KnowledgeBaseService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api/knowledge")
//public class KnowledgeBaseController {
//
//    private final KnowledgeBaseService knowledgeBaseService;
//
//    @Autowired
//    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {
//        this.knowledgeBaseService = knowledgeBaseService;
//    }
//
//    @PostMapping
//    public ResponseEntity<KnowledgeBase> createKnowledgeBase(@RequestBody String textData) {
//        KnowledgeBase saved = knowledgeBaseService.processAndSaveKnowledgeBase(textData);
//        return ResponseEntity.ok(saved);
//    }
//
//    // Other endpoints for CRUD operations
//}