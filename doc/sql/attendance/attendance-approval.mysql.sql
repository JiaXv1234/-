-- 审批中心菜单、按钮、接口及管理员授权。仅在 bladex 系统库执行；不变更考勤表结构。
-- 普通角色须在角色管理中分别授权菜单/按钮及接口权限；不自动开放审批给全部用户。
START TRANSACTION;

INSERT INTO blade_menu (id,parent_id,code,name,alias,path,source,sort,category,action,is_open,component,is_deleted)
SELECT 2091010000000000001, p.id, 'attendance_approval','审批中心','menu','/attendance/approval',
'iconfont iconicon_task',2,1,0,1,'views/attendance/approval',0
FROM blade_menu p WHERE p.code='attendance' AND p.is_deleted=0
AND NOT EXISTS (SELECT 1 FROM blade_menu WHERE code='attendance_approval');

INSERT INTO blade_menu (id,parent_id,code,name,alias,path,source,sort,category,action,is_open,component,is_deleted)
SELECT 2091010000000000010,p.id,'attendance_approval_agree','同意','agree','/api/blade-attendance/approval/agree','',1,2,1,1,'',0
FROM blade_menu p WHERE p.code='attendance_approval' AND p.is_deleted=0
AND NOT EXISTS (SELECT 1 FROM blade_menu WHERE code='attendance_approval_agree');

INSERT INTO blade_scope_api (id,menu_id,resource_code,scope_name,scope_path,scope_type,status,is_deleted)
SELECT 2091010000000000020,m.id,'attendance_approval_agree','审批同意','/api/blade-attendance/approval/agree',1,1,0
FROM blade_menu m WHERE m.code='attendance_approval_agree' AND m.is_deleted=0
AND NOT EXISTS (SELECT 1 FROM blade_scope_api WHERE resource_code='attendance_approval_agree');

INSERT INTO blade_role_scope (id,scope_category,scope_id,role_id)
SELECT 2091010000000000050,2,s.id,r.id
FROM blade_scope_api s JOIN blade_role r ON r.role_alias='administrator' AND r.tenant_id='000000' AND r.is_deleted=0
WHERE s.resource_code='attendance_approval_agree' AND s.is_deleted=0
AND NOT EXISTS (SELECT 1 FROM blade_role_scope rs WHERE rs.scope_category=2 AND rs.scope_id=s.id AND rs.role_id=r.id);

INSERT INTO blade_menu (id,parent_id,code,name,alias,path,source,sort,category,action,is_open,component,is_deleted)
SELECT 2091010000000000011,p.id,'attendance_approval_reject','退回','reject','/api/blade-attendance/approval/reject','',2,2,1,1,'',0
FROM blade_menu p WHERE p.code='attendance_approval' AND p.is_deleted=0
AND NOT EXISTS (SELECT 1 FROM blade_menu WHERE code='attendance_approval_reject');

INSERT INTO blade_scope_api (id,menu_id,resource_code,scope_name,scope_path,scope_type,status,is_deleted)
SELECT 2091010000000000021,m.id,'attendance_approval_reject','审批退回','/api/blade-attendance/approval/reject',1,1,0
FROM blade_menu m WHERE m.code='attendance_approval_reject' AND m.is_deleted=0
AND NOT EXISTS (SELECT 1 FROM blade_scope_api WHERE resource_code='attendance_approval_reject');

INSERT INTO blade_role_scope (id,scope_category,scope_id,role_id)
SELECT 2091010000000000051,2,s.id,r.id
FROM blade_scope_api s JOIN blade_role r ON r.role_alias='administrator' AND r.tenant_id='000000' AND r.is_deleted=0
WHERE s.resource_code='attendance_approval_reject' AND s.is_deleted=0
AND NOT EXISTS (SELECT 1 FROM blade_role_scope rs WHERE rs.scope_category=2 AND rs.scope_id=s.id AND rs.role_id=r.id);

INSERT INTO blade_menu (id,parent_id,code,name,alias,path,source,sort,category,action,is_open,component,is_deleted)
SELECT 2091010000000000012,p.id,'attendance_approval_cc','抄送','cc','/api/blade-attendance/approval/cc','',3,2,1,1,'',0
FROM blade_menu p WHERE p.code='attendance_approval' AND p.is_deleted=0
AND NOT EXISTS (SELECT 1 FROM blade_menu WHERE code='attendance_approval_cc');

INSERT INTO blade_scope_api (id,menu_id,resource_code,scope_name,scope_path,scope_type,status,is_deleted)
SELECT 2091010000000000022,m.id,'attendance_approval_cc','审批抄送','/api/blade-attendance/approval/cc',1,1,0
FROM blade_menu m WHERE m.code='attendance_approval_cc' AND m.is_deleted=0
AND NOT EXISTS (SELECT 1 FROM blade_scope_api WHERE resource_code='attendance_approval_cc');

INSERT INTO blade_role_scope (id,scope_category,scope_id,role_id)
SELECT 2091010000000000052,2,s.id,r.id
FROM blade_scope_api s JOIN blade_role r ON r.role_alias='administrator' AND r.tenant_id='000000' AND r.is_deleted=0
WHERE s.resource_code='attendance_approval_cc' AND s.is_deleted=0
AND NOT EXISTS (SELECT 1 FROM blade_role_scope rs WHERE rs.scope_category=2 AND rs.scope_id=s.id AND rs.role_id=r.id);

