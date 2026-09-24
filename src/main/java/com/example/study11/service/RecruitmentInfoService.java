package com.example.study11.service;

import com.example.study11.common.model.PageResult;
import com.example.study11.entity.dto.RecruitmentInfoCreateDTO;
import com.example.study11.entity.dto.RecruitmentInfoPageRequest;
import com.example.study11.entity.dto.RecruitmentInfoUpdateDTO;
import com.example.study11.entity.vo.RecruitmentInfoVO;
import com.example.study11.entity.vo.RecruitmentInfoStatisticsVO;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/** 招聘信息业务接口。 */
public interface RecruitmentInfoService {

    RecruitmentInfoVO create(RecruitmentInfoCreateDTO request);

    List<RecruitmentInfoVO> findList(String applicantName, String position, String status);

    PageResult<RecruitmentInfoVO> findPage(RecruitmentInfoPageRequest request);

    RecruitmentInfoStatisticsVO findStatistics();

    RecruitmentInfoVO findByRecordUuid(String recordUuid);

    RecruitmentInfoVO findById(Long id);

    RecruitmentInfoVO update(String recordUuid, RecruitmentInfoUpdateDTO request);

    void delete(String recordUuid);

    void export(String applicantName, String position, String status, HttpServletResponse response) throws IOException;
}
