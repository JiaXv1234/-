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
package org.springblade.gateway.config;

import com.github.xiaoymin.knife4j.spring.gateway.Knife4jGatewayProperties;
import com.github.xiaoymin.knife4j.spring.gateway.discover.router.DiscoverClientRouteServiceConvert;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.discovery.DiscoveryClientRouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * knife4j自动聚合配置
 *
 * @author BladeX
 */
@Configuration(proxyBeanMethods = false)
public class ReactiveDiscoveryConfiguration {

	@Bean
	@ConditionalOnProperty(name = {"spring.cloud.gateway.server.webflux.discovery.locator.enabled"})
	public DiscoverClientRouteServiceConvert discoverClientRouteServiceConvert(DiscoveryClientRouteDefinitionLocator discoveryClient,
																			   Knife4jGatewayProperties knife4jGatewayProperties) {
		return new DiscoverClientRouteServiceConvert(discoveryClient, knife4jGatewayProperties);
	}

}
