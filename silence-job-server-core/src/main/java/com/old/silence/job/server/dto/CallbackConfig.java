package com.old.silence.job.server.dto;


import com.old.silence.job.common.enums.ContentType;

/**
 * 回调节点配置
 *
 */

public class CallbackConfig {

    /**
     * webhook
     */
    private String webhook;

    /**
     * 请求类型
     */
    private ContentType contentType;

    /**
     * 秘钥
     */
    private String secret;

    public String getWebhook() {
        return webhook;
    }

    public void setWebhook(String webhook) {
        this.webhook = webhook;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
