package com.old.silence.job.server.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.server.domain.model.SystemUser;
import com.old.silence.job.server.infrastructure.persistence.dao.SystemUserDao;
import com.old.silence.job.server.vo.CommonOptions;

import java.util.List;

/**
 * @author MurrayZhang
 */
@RestController
@RequestMapping("/api/v1")
public class SystemUserResource {


    private final SystemUserDao systemUserDao;

    public SystemUserResource(SystemUserDao systemUserDao) {
        this.systemUserDao = systemUserDao;
    }

    @GetMapping("/systemUsers")
    public List<CommonOptions> getSystemUsers() {
        QueryWrapper<SystemUser> queryWrapper = new QueryWrapper<>();
        var systemUsers = systemUserDao.selectList(queryWrapper);
        return CollectionUtils.transformToList(systemUsers, systemUser ->
                new CommonOptions(systemUser.getUsername(), systemUser.getId()));

    }
}
