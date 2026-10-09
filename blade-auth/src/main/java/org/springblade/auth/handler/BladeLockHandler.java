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
package org.springblade.auth.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.oauth2.exception.ExceptionCode;
import org.springblade.core.oauth2.provider.OAuth2Validation;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.WebUtil;
import org.springblade.system.feign.IAuthLockClient;

import java.util.concurrent.CompletableFuture;


/**
 * 失败锁定处理器
 * 统一管理账号锁定和IP锁定逻辑
 *
 * @author BladeX
 */
@Slf4j
@RequiredArgsConstructor
public class BladeLockHandler {

	private final IAuthLockClient authLockClient;

	/**
	 * 校验账号是否锁定
	 *
	 * @param tenantId 租户id
	 * @param account  账号
	 * @return OAuth2Validation
	 */
	public OAuth2Validation validateAccountLock(String tenantId, String account) {
		try {
			R<Boolean> result = authLockClient.isAccountLocked(tenantId, account);
			if (result.isSuccess() && Boolean.TRUE.equals(result.getData())) {
				log.error("用户：{}，已锁定，请求ip：{}", account, WebUtil.getIP());
				return buildValidationFailure();
			}
		} catch (Exception e) {
			log.warn("账号锁定校验异常: {}", e.getMessage());
		}
		return new OAuth2Validation();
	}

	/**
	 * 校验IP是否锁定
	 *
	 * @param tenantId 租户id
	 * @return OAuth2Validation
	 */
	public OAuth2Validation validateIpLock(String tenantId) {
		String clientIp = WebUtil.getIP();
		try {
			R<Boolean> result = authLockClient.isIpLocked(tenantId, clientIp);
			if (result.isSuccess() && Boolean.TRUE.equals(result.getData())) {
				log.error("IP：{}，已锁定", clientIp);
				return buildValidationFailure();
			}
		} catch (Exception e) {
			log.warn("IP锁定校验异常: {}", e.getMessage());
		}
		return new OAuth2Validation();
	}

	/**
	 * 处理认证失败
	 * 同时增加账号和IP错误次数
	 *
	 * @param tenantId 租户id
	 * @param account  账号
	 * @param userId   用户ID
	 */
	public void handleAuthFailure(String tenantId, String account, Long userId) {
		String ip = WebUtil.getIP();
		String userAgent = WebUtil.getUserAgent();
		CompletableFuture.runAsync(() -> {
			try {
				authLockClient.addAccountFailCount(tenantId, account, userId, ip, userAgent);
				authLockClient.addIpFailCount(tenantId, ip, userAgent);
			} catch (Exception e) {
				log.warn("认证失败计数异常: {}", e.getMessage());
			}
		});
	}

	/**
	 * 处理认证失败
	 * 同时增加账号和IP错误次数
	 *
	 * @param tenantId 租户id
	 * @param account  账号
	 */
	public void handleAuthFailure(String tenantId, String account) {
		handleAuthFailure(tenantId, account, null);
	}

	/**
	 * 处理认证成功
	 * 释放系统自动锁定
	 *
	 * @param tenantId 租户id
	 * @param account  账号
	 */
	public void handleAuthSuccess(String tenantId, String account) {
		String ip = WebUtil.getIP();
		CompletableFuture.runAsync(() -> {
			try {
				authLockClient.releaseSystemLock(tenantId, account);
				authLockClient.releaseSystemIpLock(tenantId, ip);
			} catch (Exception e) {
				log.warn("认证成功释放锁定异常: {}", e.getMessage());
			}
		});
	}

	/**
	 * 构建校验失败结果
	 *
	 * @return OAuth2Validation
	 */
	private OAuth2Validation buildValidationFailure() {
		OAuth2Validation validation = new OAuth2Validation();
		validation.setSuccess(false);
		validation.setCode(ExceptionCode.USER_TOO_MANY_FAILS.getCode());
		validation.setMessage(ExceptionCode.USER_TOO_MANY_FAILS.getMessage());
		return validation;
	}

}
