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
package org.springblade.system.feign;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springblade.core.cache.utils.CacheUtil;
import org.springblade.core.secure.provider.PermissionMenu;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.StringPool;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

import static org.springblade.core.cache.constant.CacheConstant.SYS_CACHE;
import static org.springblade.core.secure.constant.PermissionConstant.*;

/**
 * 接口权限Feign实现类
 *
 * @author Chill
 */
@NonDS
@Hidden
@RestController
@RequiredArgsConstructor
public class ApiScopeClient implements IApiScopeClient {

	private static final String SCOPE_CACHE_ALL_MENU = "apiScope:allMenu:permission";

	private final JdbcTemplate jdbcTemplate;

	@Override
	@GetMapping(PERMISSION_PATH)
	public List<String> permissionPath(String roleId) {
		List<Long> roleIds = Func.toLongList(roleId);
		return jdbcTemplate.queryForList(permissionAllStatement(roleIds.size()), String.class, roleIds.toArray());
	}

	@Override
	@GetMapping(PERMISSION_CODE)
	public List<String> permissionCode(String permission, String roleId) {
		List<Object> args = new ArrayList<>(Collections.singletonList(permission));
		List<Long> roleIds = Func.toLongList(roleId);
		args.addAll(roleIds);
		return jdbcTemplate.queryForList(permissionCodeStatement(roleIds.size()), String.class, args.toArray());
	}

	@Override
	@GetMapping(PERMISSION_MENU)
	public List<String> permissionMenu(String permission, String roleId) {
		// 获取所有菜单
		List<PermissionMenu> allMenus = permissionAllMenu();
		// 获取角色菜单
		List<Long> roleIds = Func.toLongList(roleId);
		List<PermissionMenu> roleIdMenus = jdbcTemplate.query(permissionMenuStatement(roleIds.size()), new BeanPropertyRowMapper<>(PermissionMenu.class), roleIds.toArray());
		// 反向递归角色菜单所有父级
		List<PermissionMenu> routes = new LinkedList<>(roleIdMenus);
		roleIdMenus.forEach(roleMenu -> recursion(allMenus, routes, roleMenu));
		// 支持逗号分隔的多个权限匹配
		Set<String> permissionSet = Arrays.stream(permission.split(StringPool.COMMA))
			.map(String::trim)
			.collect(Collectors.toSet());
		// 获取匹配的菜单权限值
		return routes.stream()
			.map(PermissionMenu::getCode)
			.filter(permissionSet::contains)
			.collect(Collectors.toList());
	}

	/**
	 * 获取所有菜单权限信息
	 */
	private List<PermissionMenu> permissionAllMenu() {
		List<PermissionMenu> permissions = CacheUtil.get(SYS_CACHE, SCOPE_CACHE_ALL_MENU, StringPool.EMPTY, List.class, Boolean.FALSE);
		if (permissions == null) {
			permissions = jdbcTemplate.query(permissionAllMenuStatement(), new BeanPropertyRowMapper<>(PermissionMenu.class));
			CacheUtil.put(SYS_CACHE, SCOPE_CACHE_ALL_MENU, StringPool.EMPTY, permissions, Boolean.FALSE);
		}
		return permissions;
	}

	/**
	 * 递归获取菜单父级
	 *
	 * @param allMenus 所有菜单合集
	 * @param routes   角色分配的菜单合集
	 * @param roleMenu 当前菜单
	 */
	private void recursion(List<PermissionMenu> allMenus, List<PermissionMenu> routes, PermissionMenu roleMenu) {
		Optional<PermissionMenu> menu = allMenus.stream().filter(x -> Func.equals(x.getId(), roleMenu.getParentId())).findFirst();
		if (menu.isPresent() && !routes.contains(menu.get())) {
			routes.add(menu.get());
			recursion(allMenus, routes, menu.get());
		}
	}

}
