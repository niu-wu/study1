package com.example.study11.mini.service;

import com.example.study11.mini.entity.dto.MiniSigninSubmitRequest;
import com.example.study11.mini.entity.vo.MiniPositionVO;

import java.util.List;

/** 应聘者小程序面试签到。 */
public interface MiniSigninService {

    /** 签到表单职位下拉。 */
    List<MiniPositionVO> listPositions();

    /** 提交签到。 */
    void submit(MiniSigninSubmitRequest request);
}
