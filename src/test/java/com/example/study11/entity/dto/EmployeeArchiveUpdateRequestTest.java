package com.example.study11.entity.dto;

import com.example.study11.exception.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmployeeArchiveUpdateRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void forbiddenFieldsInJsonAreRejected() throws Exception {
        EmployeeArchiveUpdateRequest request = objectMapper.readValue(
                "{\"department\":\"研发部\",\"employmentStatus\":\"REGULAR\",\"password\":\"secret\"}",
                EmployeeArchiveUpdateRequest.class);

        assertEquals("研发部", request.getDepartment());
        ApiException exception = assertThrows(ApiException.class, request::rejectUnexpectedFields);
        assertEquals(400, exception.getStatus().value());
        assertEquals("employmentStatus 不可修改", exception.getMessage());
    }

    @Test
    void allowedFieldsDeserialize() throws Exception {
        EmployeeArchiveUpdateRequest request = objectMapper.readValue(
                "{\"companyEmail\":\"hr@example.com\",\"contractSalary\":9000}",
                EmployeeArchiveUpdateRequest.class);

        assertEquals("hr@example.com", request.getCompanyEmail());
        assertEquals(new BigDecimal("9000"), request.getContractSalary());
        request.rejectUnexpectedFields();
    }
}
