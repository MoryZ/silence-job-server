-- Entity Design Improvement Migration Script
-- Create association tables to replace string-based ID storage
-- No foreign key constraints, no data migration

-- 1. Create association tables

-- Create Job-NotifyConfig relation table
DROP TABLE IF EXISTS sj_job_notify_config_relation;
CREATE TABLE sj_job_notify_config_relation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_id BIGINT NOT NULL,
    notify_config_id BIGINT NOT NULL,
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_job_notify (job_id, notify_config_id),
    KEY idx_job_id (job_id),
    KEY idx_notify_config_id (notify_config_id)
);

-- Create Workflow-NotifyConfig relation table
DROP TABLE IF EXISTS sj_workflow_notify_config_relation;
CREATE TABLE sj_workflow_notify_config_relation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_id BIGINT NOT NULL,
    notify_config_id BIGINT NOT NULL,
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_workflow_notify (workflow_id, notify_config_id),
    KEY idx_workflow_id (workflow_id),
    KEY idx_notify_config_id (notify_config_id)
);

-- Create RetrySceneConfig-NotifyConfig relation table
DROP TABLE IF EXISTS sj_retry_scene_config_notify_config_relation;
CREATE TABLE sj_retry_scene_config_notify_config_relation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    retry_scene_config_id BIGINT NOT NULL,
    notify_config_id BIGINT NOT NULL,
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_retry_scene_notify (retry_scene_config_id, notify_config_id),
    KEY idx_retry_scene_config_id (retry_scene_config_id),
    KEY idx_notify_config_id (notify_config_id)
);

-- Create NotifyConfig-Recipient relation table
DROP TABLE IF EXISTS sj_notify_config_recipient_relation;
CREATE TABLE sj_notify_config_recipient_relation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    notify_config_id BIGINT NOT NULL,
    recipient_id BIGINT NOT NULL,
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_notify_recipient (notify_config_id, recipient_id),
    KEY idx_notify_config_id (notify_config_id),
    KEY idx_recipient_id (recipient_id)
);
