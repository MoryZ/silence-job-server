package com.old.silence.job.server.job.task.support.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Sets;
import com.google.common.graph.MutableGraph;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.server.common.util.GraphUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;


public class MutableGraphCache {

    private static final Cache<BigInteger/*工作流批次*/, MutableGraph<BigInteger>> cache;

    static {
        cache = CacheBuilder.newBuilder()
                .concurrencyLevel(8) // 并发级别
                .expireAfterWrite(5, TimeUnit.MINUTES) // 写入后的过期时间
                .build();
    }

    /**
     * 获取指定workflowBatchId的可变图对象，若图对象不存在则使用给定的jsonGraph反序列化生成新的图对象并返回
     *
     * @param workflowBatchId 工作流批次ID
     * @param jsonGraph       JSON格式的图对象字符串
     * @return {@link MutableGraph} 图对象
     */
    public static MutableGraph<BigInteger> getOrDefault(BigInteger workflowBatchId, String jsonGraph) {
        return Optional.ofNullable(cache.getIfPresent(workflowBatchId)).orElse(GraphUtils.deserializeJsonToGraph(jsonGraph));
    }

    /**
     * 根据给定的workflowBatchId获取图对象。
     *
     * @param workflowBatchId 工作流批次ID
     * @return {@link MutableGraph} 返回对应的图对象，如果不存在则返回空图
     */
    public static MutableGraph<BigInteger> get(BigInteger workflowBatchId) {
        return getOrDefault(workflowBatchId, "");
    }

    /**
     * 获取所有的叶子节点
     *
     * @param workflowBatchId 工作流批次ID
     * @param jsonGraph       JSON格式的图对象字符串
     * @return 叶子节点
     */
    public static List<BigInteger> getLeaves(BigInteger workflowBatchId, String jsonGraph) {

        MutableGraph<BigInteger> graph = getOrDefault(workflowBatchId, jsonGraph);
        List<BigInteger> leaves = new ArrayList<>();
        for (BigInteger node : graph.nodes()) {
            if (CollectionUtils.isEmpty(graph.successors(node))) {
                leaves.add(node);
            }
        }

        return leaves;
    }

    public static Set<BigInteger> getAllDescendants(MutableGraph<BigInteger> graph, BigInteger parentId) {
        Set<BigInteger> descendants = new HashSet<>();
        getAllDescendantsHelper(graph, parentId, descendants);
        return descendants;
    }

    public static Set<BigInteger> getBrotherNode(MutableGraph<BigInteger> graph, BigInteger nodeId) {
        Set<BigInteger> predecessors = graph.predecessors(nodeId);
        if (CollectionUtils.isEmpty(predecessors)) {
            return Sets.newHashSet();
        }
        return graph.successors(predecessors.stream().findFirst().get());
    }

    private static void getAllDescendantsHelper(MutableGraph<BigInteger> graph, BigInteger parentId, Set<BigInteger> descendants) {
        Set<BigInteger> successors = graph.successors(parentId);
        descendants.addAll(successors);

        for (BigInteger successor : successors) {
            getAllDescendantsHelper(graph, successor, descendants);
        }
    }

}
