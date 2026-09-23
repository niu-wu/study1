package com.example.study11.mini.service.impl;

import com.example.study11.exception.ApiException;
import com.example.study11.mini.dao.MiniNewsDao;
import com.example.study11.mini.entity.po.MiniNewsPo;
import com.example.study11.mini.entity.vo.MiniNewsDetailVO;
import com.example.study11.mini.entity.vo.MiniNewsListItemVO;
import com.example.study11.mini.entity.vo.MiniPageVO;
import com.example.study11.mini.service.MiniNewsService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/** 公司动态实现。 */
@Service
public class MiniNewsServiceImpl implements MiniNewsService {

    private final MiniNewsDao miniNewsDao;

    public MiniNewsServiceImpl(MiniNewsDao miniNewsDao) {
        this.miniNewsDao = miniNewsDao;
    }

    @Override
    public MiniPageVO<MiniNewsListItemVO> list(int page, int size) {
        if (page < 1) page = 1;
        if (size < 1) size = 10;
        if (size > 50) size = 50;
        int offset = (page - 1) * size;

        List<MiniNewsPo> rows = miniNewsDao.selectPage(offset, size);
        long total = miniNewsDao.selectCount();

        List<MiniNewsListItemVO> list = rows.stream().map(po -> {
            MiniNewsListItemVO vo = new MiniNewsListItemVO();
            vo.setId(po.getId());
            vo.setTitle(po.getTitle());
            vo.setSummary(po.getSummary());
            vo.setPublishDate(po.getPublishDate());
            return vo;
        }).toList();

        return new MiniPageVO<>(list, total, page, size);
    }

    @Override
    public MiniNewsDetailVO detail(Long id) {
        MiniNewsPo po = miniNewsDao.selectById(id);
        if (po == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "动态不存在");
        }
        MiniNewsDetailVO vo = new MiniNewsDetailVO();
        vo.setId(po.getId());
        vo.setTitle(po.getTitle());
        vo.setContent(po.getContent());
        vo.setPublishDate(po.getPublishDate());
        return vo;
    }
}
