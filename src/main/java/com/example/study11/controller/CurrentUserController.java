package com.example.study11.controller;

import com.example.study11.entity.dto.CurrentUserUpdateDTO;
import com.example.study11.entity.vo.UserVO;
import com.example.study11.exception.ApiException;
import com.example.study11.filter.TokenInterceptor;
import com.example.study11.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** 当前登录用户资料接口。 */
@RestController
public class CurrentUserController {

    @Resource
    private UserService userService;

    public CurrentUserController() {
    }

    public CurrentUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("users/me")
    public ResponseEntity<UserVO> getCurrentUser(HttpServletRequest request) {
        return ResponseEntity.ok(userService.getUserDetailsById(currentUserId(request)));
    }

    @PutMapping("users/me")
    public ResponseEntity<UserVO> updateCurrentUser(@RequestBody @Validated CurrentUserUpdateDTO requestBody,
                                                     HttpServletRequest request) {
        return ResponseEntity.ok(userService.updateCurrentUser(currentUserId(request), requestBody));
    }

    private Integer currentUserId(HttpServletRequest request) {
        Object value = request.getAttribute(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE);
        if (value instanceof Integer userId && userId > 0) {
            return userId;
        }
        throw ApiException.unauthorized("未登录");
    }
}
