package com.old.silence.job.server.domain.service;

import java.util.List;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.old.silence.job.server.api.assembler.NamespaceMapper;
import com.old.silence.job.server.domain.model.Namespace;
import com.old.silence.job.server.infrastructure.persistence.dao.NamespaceDao;
import com.old.silence.job.server.vo.NamespaceResponseVO;
import com.old.silence.core.util.CollectionUtils;


@Service
public class NamespaceService {
    private final NamespaceDao namespaceDao;
    private final NamespaceMapper namespaceMapper;

    public NamespaceService(NamespaceDao namespaceDao, NamespaceMapper namespaceMapper) {
        this.namespaceDao = namespaceDao;
        this.namespaceMapper = namespaceMapper;
    }

    
    public int create(Namespace namespace) {
        return namespaceDao.insert(namespace);
    }

    public int update(Namespace namespace) {
        return namespaceDao.updateById(namespace);
    }

    
    public IPage<NamespaceResponseVO> query(Page<Namespace> pageDTO, QueryWrapper<Namespace> queryWrapper) {
        Page<Namespace> selectPage = namespaceDao.selectPage(pageDTO, queryWrapper);

        return selectPage.convert(namespaceMapper::convert);
    }

    
    public int deleteByUniqueId(String uniqueId) {

        return namespaceDao.delete(new LambdaQueryWrapper<Namespace>().eq(Namespace::getUniqueId, uniqueId));
    }

    
    public List<NamespaceResponseVO> getAllNamespace() {
        List<Namespace> namespaces = namespaceDao.selectList(
                new LambdaQueryWrapper<Namespace>()
                        .select(Namespace::getName, Namespace::getUniqueId)
                        .orderByDesc(Namespace::getId)
        );
        return CollectionUtils.transformToList(namespaces, namespaceMapper::convert);
    }
}
