package com.old.silence.job.server.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author moryzang
 */
public class CommonOptions {

    private final String label;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final Object value;

    public CommonOptions(String label, Object value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public Object getValue() {
        return value;
    }
}
