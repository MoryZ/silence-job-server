package com.old.silence.job.server.vo;



import java.time.Instant;
import java.util.List;



public class SystemUserResponseVO {

    private Long id;

    private String username;

    private Integer role;

    private List<String> groupNameList;

    private List<NamespaceResponseVO> namespaceIds;

    private List<PermissionsResponseVO> permissions;

    private String token;

    private Instant createdDate;

    private Instant updatedDate;

    private String mode;

}
