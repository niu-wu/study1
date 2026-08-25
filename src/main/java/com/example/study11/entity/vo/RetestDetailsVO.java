package com.example.study11.entity.vo;

import com.example.study11.entity.enums.RecruitmentType;
import lombok.Data;

/** 复试流程详情响应对象。 */
@Data
public class RetestDetailsVO {

    private String recordUuid;

    private String status;

    private RecruitmentType recruitmentType;

    private RetestApplicationVO application;

    private RetestReviewVO review;
}
