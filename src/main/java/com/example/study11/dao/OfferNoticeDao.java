package com.example.study11.dao;

import com.example.study11.entity.po.OfferNoticePo;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/** 录用通知数据访问层。 */
public interface OfferNoticeDao {

    int insert(OfferNoticePo offerNoticePo);

    OfferNoticePo selectByRecordUuid(@Param("recordUuid") String recordUuid);

    OfferNoticePo selectByRecordUuidForUpdate(@Param("recordUuid") String recordUuid);

    int updateToSent(@Param("recordUuid") String recordUuid,
                     @Param("sentByUserId") Integer sentByUserId,
                     @Param("sentAt") LocalDateTime sentAt);
}
