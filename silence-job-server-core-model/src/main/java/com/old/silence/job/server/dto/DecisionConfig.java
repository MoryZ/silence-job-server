package com.old.silence.job.server.dto;


import com.old.silence.job.common.enums.ExpressionTypeEnum;

/**
 * 决策节点配置
 *
 */

public class DecisionConfig {

    /**
     * 表达式类型 1、SpEl、2、Aviator 3、QL
     */
    private ExpressionTypeEnum expressionType;

    /**
     * 条件节点表达式
     */
    private String nodeExpression;

//    /**
//     * 判定逻辑 and 或者 or
//     */
//    private Integer logicalCondition;

    /**
     * 是否为其他情况
     */
    private Boolean defaultDecision;

    public ExpressionTypeEnum getExpressionType() {
        return expressionType;
    }

    public void setExpressionType(ExpressionTypeEnum expressionType) {
        this.expressionType = expressionType;
    }

    public String getNodeExpression() {
        return nodeExpression;
    }

    public void setNodeExpression(String nodeExpression) {
        this.nodeExpression = nodeExpression;
    }

    public Boolean getDefaultDecision() {
        return defaultDecision;
    }

    public void setDefaultDecision(Boolean defaultDecision) {
        this.defaultDecision = defaultDecision;
    }
}
