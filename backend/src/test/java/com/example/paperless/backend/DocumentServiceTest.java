package com.example.paperless.backend;

import com.example.paperless.backend.businessLogic.DocumentRepository;
import com.example.paperless.backend.businessLogic.DocumentService;
import com.example.paperless.backend.models.Document;
import com.example.paperless.backend.models.ElasticDocument;
import com.example.paperless.backend.rabbitMQ.RabbitMQSenderService;
import com.example.paperless.backend.ElasticSearch.ElasticRepository;
import io.minio.MinioClient;
import io.minio.GetObjectArgs;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private RabbitMQSenderService rabbitMQService;

    @Mock
    private MinioClient minioClient;

    @Mock
    private ElasticRepository elasticRepository;

    @InjectMocks
    private DocumentService documentService;

    private Document document;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        document = new Document();
        document.setTitle("Test Document");
        document.setContent("Test content for the document.");
        document.setDateOfCreation(LocalDateTime.now());
    }

    @Test
    public void testGetAllDocuments() {
        List<Document> documents = List.of(document);
        when(documentRepository.findAll()).thenReturn(documents);

        List<Document> result = documentService.getAllDocuments();

        assertEquals(1, result.size());
        assertEquals("Test Document", result.get(0).getTitle());
        verify(documentRepository, times(1)).findAll();
    }

    @Test
    public void testGetDocumentById() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));

        Document result = documentService.getDocumentById(1L);

        assertNotNull(result);
        assertEquals("Test Document", result.getTitle());
        verify(documentRepository, times(1)).findById(1L);
    }

    @Test
    public void testAddDocument() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("testFile.txt");
        when(file.getBytes()).thenReturn("Test content".getBytes());
        when(documentRepository.save(any(Document.class))).thenReturn(document);

        Document result = documentService.addDocument(file);

        assertNotNull(result);
        assertEquals("Test Document", result.getTitle());
        verify(documentRepository, times(1)).save(any(Document.class));
        verify(rabbitMQService, times(1)).sendMessage(anyString());
        verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
    }

    @Test
    public void testUpdateDocument() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
        String updatedContent = "Updated content";

        documentService.updateDocument(1L, updatedContent);

        assertEquals(updatedContent, document.getContent());
        verify(documentRepository, times(1)).save(any(Document.class));
    }

    @Test
    public void testSearchDocuments() {
        ElasticDocument elasticDocument = new ElasticDocument("1", "Test Title", "Test content", "path/to/file");
        elasticDocument.setId("1");

        List<ElasticDocument> elasticDocuments = List.of(elasticDocument);
        when(elasticRepository.findByTitleContainingOrContentContaining("Test", "Test")).thenReturn(elasticDocuments);
        when(documentRepository.findDocumentsByIds(List.of(1L))).thenReturn(List.of(document));

        List<Document> result = documentService.searchDocuments("Test");

        assertEquals(1, result.size());
        assertEquals("Test Document", result.get(0).getTitle());
        verify(elasticRepository, times(1)).findByTitleContainingOrContentContaining("Test", "Test");
    }

    @Test
    public void testDeleteDocument() throws Exception {
        when(documentRepository.existsById(1L)).thenReturn(true);
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));

        documentService.deleteDocument(1L);

        verify(minioClient, times(1)).removeObject(any(RemoveObjectArgs.class));
        verify(elasticRepository, times(1)).deleteById("1");
        verify(documentRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDownloadDocument() throws Exception {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));

        // Mock the behavior of MinioClient to return an InputStream
        InputStream mockStream = new ByteArrayInputStream("Test content".getBytes());
        doAnswer(invocation -> mockStream).when(minioClient).getObject(any(GetObjectArgs.class));

        byte[] result = documentService.downloadDocument(1L);

        assertNotNull(result);
        assertEquals("Test content", new String(result));
        verify(minioClient, times(1)).getObject(any(GetObjectArgs.class));
    }
}
