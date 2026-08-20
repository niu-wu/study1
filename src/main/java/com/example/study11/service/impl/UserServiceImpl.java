package com.example.study11.service.impl;

import com.example.study11.dao.UserDao;
import com.example.study11.entity.dto.UserSaveDTO;
import com.example.study11.entity.dto.UserUpdateDTO;
import com.example.study11.entity.po.UserPo;
import com.example.study11.entity.vo.UserVO;
import com.example.study11.exception.ApiException;
import com.example.study11.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户业务实现
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");

    @Resource
    private UserDao userDao;

    /**
     * 新建用户统一写入 BCrypt 摘要；迁移期没有编码器时保留兼容回退。
     */
    @Autowired(required = false)
    private PasswordEncoder passwordEncoder;

    @Override
    public List<UserVO> findList() {
        return userDao.findList().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public Integer dealSave(UserSaveDTO userSaveDTO) {
        // 唯一性校验:用户名已存在则拒绝
        // 逻辑删除的用户也视为已存在,所以这里不需要额外判断 is_deleted
        UserPo existUser = userDao.selectByUserName(userSaveDTO.getUsername());
        if (existUser != null) {
            throw new ApiException(HttpStatus.CONFLICT, "用户名已存在");
        }
        UserPo userPo = new UserPo();
        userPo.setUsername(userSaveDTO.getUsername());
        // 密码不能明文写入数据库，统一交由 BCrypt 生成不可逆摘要。
        userPo.setPassword(encodePassword(userSaveDTO.getPassword()));
        userPo.setCreatedAt(new Date());
        userPo.setUpdatetime(new Date());
        userPo.setEmail(userSaveDTO.getEmail());
        userPo.setPhone(userSaveDTO.getPhone());
        userPo.setBirthday(parseDate(userSaveDTO.getBirthday()));
        userPo.setStatus(1);
        userPo.setIsDeleted(0);
        // 将po插入数据库
        // 插入后会回填主键到 userPo.id
        userDao.insert(userPo);
        log.info("用户创建成功: id={}", userPo.getId());
        return userPo.getId();
    }

    @Override
    public UserPo getByUserName(String username) {
        return userDao.selectByUserName(username);
    }

    @Override
    public UserVO getUserDetailsById(Integer id) {
        UserPo userPo = getExistingUser(id);
        return toVO(userPo);
    }

    @Override
    public void updateUserById(UserUpdateDTO userUpdateDTO) {
        if (userUpdateDTO.getId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "用户ID不能为空");
        }
        // 存在性校验
        getExistingUser(userUpdateDTO.getId());
        // 用户名唯一性校验:若改成了别人的用户名则拒绝
        UserPo sameName = userDao.selectByUserName(userUpdateDTO.getUsername());
        if (sameName != null && !sameName.getId().equals(userUpdateDTO.getId())) {
            throw new ApiException(HttpStatus.CONFLICT, "用户名已存在");
        }
        UserPo userPo = new UserPo();
        userPo.setId(userUpdateDTO.getId());
        userPo.setUsername(userUpdateDTO.getUsername());
        userPo.setStatus(userUpdateDTO.getStatus());
        userPo.setUpdatetime(new Date());
        userPo.setEmail(userUpdateDTO.getEmail());
        userPo.setPhone(userUpdateDTO.getPhone());
        userPo.setBirthday(parseDate(userUpdateDTO.getBirthday()));
        userDao.updateUserById(userPo);
        log.info("用户更新成功: id={}", userUpdateDTO.getId());
    }

    @Override
    public void deleteUserById(Integer id) {
        UserPo userPo = getExistingUser(id);
        // 重复删除保护
        // 逻辑删除的用户也视为不存在,所以这里抛异常
        if (userPo.getIsDeleted() != null && userPo.getIsDeleted() != 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "该用户已被删除");
        }
        userDao.updateUserIsDeleted(id, 1);
        log.info("用户逻辑删除成功: id={}", id);
    }


    // 按 id 查询,不存在则抛统一"资源不存在"异常
    // 逻辑删除的用户也视为不存在
    private UserPo getExistingUser(Integer id) {
        UserPo userPo = userDao.selectUserById(id);
        if (userPo == null || (userPo.getIsDeleted() != null && userPo.getIsDeleted() != 0)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "用户不存在");
        }
        return userPo;
    }


    // PO 转 VO,剥离 password 等敏感字段
    // 也可使用 MapStruct 等工具自动生成转换代码
    private UserVO toVO(UserPo userPo) {
        UserVO vo = new UserVO();
        vo.setId(userPo.getId());
        vo.setUsername(userPo.getUsername());
        vo.setCreatedAt(userPo.getCreatedAt());
        vo.setUpdatetime(userPo.getUpdatetime());
        vo.setEmail(userPo.getEmail());
        vo.setPhone(userPo.getPhone());
        vo.setBirthday(userPo.getBirthday());
        vo.setStatus(userPo.getStatus());
        vo.setIsDeleted(userPo.getIsDeleted());
        return vo;
    }

    // 字符串转日期,解析失败返回 null
    // 解析失败的情况:空字符串,格式不对,日期不存在(2023-02-30)
    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            return DATE_FMT.parse(dateStr);
        } catch (Exception e) {
            log.warn("日期解析失败: {}", dateStr);
            return null;
        }
    }

    /** 生成密码摘要，兼容没有注入编码器的旧测试或迁移工具。 */
    private String encodePassword(String rawPassword) {
        return passwordEncoder == null ? rawPassword : passwordEncoder.encode(rawPassword);
    }
}
