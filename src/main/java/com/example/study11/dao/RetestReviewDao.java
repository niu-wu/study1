package com.example.study11.dao;

import com.example.study11.entity.po.RetestReviewPo;
import org.apache.ibatis.annotations.Param;

/** 复试安排审核数据访问层。 */
public interface RetestReviewDao {

    int insert(RetestReviewPo reviewPo);

    RetestReviewPo selectByRecordUuid(@Param("recordUuid") String recordUuid);
}
