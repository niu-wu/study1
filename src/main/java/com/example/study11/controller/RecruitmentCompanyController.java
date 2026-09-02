package com.example.study11.controller;

import com.example.study11.entity.vo.RecruitmentCompanyOptionVO;
import com.example.study11.exception.ApiException;
import com.example.study11.filter.TokenInterceptor;
import com.example.study11.service.RecruitmentCompanyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 招聘公司查询接口。 */
@RestController
@RequestMapping("/api/recruitment-companies")
public class RecruitmentCompanyController {

    private final RecruitmentCompanyService recruitmentCompanyService;

    public RecruitmentCompanyController(RecruitmentCompanyService recruitmentCompanyService) {
        this.recruitmentCompanyService = recruitmentCompanyService;
    }

    @GetMapping("/options")
    public ResponseEntity<List<RecruitmentCompanyOptionVO>> options(HttpServletRequest request) {
        return ResponseEntity.ok(recruitmentCompanyService.findActiveOptions(currentUserId(request)));
    }

    private Integer currentUserId(HttpServletRequest request) {
        Object value = request.getAttribute(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE);
        if (value instanceof Integer userId && userId > 0) {
            return userId;
        }
        throw ApiException.unauthorized("未登录");
    }
}
