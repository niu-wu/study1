package com.example.study11.common.model;

import lombok.Getter;

import java.util.List;

/** 通用分页结果。 */
@Getter
public class PageResult<T> {

    private final int page;

    private final int pageSize;

    private final long total;

    private final int totalPages;

    private final List<T> records;

    public PageResult(int page, int pageSize, long total, List<T> records) {
        this.page = page;
        this.pageSize = pageSize;
        this.total = total;
        this.totalPages = total == 0 ? 0 : (int) ((total + pageSize - 1) / pageSize);
        this.records = records == null ? List.of() : List.copyOf(records);
    }
}
