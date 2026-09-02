package com.example.study11.entity.po;

import lombok.Data;

/** 招聘公司持久化对象。 */
@Data
public class RecruitmentCompanyPo {

    private String companyUuid;
    private Long id;
    private String companyName;
    private Integer currentHeadcount;
    private String status;
    private Integer isDeleted;
}
