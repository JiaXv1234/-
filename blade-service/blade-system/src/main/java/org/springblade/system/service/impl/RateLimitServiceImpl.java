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
package org.springblade.system.service.impl;

import lombok.AllArgsConstructor;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.mp.enums.StatusType;
import org.springblade.core.ratelimit.RateLimitRuleManager;
import org.springblade.core.tool.utils.Func;
import org.springblade.system.mapper.RateLimitMapper;
import org.springblade.system.pojo.entity.RateLimit;
import org.springblade.system.service.IRateLimitService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 限流规则 服务实现类
 *
 * @author Chill
 */
@Service
@AllArgsConstructor
public class RateLimitServiceImpl extends BaseServiceImpl<RateLimitMapper, RateLimit> implements IRateLimitService {

	private final RateLimitRuleManager rateLimitRuleManager;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean submitRateLimit(RateLimit rateLimit) {
		boolean result = this.saveOrUpdate(rateLimit);
		if (result) {
			rateLimitRuleManager.refresh();
		}
		return result;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean removeRateLimit(String ids) {
		boolean result = this.removeByIds(Func.toLongList(ids));
		if (result) {
			rateLimitRuleManager.refresh();
		}
		return result;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean enableRateLimit(String ids) {
		boolean result = this.changeStatus(Func.toLongList(ids), StatusType.ACTIVE.getType());
		if (result) {
			rateLimitRuleManager.refresh();
		}
		return result;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean disableRateLimit(String ids) {
		boolean result = this.changeStatus(Func.toLongList(ids), StatusType.DISABLED.getType());
		if (result) {
			rateLimitRuleManager.refresh();
		}
		return result;
	}

}
