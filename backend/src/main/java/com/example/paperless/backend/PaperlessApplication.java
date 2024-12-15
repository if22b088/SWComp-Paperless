package com.example.paperless.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication

// the enable notations are necessary to tellSpring to look for JPA/elasticsearch only in these two packages. otherwise there would be a conflict
@EnableJpaRepositories(basePackages = "com.example.paperless.backend.businessLogic")
@EnableElasticsearchRepositories(basePackages = "com.example.paperless.backend.ElasticSearch")
public class PaperlessApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaperlessApplication.class, args);
    }

}
