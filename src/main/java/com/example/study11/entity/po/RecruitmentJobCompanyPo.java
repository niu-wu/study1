package com.example.study11.entity.po;

import lombok.Data;

/** 岗位公司配额持久化对象。 */
@Data
public class RecruitmentJobCompanyPo {

    private String allocationUuid;
    private String jobUuid;
    private String companyUuid;
    private Integer requiredHeadcount;
    private String status;
}
