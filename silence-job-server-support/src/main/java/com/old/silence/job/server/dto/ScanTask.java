package com.old.silence.job.server.common.dto;



import java.util.Set;

/**
 * 扫描任务模型
 *
 */

public class ScanTask {

//    private String namespaceId;
//
//    private String groupName;

    private Set<Integer> buckets;

//    private Integer groupPartition;


    public Set<Integer> getBuckets() {
        return buckets;
    }

    public void setBuckets(Set<Integer> buckets) {
        this.buckets = buckets;
    }
}
