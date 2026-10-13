-- ============================================================================
-- 黄石新兴考勤系统 · 值班管理 接口权限（API Scope）初始化
-- ----------------------------------------------------------------------------
-- 背景：
--   DutyController 上的 @PreAuth(permission = "duty_xxx") 走的是 BladeX 的
--   「接口权限」机制，其数据源是下面两张表：
--       blade_scope_api   —— 接口权限定义（resource_code = 权限编号）
--       blade_role_scope  —— 角色授权（scope_category = 2 表示接口权限）
--   而 blade_menu / blade_role_menu 只负责前端「菜单与按钮显隐」，
--   **不参与接口鉴权**。
--
--   因此只建菜单和按钮权限是不够的：blade_scope_api 为空时，
--   BladePermissionHandler.hasPermission() 查询结果为空 -> AuthAspect 抛
--   SecureException(UN_AUTHORIZED) -> HTTP 401「请求未授权」。
--   前端 axios 再把 401 当作会话失效，于是弹出「登录状态已失效，请重新登录」，
--   极具误导性。（见 saber3/src/axios.js 的 401 分支）
--
-- 适用：MySQL，在系统库（bladex）执行。
-- 特性：幂等（可重复执行）、只新增不删除、可通过文末脚本回滚。
-- ============================================================================

-- ---------------------------------------------------------------------------
-- 0. 前置检查：确认 duty 菜单与按钮权限已存在（否则先执行 attendance-duty.mysql.sql）
-- ---------------------------------------------------------------------------
SELECT m.id AS menu_id, m.code, m.name, m.category, m.path, m.is_deleted
FROM blade_menu m
WHERE m.code = 'duty' OR m.code LIKE 'duty\_%'
ORDER BY m.category, m.id;

-- ---------------------------------------------------------------------------
-- 1. 补接口权限定义（父菜单 id 动态取，兼容不同环境生成的雪花 id）
-- ---------------------------------------------------------------------------
INSERT INTO blade_scope_api (id, menu_id, resource_code, scope_name, scope_path, scope_type, status, is_deleted)
SELECT 2091009000000000020, m.id, 'duty_save', '值班新增', '/api/blade-attendance/duty/save', 1, 1, 0
FROM blade_menu m
WHERE m.code = 'duty_save'
  AND NOT EXISTS (SELECT 1 FROM blade_scope_api WHERE resource_code = 'duty_save');

INSERT INTO blade_scope_api (id, menu_id, resource_code, scope_name, scope_path, scope_type, status, is_deleted)
SELECT 2091009000000000021, m.id, 'duty_cancel', '值班作废', '/api/blade-attendance/duty/cancel', 1, 1, 0
FROM blade_menu m
WHERE m.code = 'duty_cancel'
  AND NOT EXISTS (SELECT 1 FROM blade_scope_api WHERE resource_code = 'duty_cancel');

INSERT INTO blade_scope_api (id, menu_id, resource_code, scope_name, scope_path, scope_type, status, is_deleted)
SELECT 2091009000000000022, m.id, 'duty_import', '值班导入', '/api/blade-attendance/duty/import', 1, 1, 0
FROM blade_menu m
WHERE m.code = 'duty_import'
  AND NOT EXISTS (SELECT 1 FROM blade_scope_api WHERE resource_code = 'duty_import');

INSERT INTO blade_scope_api (id, menu_id, resource_code, scope_name, scope_path, scope_type, status, is_deleted)
SELECT 2091009000000000023, m.id, 'duty_rollback', '值班回滚', '/api/blade-attendance/duty/rollback', 1, 1, 0
FROM blade_menu m
WHERE m.code = 'duty_rollback'
  AND NOT EXISTS (SELECT 1 FROM blade_scope_api WHERE resource_code = 'duty_rollback');

