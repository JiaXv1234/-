-- -----------------------------------
-- 新增账号锁定时长参数
-- -----------------------------------
INSERT INTO [dbo].[blade_param] ([id], [param_name], [param_key], [param_value], [remark], [create_user], [create_dept], [create_time], [update_user], [update_time], [status], [is_deleted])
VALUES (N'1123598819738675204', N'账号锁定时长(分钟)', N'account.lockDuration', N'30', N'锁定时长', N'1123598821738675201', N'1123598813738675201', N'2025-12-01 12:00:00', N'1123598821738675201', N'2025-12-01 12:00:00', N'1', N'0')
GO

-- ----------------------------
-- 限流规则表
-- ----------------------------
CREATE TABLE [dbo].[blade_rate_limit] (
  [id] bigint  NOT NULL,
  [tenant_id] nvarchar(12) COLLATE Chinese_PRC_CI_AS DEFAULT '000000' NULL,
  [limit_name] nvarchar(128) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [limit_pattern] nvarchar(1000) COLLATE Chinese_PRC_CI_AS  NOT NULL,
  [request_method] nvarchar(16) COLLATE Chinese_PRC_CI_AS DEFAULT 'ALL' NULL,
  [limit_dimension] nvarchar(16) COLLATE Chinese_PRC_CI_AS DEFAULT 'IP' NULL,
  [limit_count] int DEFAULT 100 NULL,
  [limit_period] int DEFAULT 1 NULL,
  [time_unit] nvarchar(16) COLLATE Chinese_PRC_CI_AS DEFAULT 'MINUTES' NULL,
  [priority] int DEFAULT 1 NULL,
  [remark] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [create_user] bigint  NULL,
  [create_dept] bigint  NULL,
  [create_time] datetime2(0)  NULL,
  [update_user] bigint  NULL,
  [update_time] datetime2(0)  NULL,
  [status] int DEFAULT 1 NULL,
  [is_deleted] int DEFAULT 0 NULL
)
GO

ALTER TABLE [dbo].[blade_rate_limit] SET (LOCK_ESCALATION = TABLE)
GO

ALTER TABLE [dbo].[blade_rate_limit] ADD CONSTRAINT [PK_blade_rate_limit] PRIMARY KEY CLUSTERED ([id])
GO

EXEC sp_addextendedproperty 'MS_Description', N'主键', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit', 'COLUMN', N'id'
GO
EXEC sp_addextendedproperty 'MS_Description', N'租户ID', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit', 'COLUMN', N'tenant_id'
GO
EXEC sp_addextendedproperty 'MS_Description', N'规则名称', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit', 'COLUMN', N'limit_name'
GO
EXEC sp_addextendedproperty 'MS_Description', N'URL匹配模式', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit', 'COLUMN', N'limit_pattern'
GO
EXEC sp_addextendedproperty 'MS_Description', N'请求方法', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit', 'COLUMN', N'request_method'
GO
EXEC sp_addextendedproperty 'MS_Description', N'限流维度', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit', 'COLUMN', N'limit_dimension'
GO
EXEC sp_addextendedproperty 'MS_Description', N'请求阈值', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit', 'COLUMN', N'limit_count'
GO
EXEC sp_addextendedproperty 'MS_Description', N'时间窗口', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit', 'COLUMN', N'limit_period'
GO
EXEC sp_addextendedproperty 'MS_Description', N'时间单位', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit', 'COLUMN', N'time_unit'
GO
EXEC sp_addextendedproperty 'MS_Description', N'优先级', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit', 'COLUMN', N'priority'
GO
EXEC sp_addextendedproperty 'MS_Description', N'备注', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit', 'COLUMN', N'remark'
GO
EXEC sp_addextendedproperty 'MS_Description', N'创建人', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit', 'COLUMN', N'create_user'
GO
EXEC sp_addextendedproperty 'MS_Description', N'创建部门', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit', 'COLUMN', N'create_dept'
GO
EXEC sp_addextendedproperty 'MS_Description', N'创建时间', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit', 'COLUMN', N'create_time'
GO
EXEC sp_addextendedproperty 'MS_Description', N'修改人', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit', 'COLUMN', N'update_user'
GO
EXEC sp_addextendedproperty 'MS_Description', N'修改时间', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit', 'COLUMN', N'update_time'
GO
EXEC sp_addextendedproperty 'MS_Description', N'状态', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit', 'COLUMN', N'status'
GO
EXEC sp_addextendedproperty 'MS_Description', N'是否已删除', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit', 'COLUMN', N'is_deleted'
GO
EXEC sp_addextendedproperty 'MS_Description', N'限流规则表', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit'
GO

-- ----------------------------
-- 限流触发日志表
-- ----------------------------
CREATE TABLE [dbo].[blade_rate_limit_log] (
  [id] bigint  NOT NULL,
  [tenant_id] nvarchar(12) COLLATE Chinese_PRC_CI_AS DEFAULT '000000' NULL,
  [service_id] nvarchar(32) COLLATE Chinese_PRC_CI_AS  NULL,
  [server_host] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [server_ip] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [env] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [limit_id] bigint  NULL,
  [limit_name] nvarchar(128) COLLATE Chinese_PRC_CI_AS  NULL,
  [limit_dimension] nvarchar(16) COLLATE Chinese_PRC_CI_AS  NULL,
  [limit_count] int  NULL,
  [limit_period] int  NULL,
  [time_unit] nvarchar(16) COLLATE Chinese_PRC_CI_AS  NULL,
  [limit_key] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [method] nvarchar(10) COLLATE Chinese_PRC_CI_AS  NULL,
  [request_uri] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [user_agent] nvarchar(1000) COLLATE Chinese_PRC_CI_AS  NULL,
  [remote_ip] nvarchar(255) COLLATE Chinese_PRC_CI_AS  NULL,
  [params] nvarchar(max) COLLATE Chinese_PRC_CI_AS  NULL,
  [create_by] nvarchar(64) COLLATE Chinese_PRC_CI_AS  NULL,
  [create_time] datetime2(0)  NULL
)
GO

