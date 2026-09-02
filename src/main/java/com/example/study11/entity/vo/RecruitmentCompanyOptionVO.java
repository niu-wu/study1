package com.example.study11.entity.vo;

import lombok.Data;

/** 公司下拉选项响应。 */
@Data
public class RecruitmentCompanyOptionVO {

    private String companyUuid;
    private String companyName;
    private Integer currentHeadcount;
}
