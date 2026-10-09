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
package org.springblade.system.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.cache.utils.CacheUtil;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.annotation.IsAdmin;
import org.springblade.core.secure.annotation.PreAuth;
import org.springblade.core.secure.constant.AuthConstant;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.system.cache.UserCache;
import org.springblade.system.pojo.entity.Role;
import org.springblade.system.pojo.entity.User;
import org.springblade.system.pojo.vo.GrantVO;
import org.springblade.system.pojo.vo.RoleVO;
import org.springblade.system.service.IRoleService;
import org.springblade.system.wrapper.RoleWrapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static org.springblade.core.cache.constant.CacheConstant.SYS_CACHE;

/**
 * 控制器
 *
 * @author Chill
 */
@NonDS
@RestController
@AllArgsConstructor
@IsAdmin
@RequestMapping("/role")
@Tag(name = "角色", description = "角色")
public class RoleController extends BladeController {

	private final IRoleService roleService;

	/**
	 * 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description = "传入role")
	public R<RoleVO> detail(Role role) {
		return R.data(RoleWrapper.build().entityVO(roleService.getDetail(role)));
	}

	/**
	 * 列表
	 */
	@GetMapping("/list")
	@Parameters({
		@Parameter(name = "roleName", description = "参数名称", in = ParameterIn.QUERY, schema = @Schema(type = "string")),
		@Parameter(name = "roleAlias", description = "角色别名", in = ParameterIn.QUERY, schema = @Schema(type = "string"))
	})
	@ApiOperationSupport(order = 2)
	@Operation(summary = "列表", description = "传入role")
	public R<List<RoleVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> role) {
		return R.data(roleService.selectList(role));
	}

	/**
	 * 获取角色树形结构
	 * <p>
	 * 仅超级管理员可指定任意 tenantId 查询；其他用户强制使用自身 tenantId，
	 * 防止通过 tenantId 参数越权读取其他租户的角色树。
	 */
	@GetMapping("/tree")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "树形结构", description = "树形结构")
	public R<List<RoleVO>> tree(String tenantId) {
		return R.data(roleService.tree(tenantId));
	}

	/**
	 * 获取指定角色树形结构
	 * <p>
	 * 仅超级管理员可通过任意 roleId 跳转到对应租户的角色树（用于跨租户管理）；
	 * 其他用户忽略 roleId 参数，强制返回自身租户的角色树，
	 * 防止通过 roleId 越权读取其他租户的角色结构。
	 */
	@GetMapping("/tree-by-id")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "树形结构", description = "树形结构")
	public R<List<RoleVO>> treeById(Long roleId) {
		return R.data(roleService.treeById(roleId));
	}

	/**
	 * 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "新增或修改", description = "传入role")
	public R submit(@Valid @RequestBody Role role) {
		CacheUtil.clear(SYS_CACHE);
		CacheUtil.clear(SYS_CACHE, Boolean.FALSE);
		return R.status(roleService.submit(role));
	}


	/**
	 * 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "删除", description = "传入ids")
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		CacheUtil.clear(SYS_CACHE);
		CacheUtil.clear(SYS_CACHE, Boolean.FALSE);
		return R.status(roleService.removeRole(ids));
	}

	/**
	 * 设置角色权限
	 */
	@PostMapping("/grant")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "权限设置", description = "传入roleId集合以及menuId集合")
	public R grant(@RequestBody GrantVO grantVO) {
		CacheUtil.clear(SYS_CACHE);
		CacheUtil.clear(SYS_CACHE, Boolean.FALSE);
		boolean temp = roleService.grant(grantVO.getRoleIds(), grantVO.getMenuIds(), grantVO.getDataScopeIds(), grantVO.getApiScopeIds());
		return R.status(temp);
	}

	/**
	 * 下拉数据源
	 */
	@PreAuth(AuthConstant.PERMIT_ALL)
	@GetMapping("/select")
	@ApiOperationSupport(order = 8)
	@Operation(summary = "下拉数据源", description = "传入id集合")
	public R<List<Role>> select(Long userId, String roleId) {
		if (Func.isNotEmpty(userId)) {
			User user = UserCache.getUser(userId);
			roleId = user.getRoleId();
		}
		List<Role> list = roleService.list(Wrappers.<Role>lambdaQuery().in(Role::getId, Func.toLongList(roleId)));
		return R.data(list);
	}

	/**
	 * 获取现有角色别名列表
	 */
	@GetMapping("/alias")
	@ApiOperationSupport(order = 9)
	@Operation(summary = "获取角色别名", description = "获取角色别名")
	public R<List<Role>> alias() {
		return R.data(roleService.alias(AuthUtil.getTenantId()));
	}

}
