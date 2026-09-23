package com.example.study11.mini.controller;

import com.example.study11.mini.entity.po.MiniCompanyProfilePo;
import com.example.study11.mini.service.MiniCompanyProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 应聘者小程序公司简介（公开接口）。 */
@RestController
@RequestMapping("/api/mini")
public class MiniCompanyProfileController {

    private final MiniCompanyProfileService miniCompanyProfileService;

    public MiniCompanyProfileController(MiniCompanyProfileService miniCompanyProfileService) {
        this.miniCompanyProfileService = miniCompanyProfileService;
    }

    /** 公司简介。 */
    @GetMapping("/company-profile")
    public ResponseEntity<MiniCompanyProfilePo> profile() {
        return ResponseEntity.ok(miniCompanyProfileService.getProfile());
    }
}
