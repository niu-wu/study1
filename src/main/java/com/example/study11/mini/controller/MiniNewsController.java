package com.example.study11.mini.controller;

import com.example.study11.mini.entity.vo.MiniNewsDetailVO;
import com.example.study11.mini.entity.vo.MiniNewsListItemVO;
import com.example.study11.mini.entity.vo.MiniPageVO;
import com.example.study11.mini.service.MiniNewsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 应聘者小程序公司动态（公开接口）。 */
@RestController
@RequestMapping("/api/mini/news")
public class MiniNewsController {

    private final MiniNewsService miniNewsService;

    public MiniNewsController(MiniNewsService miniNewsService) {
        this.miniNewsService = miniNewsService;
    }

    /** 分页列表。 */
    @GetMapping
    public ResponseEntity<MiniPageVO<MiniNewsListItemVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(miniNewsService.list(page, size));
    }

    /** 详情。 */
    @GetMapping("/{id}")
    public ResponseEntity<MiniNewsDetailVO> detail(@PathVariable Long id) {
        return ResponseEntity.ok(miniNewsService.detail(id));
    }
}
