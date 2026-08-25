package com.example.study11.service;

import com.example.study11.entity.vo.RetestDetailsVO;

import java.time.LocalDateTime;

/** 复试申请与安排确认业务接口。 */
public interface RetestService {

    RetestDetailsVO apply(String recordUuid, String applicantRemark, Integer applicantUserId);

    RetestDetailsVO confirm(String recordUuid, String retestCompany, String retestContactPerson,
                            LocalDateTime retestTime, Integer reviewerUserId);

    RetestDetailsVO findByRecordUuid(String recordUuid);
}
