-- -----------------------------------
-- 新增账号锁定时长参数
-- -----------------------------------
INSERT INTO `blade_param` (`id`, `param_name`, `param_key`, `param_value`, `remark`, `create_user`, `create_dept`, `create_time`, `update_user`, `update_time`, `status`, `is_deleted`)
VALUES (1123598819738675204, '账号锁定时长(分钟)', 'account.lockDuration', '30', '锁定时长', 1123598821738675201, 1123598813738675201, '2025-12-01 12:00:00', 1123598821738675201, '2025-12-01 12:00:00', 1, 0);

-- ----------------------------
-- 限流规则表
-- ----------------------------
CREATE TABLE `blade_rate_limit`  (
  `id` bigint NOT NULL COMMENT '主键',
  `tenant_id` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '000000' COMMENT '租户ID',
  `limit_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '规则名称',
  `limit_pattern` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'URL匹配模式',
  `request_method` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'ALL' COMMENT '请求方法',
  `limit_dimension` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'IP' COMMENT '限流维度',
  `limit_count` int NULL DEFAULT 100 COMMENT '请求阈值',
  `limit_period` int NULL DEFAULT 1 COMMENT '时间窗口',
  `time_unit` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'MINUTES' COMMENT '时间单位',
  `priority` int NULL DEFAULT 1 COMMENT '优先级',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_user` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_dept` bigint NULL DEFAULT NULL COMMENT '创建部门',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_user` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `status` int NULL DEFAULT 1 COMMENT '状态',
  `is_deleted` int NULL DEFAULT 0 COMMENT '是否已删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '限流规则表';

-- ----------------------------
-- 限流触发日志表
-- ----------------------------
CREATE TABLE `blade_rate_limit_log`  (
  `id` bigint NOT NULL COMMENT '编号',
  `tenant_id` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '000000' COMMENT '租户ID',
  `service_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '服务ID',
  `server_host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '服务器名',
  `server_ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '服务器IP地址',
  `env` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '服务器环境',
  `limit_id` bigint NULL DEFAULT NULL COMMENT '限流规则主键ID',
  `limit_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '规则名称',
  `limit_dimension` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '限流维度',
  `limit_count` int NULL DEFAULT NULL COMMENT '请求阈值',
  `limit_period` int NULL DEFAULT NULL COMMENT '时间窗口',
  `time_unit` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '时间单位',
  `limit_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '限流计数key',
  `method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作方式',
  `request_uri` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求URI',
  `user_agent` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户代理',
  `remote_ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作IP地址',
  `params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '操作提交的数据',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '限流触发日志表';

-- ----------------------------
-- 限流管理菜单
-- ----------------------------
INSERT INTO `blade_menu` (`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `component`, `remark`, `is_deleted`) VALUES
(1963598815738675321, 1123598815738675307, 'rate_limit', '限流管理', 'menu', '/authority/ratelimit', 'iconfont icon-APIjieru', 5, 1, 0, 1, '', NULL, 0),
(1963598815738675322, 1963598815738675321, 'rate_limit_add', '新增', 'add', '/authority/ratelimit/add', 'plus', 1, 2, 1, 1, '', NULL, 0),
(1963598815738675323, 1963598815738675321, 'rate_limit_edit', '修改', 'edit', '/authority/ratelimit/edit', 'form', 2, 2, 2, 1, '', NULL, 0),
(1963598815738675324, 1963598815738675321, 'rate_limit_delete', '删除', 'delete', '/api/blade-system/rate-limit/remove', 'delete', 3, 2, 3, 1, '', NULL, 0),
(1963598815738675325, 1963598815738675321, 'rate_limit_view', '查看', 'view', '/authority/ratelimit/view', 'file-text', 4, 2, 2, 1, '', NULL, 0);

-- ----------------------------
-- 客户端授权类型增加行为验证码模式
-- ----------------------------
UPDATE `blade_client` SET `authorized_grant_types` = CONCAT(`authorized_grant_types`, ',behavior') WHERE `authorized_grant_types` NOT LIKE '%behavior%';
