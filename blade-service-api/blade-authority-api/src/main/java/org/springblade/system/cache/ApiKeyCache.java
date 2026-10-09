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
package org.springblade.system.cache;

import org.springblade.core.cache.utils.CacheUtil;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.SpringUtil;
import org.springblade.system.feign.IApiKeyClient;

import java.util.Objects;

import static org.springblade.core.secure.constant.ApiKeyConstant.*;

/**
 * API Key 缓存
 *
 * @author Chill
 */
public class ApiKeyCache {

	private static IApiKeyClient apiKeyClient;

	private static IApiKeyClient getApiKeyClient() {
		if (apiKeyClient == null) {
			apiKeyClient = SpringUtil.getBean(IApiKeyClient.class);
		}
		return apiKeyClient;
	}

	/**
	 * 获取用户信息
	 *
	 * @param apiKey API Key
	 * @return BladeUser
	 */
	public static BladeUser getUser(String apiKey) {
		BladeUser bladeUser = CacheUtil.get(API_KEY_CACHE, CACHE_USER_PREFIX, apiKey, BladeUser.class, Boolean.FALSE);
		if (bladeUser != null) {
			// 若用户ID为空，说明是缓存的空对象标记，返回null防止缓存穿透
			return bladeUser.getUserId() != null ? bladeUser : null;
		}
		bladeUser = getApiKeyClient().getUser(apiKey);
		// 动态缓存用户对象，防止缓存穿透
		CacheUtil.put(API_KEY_CACHE, CACHE_USER_PREFIX, apiKey, Objects.requireNonNullElseGet(bladeUser, BladeUser::new), Boolean.FALSE);
		return bladeUser;
	}

	/**
	 * 获取访问路径权限
	 *
	 * @param apiKey API Key
	 * @return apiPath
	 */
	public static String getApiPath(String apiKey) {
		String apiPath = CacheUtil.get(API_KEY_CACHE, CACHE_PATH_PREFIX, apiKey, String.class, Boolean.FALSE);
		if (apiPath == null) {
			apiPath = getApiKeyClient().getApiPath(apiKey);
			// 动态缓存权限路径，防止缓存穿透
			CacheUtil.put(API_KEY_CACHE, CACHE_PATH_PREFIX, apiKey, Func.toStrWithEmpty(apiPath, FULL_PATH), Boolean.FALSE);
		}
		return apiPath;
	}

	/**
	 * 移除缓存
	 *
	 * @param apiKey API Key
	 */
	public static void removeCache(String apiKey) {
		CacheUtil.evict(API_KEY_CACHE, CACHE_USER_PREFIX, apiKey, Boolean.FALSE);
		CacheUtil.evict(API_KEY_CACHE, CACHE_PATH_PREFIX, apiKey, Boolean.FALSE);
	}

}
