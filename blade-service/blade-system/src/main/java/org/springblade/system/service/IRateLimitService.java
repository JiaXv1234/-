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

import org.springblade.core.mp.base.BaseService;
import org.springblade.system.pojo.entity.RateLimit;

/**
 * 限流规则 服务类
 *
 * @author Chill
 */
public interface IRateLimitService extends BaseService<RateLimit> {

	/**
	 * 新增或修改 限流规则
	 *
	 * @param rateLimit 实体
	 * @return 是否成功
	 */
	boolean submitRateLimit(RateLimit rateLimit);

	/**
	 * 删除 限流规则
	 *
	 * @param ids 主键集合 (逗号分隔)
	 * @return 是否成功
	 */
	boolean removeRateLimit(String ids);

	/**
	 * 启用 限流规则
	 *
	 * @param ids 主键集合 (逗号分隔)
	 * @return 是否成功
	 */
	boolean enableRateLimit(String ids);

	/**
	 * 禁用 限流规则
	 *
	 * @param ids 主键集合 (逗号分隔)
	 * @return 是否成功
	 */
	boolean disableRateLimit(String ids);

}
