package com.example.study11.mini.service.impl;

import com.example.study11.exception.ApiException;
import com.example.study11.mini.dao.MiniSigninDao;
import com.example.study11.mini.dao.MiniSigninEducationDao;
import com.example.study11.mini.dao.MiniSigninWorkDao;
import com.example.study11.mini.entity.dto.MiniSigninSubmitRequest;
import com.example.study11.mini.entity.po.MiniSigninEducationPo;
import com.example.study11.mini.entity.po.MiniSigninPo;
import com.example.study11.mini.entity.po.MiniSigninWorkPo;
import com.example.study11.mini.entity.vo.MiniPositionVO;
import com.example.study11.mini.service.MiniSigninService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** 应聘者签到实现。 */
@Service
public class MiniSigninServiceImpl implements MiniSigninService {

    private final MiniSigninDao miniSigninDao;
    private final MiniSigninEducationDao miniSigninEducationDao;
    private final MiniSigninWorkDao miniSigninWorkDao;

    public MiniSigninServiceImpl(MiniSigninDao miniSigninDao,
                                 MiniSigninEducationDao miniSigninEducationDao,
                                 MiniSigninWorkDao miniSigninWorkDao) {
        this.miniSigninDao = miniSigninDao;
        this.miniSigninEducationDao = miniSigninEducationDao;
        this.miniSigninWorkDao = miniSigninWorkDao;
    }

    @Override
    public List<MiniPositionVO> listPositions() {
        return miniSigninDao.selectOpenPositions();
    }

    @Override
    @Transactional
    public void submit(MiniSigninSubmitRequest request) {
        String signinUuid = UUID.randomUUID().toString();

        // 主表
        MiniSigninPo po = new MiniSigninPo();
        po.setSigninUuid(signinUuid);
        po.setPositionId(request.getPositionId());
        if (request.getPositionId() != null) {
            po.setPositionName(miniSigninDao.selectPositionNameById(request.getPositionId()));
        }
        po.setName(request.getName());
        po.setGender(request.getGender());
        po.setPhone(request.getPhone());
        po.setEmail(request.getEmail());
        po.setIdCard(request.getIdCard());
        po.setBirthDate(request.getBirthDate());
        po.setAddress(request.getAddress());
        po.setApplyChannel(request.getApplyChannel().getCode());
        po.setReferrer(request.getReferrer());
        po.setInterviewMode(request.getInterviewMode().getCode());
        if (miniSigninDao.insert(po) != 1) {
            throw ApiException.internalServerError("签到提交失败");
        }

        // 教育经历
        if (request.getEducations() != null && !request.getEducations().isEmpty()) {
            List<MiniSigninEducationPo> eduList = new ArrayList<>();
            int sort = 0;
            for (MiniSigninSubmitRequest.EducationItem item : request.getEducations()) {
                MiniSigninEducationPo edu = new MiniSigninEducationPo();
                edu.setSigninUuid(signinUuid);
                edu.setStartDate(item.getStartDate());
                edu.setEndDate(item.getEndDate());
                edu.setSchoolName(item.getSchoolName());
                edu.setEducation(item.getEducation());
                edu.setMajor(item.getMajor());
                edu.setSortOrder(sort++);
                eduList.add(edu);
            }
            miniSigninEducationDao.batchInsert(eduList);
        }

        // 工作经历
        if (request.getWorks() != null && !request.getWorks().isEmpty()) {
            List<MiniSigninWorkPo> workList = new ArrayList<>();
            int sort = 0;
            for (MiniSigninSubmitRequest.WorkItem item : request.getWorks()) {
                MiniSigninWorkPo work = new MiniSigninWorkPo();
                work.setSigninUuid(signinUuid);
                work.setStartDate(item.getStartDate());
                work.setEndDate(item.getEndDate());
                work.setCompanyName(item.getCompanyName());
                work.setPosition(item.getPosition());
                work.setLeaveReason(item.getLeaveReason());
                work.setSortOrder(sort++);
                workList.add(work);
            }
            miniSigninWorkDao.batchInsert(workList);
        }
    }
}
