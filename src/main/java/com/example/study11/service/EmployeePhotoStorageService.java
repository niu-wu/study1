package com.example.study11.service;

import com.example.study11.entity.vo.EmployeePhotoFileVO;
import org.springframework.web.multipart.MultipartFile;

/** 员工一寸照存储服务。 */
public interface EmployeePhotoStorageService {

    /** 保存照片文件，返回 UUID 文件名。 */
    String store(MultipartFile file);

    /** 读取已保存的照片，不存在则 404。 */
    EmployeePhotoFileVO load(String storedFilename);

    /** 删除旧照片，失败时忽略。 */
    void deleteQuietly(String storedFilename);
}
