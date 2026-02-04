package com.old.silence.job.server.vo;



import java.math.BigInteger;
import java.util.List;



public class JobLogResponseVO {

    private BigInteger id;

    private BigInteger nextStartId;

    private List message;

    private boolean isFinished;

    private Integer fromIndex;

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public BigInteger getNextStartId() {
        return nextStartId;
    }

    public void setNextStartId(BigInteger nextStartId) {
        this.nextStartId = nextStartId;
    }

    public List getMessage() {
        return message;
    }

    public void setMessage(List message) {
        this.message = message;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }

    public Integer getFromIndex() {
        return fromIndex;
    }

    public void setFromIndex(Integer fromIndex) {
        this.fromIndex = fromIndex;
    }
}
