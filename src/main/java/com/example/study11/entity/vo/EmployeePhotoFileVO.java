package com.example.study11.entity.vo;

import lombok.Data;
import org.springframework.core.io.Resource;

/** 一寸照下载内容。不暴露存储目录。 */
@Data
public class EmployeePhotoFileVO {

    private Resource resource;

    private String contentType;

    private String filename;
}
