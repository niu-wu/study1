package com.example.study11.service;

import com.example.study11.entity.vo.OnboardingRecordVO;

import java.time.LocalDate;

/** 入职及现有用户体系关联业务接口。 */
public interface OnboardingService {

    OnboardingRecordVO process(String recordUuid, LocalDate onboardingDate,
                               String onboardingNote, Integer processedByUserId);

    OnboardingRecordVO findByRecordUuid(String recordUuid);
}
