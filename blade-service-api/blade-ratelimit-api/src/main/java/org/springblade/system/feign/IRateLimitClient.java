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
import org.springblade.core.ratelimit.RateLimitRule;
import org.springblade.system.pojo.dto.RateLimitLogDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 限流规则 Feign接口类
 *
 * @author Chill
 */
@FeignClient(
	value = AppConstant.APPLICATION_SYSTEM_NAME,
	fallback = IRateLimitClientFallback.class
)
public interface IRateLimitClient {

	String API_PREFIX = "/feign/client/rate-limit";
	String LIST = API_PREFIX + "/list";
	String SAVE_LOG = API_PREFIX + "/save-log";

	/**
	 * 获取全部启用的限流规则
	 *
	 * @return 限流规则集合
	 */
	@GetMapping(LIST)
	List<RateLimitRule> listEnabledRules();

	/**
	 * 保存限流触发日志
	 *
	 * @param rateLimitLog 限流触发日志
	 * @return 影响行数
	 */
	@PostMapping(SAVE_LOG)
	int saveLog(@RequestBody RateLimitLogDTO rateLimitLog);

}
