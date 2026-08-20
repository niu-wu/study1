package com.example.study11.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 修改密码请求参数
 *
 * <p>{@code confirmed} 是前端二次确认弹窗的最终结果，后端仍会再次校验，
 * 防止绕过页面直接调用接口。</p>
 */
@Data
public class ChangePasswordDTO {

    /** 当前密码。 */
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    /** 新密码。 */
    @NotBlank(message = "新密码不能为空")
    private String newPassword;

    /** 重复输入的新密码。 */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    /** 第 4 次错误起使用的图形验证码编号。 */
    private String captchaId;

    /** 第 4 次错误起使用的图形验证码答案。 */
    private String captchaCode;

    /** 是否通过最终确认弹窗。 */
    @NotNull(message = "请确认是否修改密码")
    private Boolean confirmed;
}
