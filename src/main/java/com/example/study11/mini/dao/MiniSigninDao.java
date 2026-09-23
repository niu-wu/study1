package com.example.study11.mini.dao;

import com.example.study11.mini.entity.po.MiniSigninPo;
import com.example.study11.mini.entity.vo.MiniPositionVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 应聘者签到数据访问层。 */
public interface MiniSigninDao {

    int insert(MiniSigninPo recordPo);

    MiniSigninPo selectByUuid(@Param("signinUuid") String signinUuid);

    List<MiniSigninPo> selectList(@Param("phone") String phone,
                                   @Param("name") String name);

    List<MiniPositionVO> selectOpenPositions();

    String selectPositionNameById(@Param("positionId") Long positionId);

    int updateByUuid(MiniSigninPo recordPo);

    int deleteByUuid(@Param("signinUuid") String signinUuid);
}
