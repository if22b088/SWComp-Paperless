package com.example.paperless.backend.businessLogic;

import com.example.paperless.backend.models.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    // Using query derivation
    List<Document> findByIdIn(List<Long> ids);

    // Alternatively, you can use a custom JPQL query
    @Query("SELECT d FROM Document d WHERE d.id IN :ids")
    List<Document> findDocumentsByIds(@Param("ids") List<Long> ids);
}
