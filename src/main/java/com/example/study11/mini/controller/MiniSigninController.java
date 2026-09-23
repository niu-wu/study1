package com.example.study11.mini.controller;

import com.example.study11.mini.entity.dto.MiniSigninSubmitRequest;
import com.example.study11.mini.entity.vo.MiniPositionVO;
import com.example.study11.mini.service.MiniSigninService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 应聘者小程序面试签到（公开接口，无需登录）。 */
@RestController
@RequestMapping("/api/mini")
public class MiniSigninController {

    private final MiniSigninService miniSigninService;

    public MiniSigninController(MiniSigninService miniSigninService) {
        this.miniSigninService = miniSigninService;
    }

    /** 签到表单职位下拉。 */
    @GetMapping("/positions")
    public ResponseEntity<List<MiniPositionVO>> positions() {
        return ResponseEntity.ok(miniSigninService.listPositions());
    }

    /** 提交签到。 */
    @PostMapping("/signin")
    public ResponseEntity<Void> submit(@RequestBody @Valid MiniSigninSubmitRequest request) {
        miniSigninService.submit(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
