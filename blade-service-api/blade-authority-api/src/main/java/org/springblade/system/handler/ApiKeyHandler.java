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
package org.springblade.system.handler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.KeyCrypto;
import org.springblade.core.secure.handler.IApiKeyHandler;
import org.springblade.core.secure.handler.IApiKeyLogHandler;
import org.springblade.core.secure.props.KeyProperties;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.StringPool;
import org.springblade.core.tool.utils.WebUtil;
import org.springblade.system.cache.ApiKeyCache;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import static org.springblade.core.secure.constant.ApiKeyConstant.DETAIL_API_KEY_ID;
import static org.springblade.core.secure.constant.ApiKeyConstant.FULL_PATH;

/**
 * API Key 处理器微服务实现
 *
 * @author Chill
 */
@RequiredArgsConstructor
public class ApiKeyHandler implements IApiKeyHandler {

	/**
	 * 路径匹配器
	 */
	private static final PathMatcher PATH_MATCHER = new AntPathMatcher();

	private final KeyProperties keyProperties;
	private final IApiKeyLogHandler apiKeyLogHandler;

	@Override
	public BladeUser getUser(String apiKey) {
		long startTime = System.currentTimeMillis();
		// 检查功能是否启用
		if (!keyProperties.getEnabled()) {
			return null;
		}
		// 检查令牌格式是否合法
		String parseKey = KeyCrypto.parseKey(apiKey, keyProperties.getCryptoKey());
		if (Func.isBlank(parseKey)) {
			return null;
		}
		// 加载用户信息
		BladeUser bladeUser = ApiKeyCache.getUser(apiKey);
		if (bladeUser == null) {
			return null;
		}
		// 验证访问路径权限
		HttpServletRequest request = WebUtil.getRequest();
		if (request != null) {
			// api_path 按完整路径配置，补全服务前缀后匹配
			String requestPath = keyProperties.getPathPrefix().concat(request.getRequestURI());
			if (!validateApiPath(apiKey, requestPath)) {
				return null;
			}
		}
		// 异步保存 API Key 调用日志
		long apiKeyId = Func.toLong(bladeUser.getDetail().get(DETAIL_API_KEY_ID));
		if (apiKeyId > 0L) {
			long time = System.currentTimeMillis() - startTime;
			apiKeyLogHandler.saveLog(bladeUser, apiKeyId, time, request);
		}
		return bladeUser;
	}

	@Override
	public void removeCache(String apiKey) {
		ApiKeyCache.removeCache(apiKey);
	}

	@Override
	public String generateKey() {
		return KeyCrypto.generateKey(keyProperties.getCryptoKey());
	}

	/**
	 * 验证请求路径是否有访问权限
	 *
	 * @param apiKey      API Key
	 * @param requestPath 请求路径
	 * @return true 有权限，false 无权限
	 */
	private boolean validateApiPath(String apiKey, String requestPath) {
		String apiPath = ApiKeyCache.getApiPath(apiKey);
		// 如果未配置访问权限，默认允许所有访问
		if (Func.isBlank(apiPath) || FULL_PATH.equals(apiPath)) {
			return true;
		}
		// 解析逗号分隔的路径并匹配
		String[] paths = apiPath.split(StringPool.COMMA);
		for (String path : paths) {
			String trimmedPath = path.trim();
			if (Func.isNotBlank(trimmedPath) && PATH_MATCHER.match(trimmedPath, requestPath)) {
				return true;
			}
		}
		return false;
	}

}
