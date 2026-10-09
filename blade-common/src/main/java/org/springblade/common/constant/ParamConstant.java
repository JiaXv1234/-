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
package org.springblade.common.constant;

import java.util.Arrays;
import java.util.List;

/**
 * 系统参数常量
 * 统一管理所有通过 ParamCache 读取的参数key及其默认值
 *
 * @author BladeX
 */
public interface ParamConstant {

	// ==================== 账号相关 ====================

	/**
	 * 是否开启注册参数key
	 */
	String REGISTER_USER_VALUE = "account.registerUser";
	/**
	 * 账号默认密码参数key
	 */
	String DEFAULT_PARAM_PASSWORD = "account.initPassword";
	/**
	 * 账号默认密码
	 */
	String DEFAULT_PASSWORD = "123456";
	/**
	 * 账号锁定错误次数参数key
	 */
	String FAIL_COUNT_VALUE = "account.failCount";
	/**
	 * 账号锁定默认错误次数
	 */
	Integer FAIL_COUNT = 5;
	/**
	 * 账号锁定时长参数key（单位：分钟）
	 */
	String LOCK_DURATION_VALUE = "account.lockDuration";
	/**
	 * 账号锁定默认时长（单位：分钟）
	 */
	Integer LOCK_DURATION = 30;

	// ==================== 租户相关 ====================

	/**
	 * 租户默认密码参数key
	 */
	String TENANT_PASSWORD_KEY = "tenant.default.password";
	/**
	 * 租户默认密码
	 */
	String TENANT_DEFAULT_PASSWORD = "123456";
	/**
	 * 租户默认账号额度参数key
	 */
	String TENANT_ACCOUNT_NUMBER_KEY = "tenant.default.accountNumber";
	/**
	 * 租户默认账号额度
	 */
	Integer TENANT_DEFAULT_ACCOUNT_NUMBER = -1;
	/**
	 * 租户默认菜单集合参数key
	 */
	String TENANT_MENU_CODE_KEY = "tenant.default.menuCode";
	/**
	 * 租户默认菜单集合
	 */
	List<String> TENANT_MENU_CODES = Arrays.asList(
		"desk", "flow", "work", "monitor", "resource", "role", "user", "dept", "dictbiz", "topmenu"
	);

}
