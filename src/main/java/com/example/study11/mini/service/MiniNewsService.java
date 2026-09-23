package com.example.study11.mini.service;

import com.example.study11.mini.entity.vo.MiniNewsDetailVO;
import com.example.study11.mini.entity.vo.MiniNewsListItemVO;
import com.example.study11.mini.entity.vo.MiniPageVO;

/** 公司动态。 */
public interface MiniNewsService {

    /** 分页列表。 */
    MiniPageVO<MiniNewsListItemVO> list(int page, int size);

    /** 详情。 */
    MiniNewsDetailVO detail(Long id);
}
