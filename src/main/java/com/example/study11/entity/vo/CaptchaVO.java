package com.example.study11.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图形验证码响应
 *
 * <p>图片以 data URI 返回，前端无需再拼接文件地址；验证码答案只保存在服务端 Redis，
 * 不会出现在响应中。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaVO {

    /** 验证码编号，提交修改密码时原样带回。 */
    private String captchaId;

    /** PNG 图片 data URI。 */
    private String image;

    /** 图片在服务端的有效秒数。 */
    private long expiresInSeconds;
}
