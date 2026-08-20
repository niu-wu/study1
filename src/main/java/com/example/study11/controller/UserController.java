package com.example.study11.controller;

import com.example.study11.entity.dto.UserSaveDTO;
import com.example.study11.entity.dto.UserUpdateDTO;
import com.example.study11.entity.vo.UserVO;
import com.example.study11.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户 CRUD 接口
 */
@RestController
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 查询用户列表
     * [查]
     */
    @GetMapping("users")
    public ResponseEntity<List<UserVO>> findUserList() {
        return ResponseEntity.ok(userService.findList());
    }

    /**
     * 创建用户，返回新用户 id
     * [增]
     */
    @PostMapping("users")
    public ResponseEntity<Integer> create(@RequestBody @Validated UserSaveDTO userSaveDTO) {
        return ResponseEntity.ok(userService.dealSave(userSaveDTO));
    }

    /**
     * 查询用户详情
     * [查]
     */
    @GetMapping("users/{id}")
    public ResponseEntity<UserVO> getUserDetailsById(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(userService.getUserDetailsById(id));
    }

    /**
     * 更新用户基本信息(不含密码)
     * [改]
     */
    @PutMapping("users/{id}")
    public ResponseEntity<Void> update(@PathVariable("id") Integer id,
                                       @RequestBody @Validated UserUpdateDTO userUpdateDTO) {
        userUpdateDTO.setId(id);
        userService.updateUserById(userUpdateDTO);
        return ResponseEntity.ok().build();
    }

    /**
     * 逻辑删除用户
     * [删]
     */
    @DeleteMapping("users/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Integer id) {
        userService.deleteUserById(id);
        return ResponseEntity.ok().build();
    }
}
