package com.example.study11.mini.dao;

import com.example.study11.mini.entity.po.MiniCompanyProfilePo;
import org.apache.ibatis.annotations.Param;

/** 公司简介数据访问层。 */
public interface MiniCompanyProfileDao {

    int insert(MiniCompanyProfilePo po);

    MiniCompanyProfilePo selectLatest();

    MiniCompanyProfilePo selectById(@Param("id") Long id);

    int updateById(MiniCompanyProfilePo po);

    int deleteById(@Param("id") Long id);
}
