package com.old.silence.job.server.dispatch;

import java.util.Set;


public class ConsumerBucket {

    private Set<Integer> buckets;

    public Set<Integer> getBuckets() {
        return buckets;
    }

    public void setBuckets(Set<Integer> buckets) {
        this.buckets = buckets;
    }
}
