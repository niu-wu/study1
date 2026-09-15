package com.example.study11.entity.dto;

import com.example.study11.exception.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmployeeSalarySaveRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void forbiddenComputedFieldsAreRejected() throws Exception {
        EmployeeSalarySaveRequest request = objectMapper.readValue(
                "{\"baseSalary\":5000,\"grossPay\":5000}",
                EmployeeSalarySaveRequest.class);

        assertEquals(new BigDecimal("5000"), request.getBaseSalary());
        ApiException exception = assertThrows(ApiException.class, request::rejectUnexpectedFields);
        assertEquals(400, exception.getStatus().value());
        assertEquals("grossPay 不允许传入", exception.getMessage());
    }

    @Test
    void forbiddenIdentityFieldsAreRejected() throws Exception {
        EmployeeSalarySaveRequest request = objectMapper.readValue(
                "{\"salaryUuid\":\"x\",\"createdBy\":7,\"netPay\":1}",
                EmployeeSalarySaveRequest.class);

        ApiException exception = assertThrows(ApiException.class, request::rejectUnexpectedFields);
        assertEquals(400, exception.getStatus().value());
        assertEquals("salaryUuid 不允许传入", exception.getMessage());
    }
}
