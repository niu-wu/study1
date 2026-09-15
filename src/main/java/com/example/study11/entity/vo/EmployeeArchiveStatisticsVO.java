package com.example.study11.entity.vo;

import lombok.Data;

/** 员工档案全局统计卡。不随列表筛选变化。 */
@Data
public class EmployeeArchiveStatisticsVO {

    private Long probationCount;

    private Long activeCount;

    private Long headquartersCount;

    private Long dispatchedCount;

    private Long resignedCount;
}
