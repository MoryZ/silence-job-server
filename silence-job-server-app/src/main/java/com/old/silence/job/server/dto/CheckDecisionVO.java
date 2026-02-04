package com.old.silence.job.server.dto;

import com.old.silence.job.common.enums.ExpressionTypeEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;



public class CheckDecisionVO {

    /**
     * 表达式类型 1、SpEl、2、Aviator 3、QL
     */
    @NotNull
    private ExpressionTypeEnum expressionType;

    /**
     * 条件节点表达式
     */
    @NotBlank
    private String nodeExpression;

    /**
     * 决策节点校验内容
     */
    private String checkContent;

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

    public String getCheckContent() {
        return checkContent;
    }

    public void setCheckContent(String checkContent) {
        this.checkContent = checkContent;
    }
}
