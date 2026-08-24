package com.example.study11.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.util.List;

/**
 * 文件存储配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "file.storage")
public class FileStorageProperties {

    /** 上传根目录。 */
    private String uploadDir;

    /** 单个文件最大大小。 */
    private DataSize maxFileSize;

    /** 允许上传的扩展名。 */
    private List<String> allowedExtensions;
}
