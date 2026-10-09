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
package org.springblade.gateway.provider;

import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;
import java.util.LinkedHashSet;

/**
 * RequestProvider
 *
 * @author Chill
 */
public class RequestProvider {

	/**
	 * 查询串分隔符
	 */
	private static final String QUERY_SEPARATOR = "?";

	/**
	 * 获取原始请求路径
	 * <p>
	 * 取裁剪服务名前缀之前的路径，使路径判定不依赖裁剪结果。返回未解码的路径且不含查询串：查询串由外部完全掌控，
	 * 一旦并入被判定的字符串，参数值里的分隔符会被当作路径段参与匹配；解码与归一化则交由判定方按自身语义处理。
	 *
	 * @param exchange 当前请求上下文
	 * @return 原始请求路径（未解码，不含查询串）
	 */
	public static String getOriginalRequestPath(ServerWebExchange exchange) {
		return getOriginalRequestUri(exchange).getRawPath();
	}

	/**
	 * 获取原始请求地址
	 * <p>
	 * 路径与查询串统一取未解码形态，两者编码口径一致，输出可与网络抓包逐字符对照。仅用于日志展示与排障，
	 * 路径判定请改用 {@link #getOriginalRequestPath}。
	 *
	 * @param exchange 当前请求上下文
	 * @return 原始请求地址（未解码，含查询串）
	 */
	public static String getOriginalRequestUrl(ServerWebExchange exchange) {
		URI requestUri = getOriginalRequestUri(exchange);
		String rawQuery = requestUri.getRawQuery();
		if (!StringUtils.hasText(rawQuery)) {
			return requestUri.getRawPath();
		}
		return requestUri.getRawPath() + QUERY_SEPARATOR + rawQuery;
	}

	/**
	 * 获取原始请求 URI
	 * <p>
	 * 网关裁剪服务名前缀时会把改写前的地址登记进请求属性，属性按改写次序累积，取首个即为进入网关时的形态。
	 *
	 * @param exchange 当前请求上下文
	 * @return 原始请求 URI
	 */
	private static URI getOriginalRequestUri(ServerWebExchange exchange) {
		ServerHttpRequest request = exchange.getRequest();
		LinkedHashSet<URI> uris = exchange.getRequiredAttribute(ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR);
		return uris.stream().findFirst().orElse(request.getURI());
	}

}