INSERT INTO blade_scope_api (id, menu_id, resource_code, scope_name, scope_path, scope_type, status, is_deleted)
SELECT 2091009000000000024, m.id, 'duty_retry', '门禁重试', '/api/blade-attendance/duty/retry', 1, 1, 0
FROM blade_menu m
WHERE m.code = 'duty_retry'
  AND NOT EXISTS (SELECT 1 FROM blade_scope_api WHERE resource_code = 'duty_retry');

INSERT INTO blade_scope_api (id, menu_id, resource_code, scope_name, scope_path, scope_type, status, is_deleted)
SELECT 2091009000000000025, m.id, 'duty_employee-bind', '人员映射', '/api/blade-attendance/duty/employee-bind', 1, 1, 0
FROM blade_menu m
WHERE m.code = 'duty_employee-bind'
  AND NOT EXISTS (SELECT 1 FROM blade_scope_api WHERE resource_code = 'duty_employee-bind');

INSERT INTO blade_scope_api (id, menu_id, resource_code, scope_name, scope_path, scope_type, status, is_deleted)
SELECT 2091009000000000026, m.id, 'duty_export', '值班导出', '/api/blade-attendance/duty/export', 1, 1, 0
FROM blade_menu m
WHERE m.code = 'duty_export'
  AND NOT EXISTS (SELECT 1 FROM blade_scope_api WHERE resource_code = 'duty_export');

-- ---------------------------------------------------------------------------
-- 2. 授权给「超级管理员」角色（按 role_alias 动态取角色 id）
--    如需授权给其它角色（如 hr / manager），把下面的 'administrator' 换成对应 role_alias，
--    或复制整段并替换 role_alias 后再次执行。
-- ---------------------------------------------------------------------------
INSERT INTO blade_role_scope (id, scope_category, scope_id, role_id)
SELECT 2091009000000000050 + k.seq, 2, s.id, r.id
FROM blade_scope_api s
JOIN blade_role r ON r.role_alias = 'administrator' AND r.is_deleted = 0
JOIN (SELECT 'duty_save' AS code, 0 AS seq
      UNION ALL SELECT 'duty_cancel', 1
      UNION ALL SELECT 'duty_import', 2
      UNION ALL SELECT 'duty_rollback', 3
      UNION ALL SELECT 'duty_retry', 4
      UNION ALL SELECT 'duty_employee-bind', 5
      UNION ALL SELECT 'duty_export', 6) k ON k.code = s.resource_code
WHERE NOT EXISTS (
  SELECT 1 FROM blade_role_scope rs
  WHERE rs.scope_category = 2 AND rs.scope_id = s.id AND rs.role_id = r.id
);

-- ---------------------------------------------------------------------------
-- 3. 清理接口权限缓存（BladePermissionHandler 会缓存查询结果）
--    SQL 侧只能提示：需要清 Redis 中 blade:sys::apiScope* 的键。
--    最省事的替代做法：让相关用户重新登录一次。
-- ---------------------------------------------------------------------------

-- ---------------------------------------------------------------------------
-- 4. 验证：应返回 7 行，且每条都能对应到一个角色授权
-- ---------------------------------------------------------------------------
SELECT s.resource_code, s.scope_name, s.scope_path, s.scope_type,
       COUNT(rs.id) AS granted_role_count
FROM blade_scope_api s
LEFT JOIN blade_role_scope rs ON rs.scope_category = 2 AND rs.scope_id = s.id
WHERE s.resource_code LIKE 'duty\_%'
GROUP BY s.id, s.resource_code, s.scope_name, s.scope_path, s.scope_type
ORDER BY s.resource_code;

-- ---------------------------------------------------------------------------
-- 回滚脚本（如需撤销本次初始化）
-- ---------------------------------------------------------------------------
-- DELETE FROM blade_role_scope WHERE scope_category = 2 AND scope_id IN
--   (SELECT id FROM blade_scope_api WHERE resource_code LIKE 'duty\_%');
-- DELETE FROM blade_scope_api WHERE resource_code LIKE 'duty\_%';
