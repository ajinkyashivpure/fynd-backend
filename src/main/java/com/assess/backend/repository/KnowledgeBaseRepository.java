package com.assess.backend.repository;

import com.assess.backend.model.KnowledgeBase;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KnowledgeBaseRepository extends MongoRepository<KnowledgeBase, String> {
}
