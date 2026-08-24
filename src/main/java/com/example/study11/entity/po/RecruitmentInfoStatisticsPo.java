package com.example.study11.entity.po;

import lombok.Data;

/** 招聘状态统计持久化结果。 */
@Data
public class RecruitmentInfoStatisticsPo {

    private Long pendingInitial;

    private Long pendingRetest;

    private Long pendingOnboarding;

    private Long notPassed;
}
