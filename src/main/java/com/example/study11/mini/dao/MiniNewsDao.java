package com.example.study11.mini.dao;

import com.example.study11.mini.entity.po.MiniNewsPo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 公司动态数据访问层。 */
public interface MiniNewsDao {

    int insert(MiniNewsPo po);

    List<MiniNewsPo> selectPage(@Param("offset") int offset, @Param("size") int size);

    long selectCount();

    MiniNewsPo selectById(@Param("id") Long id);

    int updateById(MiniNewsPo po);

    int deleteById(@Param("id") Long id);
}
