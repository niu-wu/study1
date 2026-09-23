package com.example.study11.entity.dto;

import com.example.study11.entity.enums.AttachmentType;
import com.example.study11.exception.ApiException;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/** 登记一条合同/证件附件。只存文件名和地址，不接收文件流。地址可空。 */
@Getter
@Setter
public class EmployeeContractAttachmentSaveRequest {

    @NotNull(message = "附件类型不能为空")
    private AttachmentType attachmentType;

    @NotBlank(message = "文件名不能为空")
    @Size(max = 255, message = "文件名过长")
    private String fileName;

    @Size(max = 500, message = "文件地址过长")
    private String fileUrl;

    @Size(max = 50, message = "文件类型过长")
    private String fileType;

    private Long fileSize;

    private Integer sortOrder;

    @JsonIgnore
    private final Map<String, Object> unexpectedFields = new LinkedHashMap<>();

    @JsonAnySetter
    public void captureUnexpectedField(String name, Object value) {
        unexpectedFields.put(name, value);
    }

    public void rejectUnexpectedFields() {
        if (unexpectedFields.isEmpty()) {
            return;
        }
        String field = unexpectedFields.keySet().iterator().next();
        throw ApiException.badRequest(field + " 不允许传入");
    }
}
