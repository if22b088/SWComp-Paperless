package com.example.paperless.backend;

import com.example.paperless.backend.Controller.DocumentResourceController;
import com.example.paperless.backend.businessLogic.DocumentService;
import com.example.paperless.backend.models.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentResourceController.class)
public class DocumentResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    private Document document;

    @BeforeEach
    public void setUp() {
        document = new Document();
        document.setId(1L);
        document.setTitle("Test Document");
        document.setContent("Test Content");
        document.setDateOfCreation(LocalDateTime.now());
    }

    @Test
    public void testGetDocuments() throws Exception {
        when(documentService.getAllDocuments()).thenReturn(List.of(document));

        mockMvc.perform(get("/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Test Document")));

        verify(documentService, times(1)).getAllDocuments();
    }

    @Test
    public void testSearchDocuments() throws Exception {
        when(documentService.searchDocuments("query")).thenReturn(List.of(document));

        mockMvc.perform(get("/documents/search")
                        .param("query", "query"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title", is("Test Document")));

        verify(documentService, times(1)).searchDocuments("query");
    }

    @Test
    public void testAddDocument() throws Exception {
        MockMultipartFile file = new MockMultipartFile("document", "testFile.txt", "text/plain", "Test content".getBytes());
        when(documentService.addDocument(ArgumentMatchers.any())).thenReturn(document);

        mockMvc.perform(multipart("/documents")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Document")));

        verify(documentService, times(1)).addDocument(ArgumentMatchers.any());
    }

    @Test
    public void testAddDocumentFailure() throws Exception {
        MockMultipartFile file = new MockMultipartFile("document", "testFile.txt", "text/plain", new byte[0]);
        when(documentService.addDocument(ArgumentMatchers.any())).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(multipart("/documents")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isInternalServerError());

        verify(documentService, times(1)).addDocument(ArgumentMatchers.any());
    }

    @Test
    public void testDeleteDocument() throws Exception {
        doNothing().when(documentService).deleteDocument(1L);

        mockMvc.perform(delete("/documents/1"))
                .andExpect(status().isNoContent());

        verify(documentService, times(1)).deleteDocument(1L);
    }

    @Test
    public void testDeleteDocumentNotFound() throws Exception {
        doThrow(new IllegalArgumentException("Document not found")).when(documentService).deleteDocument(1L);

        mockMvc.perform(delete("/documents/1"))
                .andExpect(status().isNotFound());

        verify(documentService, times(1)).deleteDocument(1L);
    }

    @Test
    public void testDownloadDocument() throws Exception {
        byte[] content = "Downloaded content".getBytes();
        when(documentService.getDocumentById(1L)).thenReturn(document);
        when(documentService.downloadDocument(1L)).thenReturn(content);

        mockMvc.perform(get("/documents/download/1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"Test Document\""))
                .andExpect(content().string("Downloaded content"));

        verify(documentService, times(1)).downloadDocument(1L);
        verify(documentService, times(1)).getDocumentById(1L);
    }

    @Test
    public void testDownloadDocumentFailure() throws Exception {
        when(documentService.downloadDocument(1L)).thenThrow(new RuntimeException("Error downloading"));

        mockMvc.perform(get("/documents/download/1"))
                .andExpect(status().isInternalServerError());

        verify(documentService, times(1)).downloadDocument(1L);
    }

}

