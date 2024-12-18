package com.example.paperless.ocr;

import com.example.paperless.ocr.model.ElasticDocument;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log
public class IndexService {

    private final ElasticRepository elasticRepository;

    @Autowired
    public IndexService(ElasticRepository elasticRepository) {
        this.elasticRepository = elasticRepository;
    }

    public void indexDocument(ElasticDocument document) {
        try {
            log.info("Indexing document with ID: " + document.getId());
            elasticRepository.save(document);
            log.info("Document indexed successfully with ID: " + document.getId());
        } catch (Exception e) {
            log.severe("Error indexing document with ID: " + document.getId() + ". " + e.getMessage());
            throw new RuntimeException("Failed to index document", e);
        }
    }
}
