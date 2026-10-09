/**
 * BladeX Commercial License Agreement
 * Copyright (c) 2018-2099, https://bladex.cn. All rights reserved.
 * <p>
 * Use of this software is governed by the Commercial License Agreement
 * obtained after purchasing a license from BladeX.
 * <p>
 * 1. This software is for development use only under a valid license
 * from BladeX.
 * <p>
 * 2. Redistribution of this software's source code to any third party
 * without a commercial license is strictly prohibited.
 * <p>
 * 3. Licensees may copyright their own code but cannot use segments
 * from this software for such purposes. Copyright of this software
 * remains with BladeX.
 * <p>
 * Using this software signifies agreement to this License, and the software
 * must not be used for illegal purposes.
 * <p>
 * THIS SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY. The author is
 * not liable for any claims arising from secondary or illegal development.
 * <p>
 * Author: Chill Zhuang (bladejava@qq.com)
 */
package org.springblade.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springblade.system.pojo.entity.Role;
import org.springblade.system.pojo.vo.RoleVO;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.Map;

/**
 * 服务类
 *
 * @author Chill
 */
public interface IRoleService extends IService<Role> {

	/**
	 * 详情查询（含超管判定 + 当前租户隔离）
	 *
	 * @param role 查询条件
	 * @return 单条记录，非超管时仅命中当前会话租户
	 */
	Role getDetail(Role role);

	/**
	 * 列表（含超管判定 + 当前租户隔离）
	 *
	 * @param role 查询条件 Map
	 * @return 列表 VO，非超管时仅命中当前会话租户
	 */
	List<RoleVO> selectList(Map<String, Object> role);

	/**
	 * 自定义角色分页查询
	 *
	 * @param page 分页参数
	 * @param role 查询条件
	 * @return 角色分页结果
	 */
	IPage<RoleVO> selectRolePage(IPage<RoleVO> page, RoleVO role);

	/**
	 * 树形结构（含超管判定 + 当前租户隔离）
	 * <p>
	 * 仅超级管理员可指定任意 tenantId 查询；其他用户传入的 tenantId 一律被忽略，
	 * 强制使用当前会话租户。
	 *
	 * @param tenantId 入参 tenantId（仅超管生效）
	 * @return 角色树
	 */
	List<RoleVO> tree(String tenantId);

	/**
	 * 通过 roleId 获取对应租户的角色树（含超管路由判定）
	 * <p>
	 * 仅超级管理员可通过任意 roleId 跳转到对应租户的角色树（用于跨租户管理）；
	 * 其他用户忽略 roleId 参数，强制返回自身租户的角色树。
	 *
	 * @param roleId 角色 ID（仅超管生效，用于反推 tenantId）
	 * @return 角色树
	 */
	List<RoleVO> treeById(Long roleId);

	/**
	 * 权限配置
	 *
	 * @param roleIds      角色id集合
	 * @param menuIds      菜单id集合
	 * @param dataScopeIds 数据权限id集合
	 * @param apiScopeIds  接口权限id集合
	 * @return 是否成功
	 */
	boolean grant(@NotEmpty List<Long> roleIds, List<Long> menuIds, List<Long> dataScopeIds, List<Long> apiScopeIds);

	/**
	 * 获取角色ID
	 *
	 * @param tenantId  租户id
	 * @param roleNames 角色名
	 * @return 角色id
	 */
	String getRoleIds(String tenantId, String roleNames);

	/**
	 * 根据角色ID集合获取角色名称
	 *
	 * @param roleIds 角色id集合（逗号分隔）
	 * @return 角色名称列表
	 */
	List<String> getRoleNames(String roleIds);

	/**
	 * 根据角色ID集合获取角色别名
	 *
	 * @param roleIds 角色id集合（逗号分隔）
	 * @return 角色别名列表
	 */
	List<String> getRoleAliases(String roleIds);

	/**
	 * 新增或修改角色
	 * <p>
	 * 顶级角色绑定当前会话租户；子角色继承父级租户，禁止跨租户迁移及越权创建超管角色。
	 *
	 * @param role 角色实体
	 * @return 是否成功
	 */
	boolean submit(Role role);

	/**
	 * 角色信息查询
	 *
	 * @param roleName 角色名
	 * @param parentId 父级id
	 * @return 角色列表
	 */
	List<RoleVO> search(String roleName, Long parentId);

	/**
	 * 删除角色（批量租户守卫校验，存在子节点时拒绝删除）
	 *
	 * @param ids 角色主键集合（逗号分隔）
	 * @return 是否成功
	 */
	boolean removeRole(String ids);

	/**
	 * 获取角色别名列表
	 *
	 * @param tenantId 租户id
	 * @return 别名列表
	 */
	List<Role> alias(String tenantId);

}