INSERT INTO blade_menu (id,parent_id,code,name,alias,path,source,sort,category,action,is_open,component,is_deleted)
SELECT 2091010000000000013,p.id,'attendance_approval_read','标记已读','read','/api/blade-attendance/approval/read','',4,2,1,1,'',0
FROM blade_menu p WHERE p.code='attendance_approval' AND p.is_deleted=0
AND NOT EXISTS (SELECT 1 FROM blade_menu WHERE code='attendance_approval_read');

INSERT INTO blade_scope_api (id,menu_id,resource_code,scope_name,scope_path,scope_type,status,is_deleted)
SELECT 2091010000000000023,m.id,'attendance_approval_read','审批标记已读','/api/blade-attendance/approval/read',1,1,0
FROM blade_menu m WHERE m.code='attendance_approval_read' AND m.is_deleted=0
AND NOT EXISTS (SELECT 1 FROM blade_scope_api WHERE resource_code='attendance_approval_read');

INSERT INTO blade_role_scope (id,scope_category,scope_id,role_id)
SELECT 2091010000000000053,2,s.id,r.id
FROM blade_scope_api s JOIN blade_role r ON r.role_alias='administrator' AND r.tenant_id='000000' AND r.is_deleted=0
WHERE s.resource_code='attendance_approval_read' AND s.is_deleted=0
AND NOT EXISTS (SELECT 1 FROM blade_role_scope rs WHERE rs.scope_category=2 AND rs.scope_id=s.id AND rs.role_id=r.id);

INSERT INTO blade_role_menu (id,menu_id,role_id)
SELECT 2091010000000000060,m.id,r.id
FROM blade_menu m JOIN blade_role r ON r.role_alias='administrator' AND r.tenant_id='000000' AND r.is_deleted=0
WHERE m.code='attendance' AND m.is_deleted=0
AND NOT EXISTS (SELECT 1 FROM blade_role_menu rm WHERE rm.menu_id=m.id AND rm.role_id=r.id);

INSERT INTO blade_role_menu (id,menu_id,role_id)
SELECT 2091010000000000061,m.id,r.id
FROM blade_menu m JOIN blade_role r ON r.role_alias='administrator' AND r.tenant_id='000000' AND r.is_deleted=0
WHERE m.code='attendance_approval' AND m.is_deleted=0
AND NOT EXISTS (SELECT 1 FROM blade_role_menu rm WHERE rm.menu_id=m.id AND rm.role_id=r.id);

INSERT INTO blade_role_menu (id,menu_id,role_id)
SELECT 2091010000000000062,m.id,r.id
FROM blade_menu m JOIN blade_role r ON r.role_alias='administrator' AND r.tenant_id='000000' AND r.is_deleted=0
WHERE m.code='attendance_approval_agree' AND m.is_deleted=0
AND NOT EXISTS (SELECT 1 FROM blade_role_menu rm WHERE rm.menu_id=m.id AND rm.role_id=r.id);

INSERT INTO blade_role_menu (id,menu_id,role_id)
SELECT 2091010000000000063,m.id,r.id
FROM blade_menu m JOIN blade_role r ON r.role_alias='administrator' AND r.tenant_id='000000' AND r.is_deleted=0
WHERE m.code='attendance_approval_reject' AND m.is_deleted=0
AND NOT EXISTS (SELECT 1 FROM blade_role_menu rm WHERE rm.menu_id=m.id AND rm.role_id=r.id);

INSERT INTO blade_role_menu (id,menu_id,role_id)
SELECT 2091010000000000064,m.id,r.id
FROM blade_menu m JOIN blade_role r ON r.role_alias='administrator' AND r.tenant_id='000000' AND r.is_deleted=0
WHERE m.code='attendance_approval_cc' AND m.is_deleted=0
AND NOT EXISTS (SELECT 1 FROM blade_role_menu rm WHERE rm.menu_id=m.id AND rm.role_id=r.id);

INSERT INTO blade_role_menu (id,menu_id,role_id)
SELECT 2091010000000000065,m.id,r.id
FROM blade_menu m JOIN blade_role r ON r.role_alias='administrator' AND r.tenant_id='000000' AND r.is_deleted=0
WHERE m.code='attendance_approval_read' AND m.is_deleted=0
AND NOT EXISTS (SELECT 1 FROM blade_role_menu rm WHERE rm.menu_id=m.id AND rm.role_id=r.id);

COMMIT;

SELECT m.code,m.name,m.path,p.name AS parent_name FROM blade_menu m
LEFT JOIN blade_menu p ON p.id=m.parent_id
WHERE m.code='attendance_approval' OR m.parent_id=2091010000000000001;
SELECT s.resource_code,s.scope_path,COUNT(rs.id) AS role_grants FROM blade_scope_api s
LEFT JOIN blade_role_scope rs ON rs.scope_category=2 AND rs.scope_id=s.id
WHERE s.resource_code IN ('attendance_approval_agree','attendance_approval_reject','attendance_approval_cc','attendance_approval_read')
GROUP BY s.id,s.resource_code,s.scope_path;

