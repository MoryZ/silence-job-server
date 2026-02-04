package com.old.silence.job.server.common.util;

import cn.hutool.core.util.StrUtil;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;


public class GraphUtils {


    /**
     * 从JSON反序列化为Guava图
     *
     * @param jsonGraph 图的json串
     * @return {@link MutableGraph} 图对象
     */
    public static MutableGraph<BigInteger> deserializeJsonToGraph(String jsonGraph) {
        if (StrUtil.isBlank(jsonGraph)) {
            return null;
        }
        // 将JSON字符串转换为Map<BigInteger, Iterable<BigInteger>>
        var typeReference = new TypeReference<Map<BigInteger, Iterable<BigInteger>>>() {
        }.getType();
        Map<BigInteger, Iterable<BigInteger>> adjacencyList = JSON.parseObject(jsonGraph, typeReference);

        // 创建Guava图并添加节点和边
        MutableGraph<BigInteger> graph = GraphBuilder.directed().build();
        for (Map.Entry<BigInteger, Iterable<BigInteger>> entry : adjacencyList.entrySet()) {
            BigInteger node = entry.getKey();
            Iterable<BigInteger> successors = entry.getValue();

            graph.addNode(node);
            for (BigInteger successor : successors) {
                graph.putEdge(node, successor);
            }
        }

        return graph;
    }

    public static Map<BigInteger, Iterable<BigInteger>> serializeGraphToJson(MutableGraph<BigInteger> graph) {
        Map<BigInteger, Iterable<BigInteger>> adjacencyList = new HashMap<>();

        for (BigInteger node : graph.nodes()) {
            adjacencyList.put(node, graph.successors(node));
        }

        return adjacencyList;
    }

}
