package com.example.study11.mini.service.impl;

import com.example.study11.mini.dao.MiniCompanyProfileDao;
import com.example.study11.mini.entity.po.MiniCompanyProfilePo;
import com.example.study11.mini.service.MiniCompanyProfileService;
import org.springframework.stereotype.Service;

/** 公司简介实现。 */
@Service
public class MiniCompanyProfileServiceImpl implements MiniCompanyProfileService {

    private final MiniCompanyProfileDao miniCompanyProfileDao;

    public MiniCompanyProfileServiceImpl(MiniCompanyProfileDao miniCompanyProfileDao) {
        this.miniCompanyProfileDao = miniCompanyProfileDao;
    }

    @Override
    public MiniCompanyProfilePo getProfile() {
        return miniCompanyProfileDao.selectLatest();
    }
}
