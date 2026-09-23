package com.example.study11.mini.entity.vo;

import lombok.Data;

import java.time.LocalDate;

/** 公司动态详情。 */
@Data
public class MiniNewsDetailVO {

    private Long id;
    private String title;
    private String content;
    private LocalDate publishDate;
}
