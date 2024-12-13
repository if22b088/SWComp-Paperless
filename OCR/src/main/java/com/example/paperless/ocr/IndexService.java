package com.example.paperless.ocr;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class IndexService {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    public String indexDocument(String index, String documentId, Map<String, Object> document) throws IOException {
        IndexRequest<Map<String, Object>> indexRequest = IndexRequest.of(i -> i
                .index(index)
                .id(documentId)
                .document(document)
        );

        IndexResponse indexResponse = elasticsearchClient.index(indexRequest);
        return indexResponse.id();
    }
}
