package com.example.paperless.backend.ElasticSearch;

import com.example.paperless.backend.models.Document;
import com.example.paperless.backend.models.ElasticDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface ElasticRepository extends ElasticsearchRepository<ElasticDocument, String> {
    List<ElasticDocument> findByTitleContainingOrContentContaining(String title, String content);
}