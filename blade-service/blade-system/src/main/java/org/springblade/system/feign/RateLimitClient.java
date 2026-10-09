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
import org.springblade.core.ratelimit.RateLimitRule;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.system.pojo.dto.RateLimitLogDTO;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springblade.core.ratelimit.RateLimitConstant.RATE_LIMIT_LOG_INSERT;
import static org.springblade.core.ratelimit.RateLimitConstant.RULE_SELECT_STATEMENT;

/**
 * 限流规则 Feign实现类
 *
 * @author Chill
 */
@NonDS
@Hidden
@RestController
@RequiredArgsConstructor
public class RateLimitClient implements IRateLimitClient {

	private final JdbcTemplate jdbcTemplate;

	@Override
	@GetMapping(LIST)
	public List<RateLimitRule> listEnabledRules() {
		return jdbcTemplate.query(RULE_SELECT_STATEMENT, new BeanPropertyRowMapper<>(RateLimitRule.class));
	}

	@Override
	@PostMapping(SAVE_LOG)
	public int saveLog(@RequestBody RateLimitLogDTO rateLimitLog) {
		return jdbcTemplate.update(RATE_LIMIT_LOG_INSERT,
			rateLimitLog.getId(), rateLimitLog.getTenantId(),
			rateLimitLog.getServiceId(), rateLimitLog.getServerIp(), rateLimitLog.getServerHost(), rateLimitLog.getEnv(),
			rateLimitLog.getLimitId(), rateLimitLog.getLimitName(), rateLimitLog.getLimitDimension(),
			rateLimitLog.getLimitCount(), rateLimitLog.getLimitPeriod(), rateLimitLog.getTimeUnit(), rateLimitLog.getLimitKey(),
			rateLimitLog.getRequestUri(), rateLimitLog.getMethod(), rateLimitLog.getRemoteIp(), rateLimitLog.getUserAgent(), rateLimitLog.getParams(),
			rateLimitLog.getCreateBy(), rateLimitLog.getCreateTime());
	}

}
