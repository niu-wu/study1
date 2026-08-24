package com.example.study11.service;

import com.example.study11.entity.vo.CandidateResumeVO;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/** 候选人简历附件存储服务。 */
public interface ResumeStorageService {

    CandidateResumeVO storeFile(String recordUuid, MultipartFile file, Integer uploaderUserId);

    Resource loadFile(Long id);

    void deleteFile(Long id);
}
