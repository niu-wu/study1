package com.example.study11.entity.dto;

import com.example.study11.exception.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmployeeSystemAccountSaveRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void passwordFieldIsRejected() throws Exception {
        EmployeeSystemAccountSaveRequest request = objectMapper.readValue(
                "{\"systemName\":\"企业邮箱\",\"accountName\":\"a@b.com\",\"password\":\"secret\"}",
                EmployeeSystemAccountSaveRequest.class);

        assertEquals("企业邮箱", request.getSystemName());
        ApiException exception = assertThrows(ApiException.class, request::rejectUnexpectedFields);
        assertEquals(400, exception.getStatus().value());
        assertEquals("password 不允许传入", exception.getMessage());
    }
}
