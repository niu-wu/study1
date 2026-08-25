package com.example.study11.dao;

import com.example.study11.entity.po.RetestApplicationPo;
import org.apache.ibatis.annotations.Param;

/** 复试申请数据访问层。 */
public interface RetestApplicationDao {

    int insert(RetestApplicationPo applicationPo);

    RetestApplicationPo selectByRecordUuid(@Param("recordUuid") String recordUuid);
}
