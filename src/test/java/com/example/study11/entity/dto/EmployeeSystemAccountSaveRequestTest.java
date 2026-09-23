package com.example.study11.entity.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmployeeSystemAccountSaveRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void passwordIsStoredWhenProvided() throws Exception {
        EmployeeSystemAccountSaveRequest request = objectMapper.readValue(
                "{\"systemName\":\"企业邮箱\",\"accountName\":\"a@b.com\",\"password\":\"secret\"}",
                EmployeeSystemAccountSaveRequest.class);

        request.rejectUnexpectedFields();
        assertEquals("secret", request.getPassword());
    }
}