ALTER TABLE [dbo].[blade_rate_limit_log] SET (LOCK_ESCALATION = TABLE)
GO

ALTER TABLE [dbo].[blade_rate_limit_log] ADD CONSTRAINT [PK_blade_rate_limit_log] PRIMARY KEY CLUSTERED ([id])
GO

EXEC sp_addextendedproperty 'MS_Description', N'编号', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit_log', 'COLUMN', N'id'
GO
EXEC sp_addextendedproperty 'MS_Description', N'租户ID', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit_log', 'COLUMN', N'tenant_id'
GO
EXEC sp_addextendedproperty 'MS_Description', N'服务ID', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit_log', 'COLUMN', N'service_id'
GO
EXEC sp_addextendedproperty 'MS_Description', N'服务器名', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit_log', 'COLUMN', N'server_host'
GO
EXEC sp_addextendedproperty 'MS_Description', N'服务器IP地址', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit_log', 'COLUMN', N'server_ip'
GO
EXEC sp_addextendedproperty 'MS_Description', N'服务器环境', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit_log', 'COLUMN', N'env'
GO
EXEC sp_addextendedproperty 'MS_Description', N'限流规则主键ID', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit_log', 'COLUMN', N'limit_id'
GO
EXEC sp_addextendedproperty 'MS_Description', N'规则名称', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit_log', 'COLUMN', N'limit_name'
GO
EXEC sp_addextendedproperty 'MS_Description', N'限流维度', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit_log', 'COLUMN', N'limit_dimension'
GO
EXEC sp_addextendedproperty 'MS_Description', N'请求阈值', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit_log', 'COLUMN', N'limit_count'
GO
EXEC sp_addextendedproperty 'MS_Description', N'时间窗口', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit_log', 'COLUMN', N'limit_period'
GO
EXEC sp_addextendedproperty 'MS_Description', N'时间单位', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit_log', 'COLUMN', N'time_unit'
GO
EXEC sp_addextendedproperty 'MS_Description', N'限流计数key', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit_log', 'COLUMN', N'limit_key'
GO
EXEC sp_addextendedproperty 'MS_Description', N'操作方式', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit_log', 'COLUMN', N'method'
GO
EXEC sp_addextendedproperty 'MS_Description', N'请求URI', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit_log', 'COLUMN', N'request_uri'
GO
EXEC sp_addextendedproperty 'MS_Description', N'用户代理', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit_log', 'COLUMN', N'user_agent'
GO
EXEC sp_addextendedproperty 'MS_Description', N'操作IP地址', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit_log', 'COLUMN', N'remote_ip'
GO
EXEC sp_addextendedproperty 'MS_Description', N'操作提交的数据', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit_log', 'COLUMN', N'params'
GO
EXEC sp_addextendedproperty 'MS_Description', N'创建者', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit_log', 'COLUMN', N'create_by'
GO
EXEC sp_addextendedproperty 'MS_Description', N'创建时间', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit_log', 'COLUMN', N'create_time'
GO
EXEC sp_addextendedproperty 'MS_Description', N'限流触发日志表', 'SCHEMA', N'dbo', 'TABLE', N'blade_rate_limit_log'
GO

-- ----------------------------
-- 限流管理菜单
-- ----------------------------
INSERT INTO [dbo].[blade_menu] ([id], [parent_id], [code], [name], [alias], [path], [source], [sort], [category], [action], [is_open], [component], [remark], [is_deleted]) VALUES (N'1963598815738675321', N'1123598815738675307', N'rate_limit', N'限流管理', N'menu', N'/authority/ratelimit', N'iconfont icon-APIjieru', N'5', N'1', N'0', N'1', N'', NULL, N'0'), (N'1963598815738675322', N'1963598815738675321', N'rate_limit_add', N'新增', N'add', N'/authority/ratelimit/add', N'plus', N'1', N'2', N'1', N'1', N'', NULL, N'0'), (N'1963598815738675323', N'1963598815738675321', N'rate_limit_edit', N'修改', N'edit', N'/authority/ratelimit/edit', N'form', N'2', N'2', N'2', N'1', N'', NULL, N'0'), (N'1963598815738675324', N'1963598815738675321', N'rate_limit_delete', N'删除', N'delete', N'/api/blade-system/rate-limit/remove', N'delete', N'3', N'2', N'3', N'1', N'', NULL, N'0'), (N'1963598815738675325', N'1963598815738675321', N'rate_limit_view', N'查看', N'view', N'/authority/ratelimit/view', N'file-text', N'4', N'2', N'2', N'1', N'', NULL, N'0')
GO

-- ----------------------------
-- 客户端授权类型增加行为验证码模式
-- ----------------------------
UPDATE [dbo].[blade_client] SET [authorized_grant_types] = CONCAT([authorized_grant_types], ',behavior') WHERE [authorized_grant_types] NOT LIKE '%behavior%'
GO
