package com.old.silence.job.server.vo;


import java.math.BigInteger;

public class CommonLabelValueResponseVO {

    private String label;

    private BigInteger value;

    public CommonLabelValueResponseVO() {
    }

    public CommonLabelValueResponseVO(String label, BigInteger value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public BigInteger getValue() {
        return value;
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }
}
