package com.example.study11.mini.dao;

import com.example.study11.mini.entity.po.MiniSigninWorkPo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 签到工作经历数据访问层。 */
public interface MiniSigninWorkDao {

    int batchInsert(@Param("list") List<MiniSigninWorkPo> list);

    List<MiniSigninWorkPo> selectBySigninUuid(@Param("signinUuid") String signinUuid);

    int deleteBySigninUuid(@Param("signinUuid") String signinUuid);
}
