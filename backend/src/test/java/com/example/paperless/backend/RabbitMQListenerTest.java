package com.example.paperless.backend;

import com.example.paperless.backend.businessLogic.DocumentService;
import com.example.paperless.backend.rabbitMQ.RabbitMQListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class RabbitMQListenerTest {

    @Mock
    private DocumentService documentService;

    @InjectMocks
    private RabbitMQListener rabbitMQListener;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testReceiveMessage() {
        String documentContent = "12345/This is the OCR result text";

        // Call the method under test
        rabbitMQListener.receiveMessage(documentContent);

        // Verify that updateDocument was called with the correct ID and content
        verify(documentService, times(1)).updateDocument(12345L, "This is the OCR result text");
    }

    @Test
    public void testReceiveMessageWithInvalidFormat() {
        String invalidContent = "12345"; // no '/' present

        // If the format is invalid, calling split will result in an array with only one element.
        // This might cause an IndexOutOfBoundsException or similar error.
        // Depending on how you want to handle this scenario, the test may verify that the service is never called.

        rabbitMQListener.receiveMessage(invalidContent);

        // Ensure that updateDocument was never called due to invalid message format
        verify(documentService, never()).updateDocument(anyLong(), anyString());
    }

    @Test
    public void testReceiveMessageWithNonNumericId() {
        String invalidIdContent = "abc/Some OCR result";

        // Attempting to parse a non-numeric ID will cause a NumberFormatException.
        // Depending on how the code should behave, you might want to verify that no update is attempted.
        // Currently, the code will throw an exception. Let's just verify that no update was attempted:

        try {
            rabbitMQListener.receiveMessage(invalidIdContent);
        } catch (NumberFormatException e) {
            // This is expected since 'abc' cannot be parsed as a long.
        }

        verify(documentService, never()).updateDocument(anyLong(), anyString());
    }
}

