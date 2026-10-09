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

import lombok.AllArgsConstructor;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.system.service.IAuthLockService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Hidden;

/**
 * 认证锁定 Feign实现类
 *
 * @author BladeX
 */
@NonDS
@Hidden
@RestController
@AllArgsConstructor
public class AuthLockClient implements IAuthLockClient {

	private final IAuthLockService authLockService;

	@Override
	@GetMapping(IS_ACCOUNT_LOCKED)
	public R<Boolean> isAccountLocked(String tenantId, String account) {
		return R.data(authLockService.isAccountLocked(tenantId, account));
	}

	@Override
	@GetMapping(IS_IP_LOCKED)
	public R<Boolean> isIpLocked(String tenantId, String ip) {
		return R.data(authLockService.isIpLocked(tenantId, ip));
	}

	@Override
	@PostMapping(ADD_ACCOUNT_FAIL_COUNT)
	public R<Void> addAccountFailCount(String tenantId, String account, Long userId, String ip, String userAgent) {
		authLockService.addAccountFailCount(tenantId, account, userId, ip, userAgent);
		return R.success("success");
	}

	@Override
	@PostMapping(ADD_IP_FAIL_COUNT)
	public R<Void> addIpFailCount(String tenantId, String ip, String userAgent) {
		authLockService.addIpFailCount(tenantId, ip, userAgent);
		return R.success("success");
	}

	@Override
	@PostMapping(RELEASE_SYSTEM_LOCK)
	public R<Void> releaseSystemLock(String tenantId, String account) {
		authLockService.releaseSystemLock(tenantId, account);
		return R.success("success");
	}

	@Override
	@PostMapping(RELEASE_SYSTEM_IP_LOCK)
	public R<Void> releaseSystemIpLock(String tenantId, String ip) {
		authLockService.releaseSystemIpLock(tenantId, ip);
		return R.success("success");
	}

}
