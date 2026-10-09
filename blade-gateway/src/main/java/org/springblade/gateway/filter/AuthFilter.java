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
package org.springblade.gateway.filter;

import com.alibaba.nacos.common.utils.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.jwt.JwtCrypto;
import org.springblade.core.jwt.JwtUtil;
import org.springblade.core.jwt.props.JwtProperties;
import org.springblade.core.launch.constant.TokenConstant;
import org.springblade.core.launch.props.BladeProperties;
import org.springblade.gateway.props.AuthProperties;
import org.springblade.gateway.provider.AuthProvider;
import org.springblade.gateway.provider.KeyProvider;
import org.springblade.gateway.provider.RequestProvider;
import org.springblade.gateway.provider.ResponseProvider;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

import static org.springblade.core.jwt.JwtCrypto.BLADE_TOKEN_CRYPTO_KEY;

/**
 * 鉴权认证
 *
 * @author Chill
 */
@Slf4j
@Component
@AllArgsConstructor
public class AuthFilter implements GlobalFilter, Ordered {
	private static final String MSG_TOKEN_MISSING = "缺失令牌,鉴权失败";
	private static final String MSG_REQUEST_UNAUTHORIZED = "请求未授权";
	private static final String MSG_TOKEN_EXPIRED = "令牌已失效";

	private final AuthProperties authProperties;
	private final ObjectMapper objectMapper;
	private final JwtProperties jwtProperties;
	private final BladeProperties bladeProperties;
	private final AntPathMatcher antPathMatcher = new AntPathMatcher();

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		//校验 Token 放行
		String path = exchange.getRequest().getURI().getPath();
		String originalPath = RequestProvider.getOriginalRequestPath(exchange);
		if (isSkip(path) || isSkip(originalPath)) {
			return chain.filter(exchange);
		}
		//校验 Token 合法性
		ServerHttpResponse resp = exchange.getResponse();
		String headerToken = exchange.getRequest().getHeaders().getFirst(AuthProvider.AUTH_KEY);
		String paramToken = exchange.getRequest().getQueryParams().getFirst(AuthProvider.AUTH_KEY);
		if (StringUtils.isBlank(headerToken) && StringUtils.isBlank(paramToken)) {
			return unAuth(resp, MSG_TOKEN_MISSING);
		}
		String auth = StringUtils.isBlank(headerToken) ? paramToken : headerToken;
		// 判断 auth 是否为空
		if (StringUtils.isBlank(auth)) {
			return unAuth(resp, MSG_REQUEST_UNAUTHORIZED);
		}
		// 校验 超级密钥 合法性，剩余属性交由微服务内部自行鉴权
		if (KeyProvider.isApiKey(auth, bladeProperties)) {
			return chain.filter(exchange);
		}
		String token = JwtUtil.getToken(auth);
		// 判断 token 是否为空
		if (StringUtils.isBlank(token)) {
			return unAuth(resp, MSG_REQUEST_UNAUTHORIZED);
		}
		// 校验 加密Token 合法性
		if (JwtUtil.isCrypto(auth)) {
			token = JwtCrypto.decryptToString(token, bladeProperties.getEnvironment().getProperty(BLADE_TOKEN_CRYPTO_KEY));
		}
		Claims claims = JwtUtil.parseJWT(token);
		if (token == null || claims == null) {
			return unAuth(resp, MSG_REQUEST_UNAUTHORIZED);
		}
		// 判断 Token 状态
		if (jwtProperties.getState()) {
			String tenantId = String.valueOf(claims.get(TokenConstant.TENANT_ID));
			String clientId = String.valueOf(claims.get(TokenConstant.CLIENT_ID));
			String userId = String.valueOf(claims.get(TokenConstant.USER_ID));
			String accessToken = JwtUtil.getAccessToken(tenantId, clientId, userId, token);
			if (!token.equalsIgnoreCase(accessToken)) {
				return unAuth(resp, MSG_TOKEN_EXPIRED);
			}
		}
		return chain.filter(exchange);
	}

	private boolean isSkip(String path) {
		return AuthProvider.getDefaultSkipUrl().stream().anyMatch(pattern -> antPathMatcher.match(pattern, path))
			|| authProperties.getSkipUrl().stream().anyMatch(pattern -> antPathMatcher.match(pattern, path))
 			|| authProperties.getAuth().stream().anyMatch(auth -> antPathMatcher.match(auth.getPattern(), path))
 			|| authProperties.getBasic().stream().anyMatch(basic -> antPathMatcher.match(basic.getPattern(), path))
			|| authProperties.getSign().stream().anyMatch(sign -> antPathMatcher.match(sign.getPattern(), path));
	}

	private Mono<Void> unAuth(ServerHttpResponse resp, String msg) {
		resp.setStatusCode(HttpStatus.UNAUTHORIZED);
		resp.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
		String result = "";
		try {
			result = objectMapper.writeValueAsString(ResponseProvider.unAuth(msg));
		} catch (JsonProcessingException e) {
			log.error(e.getMessage(), e);
		}
		DataBuffer buffer = resp.bufferFactory().wrap(result.getBytes(StandardCharsets.UTF_8));
		return resp.writeWith(Flux.just(buffer));
	}


	@Override
	public int getOrder() {
		return -100;
	}

}
