package com.example.study11.exception;

import com.example.study11.common.model.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsUploadSizeExceededToPayloadTooLarge() {
        ResponseEntity<ErrorResponse> response = handler.handleMaxUploadSizeExceededException(
                new MaxUploadSizeExceededException(10L), new MockHttpServletRequest());

        assertEquals(413, response.getStatusCode().value());
    }

    @Test
    void mapsMalformedMultipartRequestToBadRequest() {
        ResponseEntity<ErrorResponse> response = handler.handleMultipartException(
                new MultipartException("malformed multipart"), new MockHttpServletRequest());

        assertEquals(400, response.getStatusCode().value());
    }
}
