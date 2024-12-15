package com.example.paperless.ocr;

import com.example.paperless.ocr.model.ElasticDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ElasticRepository extends ElasticsearchRepository<ElasticDocument, String> {
}
