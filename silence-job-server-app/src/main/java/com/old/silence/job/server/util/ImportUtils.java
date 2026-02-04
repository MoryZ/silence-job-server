package com.old.silence.job.server.util;

import cn.hutool.core.io.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;
import com.alibaba.fastjson2.JSON;
import com.old.silence.job.common.exception.SilenceJobCommonException;

import java.io.IOException;
import java.util.List;

public final class ImportUtils {

    private static final List<String> FILE_EXTENSIONS = List.of("json");

    public static @NotNull <VO> List<VO> parseList(MultipartFile file, Class<VO> clazz) throws IOException {
        if (file.isEmpty()) {
            throw new SilenceJobCommonException("请选择一个文件上传");
        }

        // 保存文件到服务器
        String suffix = FileUtil.getSuffix(file.getOriginalFilename());
        if (!FILE_EXTENSIONS.contains(suffix)) {
            throw new SilenceJobCommonException("文件类型错误");
        }

        Object node = JSON.toJSON(file.getBytes());
        return JSON.parseArray(JSON.toJSONString(node), clazz);
    }
}


