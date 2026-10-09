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
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.provider.ApiKeyInfo;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.constant.RoleConstant;
import org.springblade.core.tool.jackson.JsonUtil;
import org.springblade.core.tool.support.Kv;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.StringPool;
import org.springblade.system.pojo.dto.ApiKeyLogDTO;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springblade.core.secure.constant.ApiKeyConstant.*;

/**
 * API Key Feign实现类
 *
 * @author Chill
 */
@NonDS
@Hidden
@RestController
@RequiredArgsConstructor
public class ApiKeyClient implements IApiKeyClient {

	private final JdbcTemplate jdbcTemplate;

	@Override
	@GetMapping(GET_USER)
	public BladeUser getUser(String apiKey) {
		// 第一步：查询 API Key 基本信息
		ApiKeyInfo apiKeyInfo = queryApiKeyInfo(apiKey);
		if (apiKeyInfo == null) {
			return null;
		}
		// 检查状态
		if (apiKeyInfo.getStatus() == null || apiKeyInfo.getStatus() != STATUS_ACTIVE) {
			return null;
		}
		// 检查过期时间
		Date expireTime = apiKeyInfo.getExpireTime();
		if (expireTime != null && expireTime.before(new Date())) {
			return null;
		}
		// 检查用户ID
		if (apiKeyInfo.getUserId() == null) {
			return null;
		}
		// 第二步：查询关联用户信息
		populateUserInfo(apiKeyInfo);
		if (Func.isBlank(apiKeyInfo.getAccount())) {
			return null;
		}
		// 第三步：解析角色名称（含超管安全判定）
		String roleName = resolveRoleName(apiKeyInfo.getRoleId());
		// 构建 BladeUser
		BladeUser bladeUser = new BladeUser();
		bladeUser.setUserId(apiKeyInfo.getUserId());
		bladeUser.setTenantId(apiKeyInfo.getTenantId());
		bladeUser.setAccount(apiKeyInfo.getAccount());
		bladeUser.setUserName(apiKeyInfo.getName());
		bladeUser.setNickName(apiKeyInfo.getRealName());
		bladeUser.setClientId(apiKey);
		bladeUser.setDeptId(Func.toStrWithEmpty(apiKeyInfo.getDeptId(), StringPool.MINUS_ONE));
		bladeUser.setPostId(Func.toStrWithEmpty(apiKeyInfo.getPostId(), StringPool.MINUS_ONE));
		bladeUser.setRoleId(Func.toStrWithEmpty(apiKeyInfo.getRoleId(), StringPool.MINUS_ONE));
		bladeUser.setRoleName(roleName);
		// 解析扩展参数
		String extParams = apiKeyInfo.getExtParams();
		Kv detail = Func.isNotBlank(extParams) ? JsonUtil.parse(extParams, Kv.class) : null;
		if (detail == null) {
			detail = Kv.create();
		}
		detail.set(DETAIL_API_KEY_ID, apiKeyInfo.getId());
		bladeUser.setDetail(detail);
		return bladeUser;
	}

	@Override
	@GetMapping(GET_API_PATH)
	public String getApiPath(String apiKey) {
		ApiKeyInfo apiKeyInfo = queryApiKeyInfo(apiKey);
		return (apiKeyInfo != null) ? apiKeyInfo.getApiPath() : null;
	}

	@Override
	@PostMapping(SAVE_LOG)
	public int saveLog(@RequestBody ApiKeyLogDTO apiKeyLog) {
		return jdbcTemplate.update(API_KEY_LOG_INSERT,
			apiKeyLog.getId(), apiKeyLog.getTenantId(),
			apiKeyLog.getServiceId(), apiKeyLog.getServerIp(), apiKeyLog.getServerHost(), apiKeyLog.getEnv(),
			apiKeyLog.getApiKeyId(), apiKeyLog.getRequestUri(), apiKeyLog.getMethod(), apiKeyLog.getRemoteIp(),
			apiKeyLog.getUserAgent(), apiKeyLog.getParams(), Func.toStr(apiKeyLog.getTime()),
			apiKeyLog.getCreateBy(), apiKeyLog.getCreateTime());
	}

	/**
	 * 第一步：查询 API Key 基本信息
	 *
	 * @param apiKey API Key
	 * @return ApiKeyInfo（仅含 Key 表字段）
	 */
	private ApiKeyInfo queryApiKeyInfo(String apiKey) {
		List<ApiKeyInfo> results = jdbcTemplate.query(API_KEY_SELECT_STATEMENT, new BeanPropertyRowMapper<>(ApiKeyInfo.class), apiKey);
		if (results.isEmpty()) {
			return null;
		}
		return results.get(0);
	}

	/**
	 * 第二步：查询关联用户信息并填充至 ApiKeyInfo
	 *
	 * @param apiKeyInfo API Key 信息（需含 userId）
	 */
	private void populateUserInfo(ApiKeyInfo apiKeyInfo) {
		List<ApiKeyInfo> results = jdbcTemplate.query(
			API_KEY_USER_SELECT_STATEMENT, new BeanPropertyRowMapper<>(ApiKeyInfo.class), apiKeyInfo.getUserId()
		);
		if (results.isEmpty()) {
			return;
		}
		ApiKeyInfo userInfo = results.get(0);
		apiKeyInfo.setAccount(userInfo.getAccount());
		apiKeyInfo.setName(userInfo.getName());
		apiKeyInfo.setRealName(userInfo.getRealName());
		apiKeyInfo.setDeptId(userInfo.getDeptId());
		apiKeyInfo.setPostId(userInfo.getPostId());
		apiKeyInfo.setRoleId(userInfo.getRoleId());
	}

	/**
	 * 第三步：解析角色别名，若用户为管理员则返回空字符串
	 *
	 * @param roleId 角色ID（逗号分隔）
	 * @return 角色别名（逗号分隔），管理员返回空字符串
	 */
	private String resolveRoleName(String roleId) {
		if (Func.isBlank(roleId) || StringPool.MINUS_ONE.equals(roleId)) {
			return StringPool.EMPTY;
		}
		Long[] roleIds = Func.toLongArray(roleId);
		if (roleIds.length == 0) {
			return StringPool.EMPTY;
		}
		// 构建参数化 IN 子句
		String placeholders = String.join(StringPool.COMMA, Collections.nCopies(roleIds.length, "?"));
		String sql = String.format(API_KEY_ROLE_SELECT_STATEMENT, placeholders);
		List<Map<String, Object>> roles = jdbcTemplate.queryForList(sql, (Object[]) roleIds);
		if (roles.isEmpty()) {
			return StringPool.EMPTY;
		}
		// 为了系统安全性考虑，管理员角色不返回角色信息，保持低权限角色调用系统给第三方的接口
		boolean isAdmin = roles.stream()
			.anyMatch(role -> {
				String roleAlias = Func.toStr(role.get(ROLE_ALIAS_COLUMN));
				return RoleConstant.ADMINISTRATOR.equals(roleAlias) || RoleConstant.ADMIN.equals(roleAlias);
			});
		if (isAdmin) {
			return StringPool.EMPTY;
		}
		// 拼接角色名称
		return roles.stream()
			.map(role -> Func.toStr(role.get(ROLE_ALIAS_COLUMN)))
			.filter(Func::isNotBlank)
			.collect(Collectors.joining(StringPool.COMMA));
	}

}
