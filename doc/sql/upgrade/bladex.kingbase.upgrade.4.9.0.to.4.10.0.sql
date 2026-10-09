-- -----------------------------------
-- 新增账号锁定时长参数
-- -----------------------------------
INSERT INTO "blade_param" ("id", "param_name", "param_key", "param_value", "remark", "create_user", "create_dept", "create_time", "update_user", "update_time", "status", "is_deleted")
VALUES (1123598819738675204, '账号锁定时长(分钟)', 'account.lockDuration', '30', '锁定时长', 1123598821738675201, 1123598813738675201, '2025-12-01 12:00:00', 1123598821738675201, '2025-12-01 12:00:00', 1, 0);

-- ----------------------------
-- 限流规则表
-- ----------------------------
CREATE TABLE "blade_rate_limit" (
  "id" int8 NOT NULL,
  "tenant_id" varchar(12) COLLATE "pg_catalog"."default" DEFAULT '000000',
  "limit_name" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
  "limit_pattern" varchar(1000) COLLATE "pg_catalog"."default" NOT NULL,
  "request_method" varchar(16) COLLATE "pg_catalog"."default" DEFAULT 'ALL',
  "limit_dimension" varchar(16) COLLATE "pg_catalog"."default" DEFAULT 'IP',
  "limit_count" int4 DEFAULT 100,
  "limit_period" int4 DEFAULT 1,
  "time_unit" varchar(16) COLLATE "pg_catalog"."default" DEFAULT 'MINUTES',
  "priority" int4 DEFAULT 1,
  "remark" varchar(255) COLLATE "pg_catalog"."default",
  "create_user" int8,
  "create_dept" int8,
  "create_time" timestamp(6),
  "update_user" int8,
  "update_time" timestamp(6),
  "status" int4 DEFAULT 1,
  "is_deleted" int4 DEFAULT 0
)
;
COMMENT ON COLUMN "blade_rate_limit"."id" IS '主键';
COMMENT ON COLUMN "blade_rate_limit"."tenant_id" IS '租户ID';
COMMENT ON COLUMN "blade_rate_limit"."limit_name" IS '规则名称';
COMMENT ON COLUMN "blade_rate_limit"."limit_pattern" IS 'URL匹配模式';
COMMENT ON COLUMN "blade_rate_limit"."request_method" IS '请求方法';
COMMENT ON COLUMN "blade_rate_limit"."limit_dimension" IS '限流维度';
COMMENT ON COLUMN "blade_rate_limit"."limit_count" IS '请求阈值';
COMMENT ON COLUMN "blade_rate_limit"."limit_period" IS '时间窗口';
COMMENT ON COLUMN "blade_rate_limit"."time_unit" IS '时间单位';
COMMENT ON COLUMN "blade_rate_limit"."priority" IS '优先级';
COMMENT ON COLUMN "blade_rate_limit"."remark" IS '备注';
COMMENT ON COLUMN "blade_rate_limit"."create_user" IS '创建人';
COMMENT ON COLUMN "blade_rate_limit"."create_dept" IS '创建部门';
COMMENT ON COLUMN "blade_rate_limit"."create_time" IS '创建时间';
COMMENT ON COLUMN "blade_rate_limit"."update_user" IS '修改人';
COMMENT ON COLUMN "blade_rate_limit"."update_time" IS '修改时间';
COMMENT ON COLUMN "blade_rate_limit"."status" IS '状态';
COMMENT ON COLUMN "blade_rate_limit"."is_deleted" IS '是否已删除';
COMMENT ON TABLE "blade_rate_limit" IS '限流规则表';
ALTER TABLE "blade_rate_limit" ADD CONSTRAINT "blade_rate_limit_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- 限流触发日志表
-- ----------------------------
CREATE TABLE "blade_rate_limit_log" (
  "id" int8 NOT NULL,
  "tenant_id" varchar(12) COLLATE "pg_catalog"."default" DEFAULT '000000',
  "service_id" varchar(32) COLLATE "pg_catalog"."default",
  "server_host" varchar(255) COLLATE "pg_catalog"."default",
  "server_ip" varchar(255) COLLATE "pg_catalog"."default",
  "env" varchar(255) COLLATE "pg_catalog"."default",
  "limit_id" int8,
  "limit_name" varchar(128) COLLATE "pg_catalog"."default",
  "limit_dimension" varchar(16) COLLATE "pg_catalog"."default",
  "limit_count" int4,
  "limit_period" int4,
  "time_unit" varchar(16) COLLATE "pg_catalog"."default",
  "limit_key" varchar(255) COLLATE "pg_catalog"."default",
  "method" varchar(10) COLLATE "pg_catalog"."default",
  "request_uri" varchar(255) COLLATE "pg_catalog"."default",
  "user_agent" varchar(1000) COLLATE "pg_catalog"."default",
  "remote_ip" varchar(255) COLLATE "pg_catalog"."default",
  "params" text COLLATE "pg_catalog"."default",
  "create_by" varchar(64) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6)
)
;
COMMENT ON COLUMN "blade_rate_limit_log"."id" IS '编号';
COMMENT ON COLUMN "blade_rate_limit_log"."tenant_id" IS '租户ID';
COMMENT ON COLUMN "blade_rate_limit_log"."service_id" IS '服务ID';
COMMENT ON COLUMN "blade_rate_limit_log"."server_host" IS '服务器名';
COMMENT ON COLUMN "blade_rate_limit_log"."server_ip" IS '服务器IP地址';
COMMENT ON COLUMN "blade_rate_limit_log"."env" IS '服务器环境';
COMMENT ON COLUMN "blade_rate_limit_log"."limit_id" IS '限流规则主键ID';
COMMENT ON COLUMN "blade_rate_limit_log"."limit_name" IS '规则名称';
COMMENT ON COLUMN "blade_rate_limit_log"."limit_dimension" IS '限流维度';
COMMENT ON COLUMN "blade_rate_limit_log"."limit_count" IS '请求阈值';
COMMENT ON COLUMN "blade_rate_limit_log"."limit_period" IS '时间窗口';
COMMENT ON COLUMN "blade_rate_limit_log"."time_unit" IS '时间单位';
COMMENT ON COLUMN "blade_rate_limit_log"."limit_key" IS '限流计数key';
COMMENT ON COLUMN "blade_rate_limit_log"."method" IS '操作方式';
COMMENT ON COLUMN "blade_rate_limit_log"."request_uri" IS '请求URI';
COMMENT ON COLUMN "blade_rate_limit_log"."user_agent" IS '用户代理';
COMMENT ON COLUMN "blade_rate_limit_log"."remote_ip" IS '操作IP地址';
COMMENT ON COLUMN "blade_rate_limit_log"."params" IS '操作提交的数据';
COMMENT ON COLUMN "blade_rate_limit_log"."create_by" IS '创建者';
COMMENT ON COLUMN "blade_rate_limit_log"."create_time" IS '创建时间';
COMMENT ON TABLE "blade_rate_limit_log" IS '限流触发日志表';
ALTER TABLE "blade_rate_limit_log" ADD CONSTRAINT "blade_rate_limit_log_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- 限流管理菜单
-- ----------------------------
INSERT INTO "blade_menu" ("id", "parent_id", "code", "name", "alias", "path", "source", "sort", "category", "action", "is_open", "component", "remark", "is_deleted") VALUES
(1963598815738675321, 1123598815738675307, 'rate_limit', '限流管理', 'menu', '/authority/ratelimit', 'iconfont icon-APIjieru', 5, 1, 0, 1, '', NULL, 0),
(1963598815738675322, 1963598815738675321, 'rate_limit_add', '新增', 'add', '/authority/ratelimit/add', 'plus', 1, 2, 1, 1, '', NULL, 0),
(1963598815738675323, 1963598815738675321, 'rate_limit_edit', '修改', 'edit', '/authority/ratelimit/edit', 'form', 2, 2, 2, 1, '', NULL, 0),
(1963598815738675324, 1963598815738675321, 'rate_limit_delete', '删除', 'delete', '/api/blade-system/rate-limit/remove', 'delete', 3, 2, 3, 1, '', NULL, 0),
(1963598815738675325, 1963598815738675321, 'rate_limit_view', '查看', 'view', '/authority/ratelimit/view', 'file-text', 4, 2, 2, 1, '', NULL, 0);

-- ----------------------------
-- 客户端授权类型增加行为验证码模式
-- ----------------------------
UPDATE "blade_client" SET "authorized_grant_types" = CONCAT("authorized_grant_types", ',behavior') WHERE "authorized_grant_types" NOT LIKE '%behavior%';
