package com.example.study11.mini.entity.po;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 公司动态。 */
@Data
public class MiniNewsPo {

    private Long id;
    private String title;
    private String summary;
    private String content;
    private LocalDate publishDate;
    private Integer createdBy;
    private LocalDateTime createdAt;
    private Integer updatedBy;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
