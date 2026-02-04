package com.old.silence.job.server.dto;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;



public class SystemUserRequestVO {

    @NotNull(groups = {PutMapping.class})
    private Long id;

    @NotBlank(message = "用户名不能为空", groups = PostMapping.class)
    private String username;

    @NotBlank(message = "密码不能为空", groups = PostMapping.class)
    private String password;

    @NotNull(groups = {PutMapping.class, PostMapping.class})
    private Integer role;

    private List<UserPermissionRequestVO> permissions;
}
