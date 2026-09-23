package com.example.study11.mini.dao;

import com.example.study11.mini.entity.po.MiniSigninEducationPo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 签到教育经历数据访问层。 */
public interface MiniSigninEducationDao {

    int batchInsert(@Param("list") List<MiniSigninEducationPo> list);

    List<MiniSigninEducationPo> selectBySigninUuid(@Param("signinUuid") String signinUuid);

    int deleteBySigninUuid(@Param("signinUuid") String signinUuid);
}
