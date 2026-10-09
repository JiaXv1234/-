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
package org.springblade.system.config;

import org.springblade.core.launch.props.BladeProperties;
import org.springblade.core.launch.server.ServerInfo;
import org.springblade.core.ratelimit.IRateLimitHandler;
import org.springblade.core.ratelimit.IRateLimitLogHandler;
import org.springblade.core.ratelimit.config.RateLimitAutoConfiguration;
import org.springblade.system.feign.IRateLimitClient;
import org.springblade.system.handler.RateLimitHandler;
import org.springblade.system.handler.RateLimitLogHandler;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 限流微服务配置。
 * <p>
 * 覆盖 starter {@link RateLimitAutoConfiguration} 的本地默认实现：规则取数 ({@link RateLimitHandler}) 与触发日志落库
 * ({@link RateLimitLogHandler}) 均改由 Feign 远程对接 blade-system，与所在服务的数据源无关，供任意业务服务复用。
 *
 * @author Chill
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(RateLimitAutoConfiguration.class)
public class RateLimitConfiguration {

	@Bean
	public IRateLimitHandler rateLimitHandler(IRateLimitClient rateLimitClient) {
		return new RateLimitHandler(rateLimitClient);
	}

	@Bean
	public IRateLimitLogHandler rateLimitLogHandler(IRateLimitClient rateLimitClient, BladeProperties bladeProperties, ServerInfo serverInfo) {
		return new RateLimitLogHandler(rateLimitClient, bladeProperties, serverInfo);
	}

}
