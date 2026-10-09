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

import org.springblade.core.tool.api.R;
import org.springframework.stereotype.Component;

/**
 * 认证锁定 Feign失败配置
 *
 * @author BladeX
 */
@Component
public class IAuthLockClientFallback implements IAuthLockClient {

	@Override
	public R<Boolean> isAccountLocked(String tenantId, String account) {
		return R.fail("获取数据失败");
	}

	@Override
	public R<Boolean> isIpLocked(String tenantId, String ip) {
		return R.fail("获取数据失败");
	}

	@Override
	public R<Void> addAccountFailCount(String tenantId, String account, Long userId, String ip, String userAgent) {
		return R.fail("获取数据失败");
	}

	@Override
	public R<Void> addIpFailCount(String tenantId, String ip, String userAgent) {
		return R.fail("获取数据失败");
	}

	@Override
	public R<Void> releaseSystemLock(String tenantId, String account) {
		return R.fail("获取数据失败");
	}

	@Override
	public R<Void> releaseSystemIpLock(String tenantId, String ip) {
		return R.fail("获取数据失败");
	}

}
