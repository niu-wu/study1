package com.example.study11.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.util.List;

/** 员工一寸照存储配置。 */
@Data
@Component
@ConfigurationProperties(prefix = "file.photo")
public class PhotoStorageProperties {

    private String uploadDir;

    private DataSize maxFileSize;

    private List<String> allowedExtensions;
}
