package com.example.study11.entity.vo;

import lombok.Data;

/** 招聘状态统计响应。 */
@Data
public class RecruitmentInfoStatisticsVO {

    private Long pendingInitial;

    private Long pendingRetest;

    private Long pendingOnboarding;

    private Long notPassed;
}
