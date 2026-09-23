package com.example.study11.mini.entity.vo;

import lombok.Data;

import java.time.LocalDate;

/** 公司动态列表项。 */
@Data
public class MiniNewsListItemVO {

    private Long id;
    private String title;
    private String summary;
    private LocalDate publishDate;
}
