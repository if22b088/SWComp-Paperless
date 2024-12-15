package com.example.paperless.ocr;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.example.paperless.ocr.model.ElasticDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class IndexService {

    @Autowired
    private ElasticRepository elasticRepository;

    public void indexDocument(ElasticDocument document) {
        elasticRepository.save(document);
        System.out.println("DOCUMENT INDEXED!" + document.getContent());
    }
}
