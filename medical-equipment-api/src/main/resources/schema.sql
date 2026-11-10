-- ============== 1. 用户表 ==============
CREATE TABLE IF NOT EXISTS sys_user (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password      VARCHAR(100) NOT NULL,
    real_name     VARCHAR(50)  NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    dept          VARCHAR(50),
    phone         VARCHAR(20),
    status        TINYINT      NOT NULL DEFAULT 1,
    create_time   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============== 2. 医疗设备表 ==============
CREATE TABLE IF NOT EXISTS medical_device (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    device_name         VARCHAR(100) NOT NULL,
    model               VARCHAR(100),
    sn_code             VARCHAR(100) NOT NULL UNIQUE,
    department          VARCHAR(50),
    location            VARCHAR(100),
    supplier            VARCHAR(100),
    purchase_date       DATE,
    status              TINYINT      NOT NULL DEFAULT 1,
    last_inspect_date   DATE,
    next_inspect_date   DATE,
    verify_expire_date  DATE,
    create_time         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============== 3. 报修工单表 ==============
CREATE TABLE IF NOT EXISTS repair_order (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no       VARCHAR(30)  NOT NULL UNIQUE,
    device_id      BIGINT       NOT NULL,
    reporter_id    BIGINT       NOT NULL,
    assignee_id    BIGINT,
    description    VARCHAR(500) NOT NULL,
    images         VARCHAR(1000),
    status         TINYINT      NOT NULL DEFAULT 1,
    repair_remark  VARCHAR(500),
    rating         TINYINT,
    comment        VARCHAR(500),
    finish_time    TIMESTAMP,
    close_reason   VARCHAR(200),
    create_time    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============== 4. 巡检任务表 ==============
CREATE TABLE IF NOT EXISTS inspect_task (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    device_id     BIGINT      NOT NULL,
    task_date     DATE        NOT NULL,
    inspector_id  BIGINT,
    status        TINYINT     NOT NULL DEFAULT 1,
    result        TINYINT,
    remark        VARCHAR(500),
    images        VARCHAR(1000),
    create_time   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============== 5. 通知表 ==============
CREATE TABLE IF NOT EXISTS notification (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id       BIGINT      NOT NULL,
    title         VARCHAR(100) NOT NULL,
    content       VARCHAR(500) NOT NULL,
    type          VARCHAR(30) NOT NULL,
    is_read       TINYINT     NOT NULL DEFAULT 0,
    ref_id        BIGINT,
    create_time   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============== 6. 工单操作日志表 ==============
CREATE TABLE IF NOT EXISTS repair_order_log (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id      BIGINT      NOT NULL,
    operator_id   BIGINT      NOT NULL,
    action        VARCHAR(30) NOT NULL,
    detail        VARCHAR(500),
    create_time   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
