package com.example.study11.mini.entity.vo;

import lombok.Data;

import java.util.List;

/** 分页结果包装。 */
@Data
public class MiniPageVO<T> {

    private List<T> list;
    private long total;
    private int page;
    private int size;

    public MiniPageVO(List<T> list, long total, int page, int size) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.size = size;
    }
}
