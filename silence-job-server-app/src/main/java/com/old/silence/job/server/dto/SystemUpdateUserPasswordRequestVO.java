package com.old.silence.job.server.dto;

import org.springframework.web.bind.annotation.PutMapping;

import jakarta.validation.constraints.NotBlank;


public class SystemUpdateUserPasswordRequestVO {

    @NotBlank(message = "原密码不能为空", groups = PutMapping.class)
    private String oldPassword;

    @NotBlank(message = "新密码不能为空", groups = PutMapping.class)
    private String newPassword;

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
