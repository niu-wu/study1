package com.example.study11.entity.po;

import lombok.Data;

/** 员工档案全局统计查询结果。 */
@Data
public class EmployeeArchiveStatisticsPo {

    private Long probationCount;

    private Long activeCount;

    private Long headquartersCount;

    private Long dispatchedCount;

    private Long resignedCount;
}
