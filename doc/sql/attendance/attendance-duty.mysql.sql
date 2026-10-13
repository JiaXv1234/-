-- 仅菜单初始化：在 bladex 系统库执行。attendance 业务表由系统设计说明书维护。
INSERT INTO blade_menu (id,parent_id,code,name,alias,path,source,sort,category,action,is_open,component,is_deleted)
SELECT 2091009000000000001,0,'attendance','考勤管理','menu','/attendance','iconfont iconicon_group',20,1,0,1,'',0
WHERE NOT EXISTS (SELECT 1 FROM blade_menu WHERE id=2091009000000000001);
INSERT INTO blade_menu (id,parent_id,code,name,alias,path,source,sort,category,action,is_open,component,is_deleted)
SELECT 2091009000000000002,2091009000000000001,'duty','值班管理','menu','/attendance/duty','iconfont iconicon_task',1,1,0,1,'views/attendance/duty',0
WHERE NOT EXISTS (SELECT 1 FROM blade_menu WHERE id=2091009000000000002);
INSERT INTO blade_menu (id,parent_id,code,name,alias,path,source,sort,category,action,is_open,component,is_deleted)
SELECT 2091009000000000010,2091009000000000002,'duty_save','新增','save','/api/blade-attendance/duty/save','',1,2,1,1,'',0
WHERE NOT EXISTS (SELECT 1 FROM blade_menu WHERE id=2091009000000000010);
INSERT INTO blade_menu (id,parent_id,code,name,alias,path,source,sort,category,action,is_open,component,is_deleted)
SELECT 2091009000000000011,2091009000000000002,'duty_cancel','作废','cancel','/api/blade-attendance/duty/cancel','',2,2,3,1,'',0
WHERE NOT EXISTS (SELECT 1 FROM blade_menu WHERE id=2091009000000000011);
INSERT INTO blade_menu (id,parent_id,code,name,alias,path,source,sort,category,action,is_open,component,is_deleted)
SELECT 2091009000000000012,2091009000000000002,'duty_import','导入','import','/api/blade-attendance/duty/import','',3,2,1,1,'',0
WHERE NOT EXISTS (SELECT 1 FROM blade_menu WHERE id=2091009000000000012);
INSERT INTO blade_menu (id,parent_id,code,name,alias,path,source,sort,category,action,is_open,component,is_deleted)
SELECT 2091009000000000013,2091009000000000002,'duty_rollback','回滚','rollback','/api/blade-attendance/duty/rollback','',4,2,1,1,'',0
WHERE NOT EXISTS (SELECT 1 FROM blade_menu WHERE id=2091009000000000013);
INSERT INTO blade_menu (id,parent_id,code,name,alias,path,source,sort,category,action,is_open,component,is_deleted)
SELECT 2091009000000000014,2091009000000000002,'duty_retry','门禁重试','retry','/api/blade-attendance/duty/retry','',5,2,1,1,'',0
WHERE NOT EXISTS (SELECT 1 FROM blade_menu WHERE id=2091009000000000014);
INSERT INTO blade_menu (id,parent_id,code,name,alias,path,source,sort,category,action,is_open,component,is_deleted)
SELECT 2091009000000000015,2091009000000000002,'duty_employee-bind','人员映射','employee-bind','/api/blade-attendance/duty/employee-bind','',6,2,1,1,'',0
WHERE NOT EXISTS (SELECT 1 FROM blade_menu WHERE id=2091009000000000015);
INSERT INTO blade_menu (id,parent_id,code,name,alias,path,source,sort,category,action,is_open,component,is_deleted)
SELECT 2091009000000000016,2091009000000000002,'duty_export','导出','export','/api/blade-attendance/duty/export','',7,2,1,1,'',0
WHERE NOT EXISTS (SELECT 1 FROM blade_menu WHERE id=2091009000000000016);

