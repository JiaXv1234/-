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

import org.springblade.core.launch.constant.AppConstant;
import org.springblade.core.tool.api.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 认证锁定 Feign接口类
 *
 * @author BladeX
 */
@FeignClient(
	value = AppConstant.APPLICATION_SYSTEM_NAME,
	fallback = IAuthLockClientFallback.class
)
public interface IAuthLockClient {

	String API_PREFIX = "/feign/client/auth-lock";
	String IS_ACCOUNT_LOCKED = API_PREFIX + "/is-account-locked";
	String IS_IP_LOCKED = API_PREFIX + "/is-ip-locked";
	String ADD_ACCOUNT_FAIL_COUNT = API_PREFIX + "/add-account-fail-count";
	String ADD_IP_FAIL_COUNT = API_PREFIX + "/add-ip-fail-count";
	String RELEASE_SYSTEM_LOCK = API_PREFIX + "/release-system-lock";
	String RELEASE_SYSTEM_IP_LOCK = API_PREFIX + "/release-system-ip-lock";

	/**
	 * 检查账号是否锁定
	 *
	 * @param tenantId 租户ID
	 * @param account  账号
	 * @return boolean
	 */
	@GetMapping(IS_ACCOUNT_LOCKED)
	R<Boolean> isAccountLocked(@RequestParam("tenantId") String tenantId, @RequestParam("account") String account);

	/**
	 * 检查IP是否锁定
	 *
	 * @param tenantId 租户ID
	 * @param ip       IP地址
	 * @return boolean
	 */
	@GetMapping(IS_IP_LOCKED)
	R<Boolean> isIpLocked(@RequestParam("tenantId") String tenantId, @RequestParam("ip") String ip);

	/**
	 * 增加账号认证失败次数
	 *
	 * @param tenantId  租户ID
	 * @param account   账号
	 * @param userId    用户ID
	 * @param ip        IP地址
	 * @param userAgent 用户代理
	 * @return void
	 */
	@PostMapping(ADD_ACCOUNT_FAIL_COUNT)
	R<Void> addAccountFailCount(@RequestParam("tenantId") String tenantId, @RequestParam("account") String account,
								@RequestParam(value = "userId", required = false) Long userId,
								@RequestParam("ip") String ip, @RequestParam(value = "userAgent", required = false) String userAgent);

	/**
	 * 增加IP认证失败次数
	 *
	 * @param tenantId  租户ID
	 * @param ip        IP地址
	 * @param userAgent 用户代理
	 * @return void
	 */
	@PostMapping(ADD_IP_FAIL_COUNT)
	R<Void> addIpFailCount(@RequestParam("tenantId") String tenantId, @RequestParam("ip") String ip,
						   @RequestParam(value = "userAgent", required = false) String userAgent);

	/**
	 * 释放账号的系统自动锁定
	 *
	 * @param tenantId 租户ID
	 * @param account  账号
	 * @return void
	 */
	@PostMapping(RELEASE_SYSTEM_LOCK)
	R<Void> releaseSystemLock(@RequestParam("tenantId") String tenantId, @RequestParam("account") String account);

	/**
	 * 释放IP的系统自动锁定
	 *
	 * @param tenantId 租户ID
	 * @param ip       IP地址
	 * @return void
	 */
	@PostMapping(RELEASE_SYSTEM_IP_LOCK)
	R<Void> releaseSystemIpLock(@RequestParam("tenantId") String tenantId, @RequestParam("ip") String ip);

}
