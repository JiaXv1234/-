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
import org.springblade.core.secure.BladeUser;
import org.springblade.system.pojo.dto.ApiKeyLogDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * API Key Feign接口类
 *
 * @author Chill
 */
@FeignClient(
	value = AppConstant.APPLICATION_SYSTEM_NAME,
	fallback = IApiKeyClientFallback.class
)
public interface IApiKeyClient {

	String API_PREFIX = "/feign/client/api-key";
	String GET_USER = API_PREFIX + "/get-user";
	String GET_API_PATH = API_PREFIX + "/get-api-path";
	String SAVE_LOG = API_PREFIX + "/save-log";

	/**
	 * 通过 API Key 获取用户信息
	 *
	 * @param apiKey API Key
	 * @return BladeUser
	 */
	@GetMapping(GET_USER)
	BladeUser getUser(@RequestParam("apiKey") String apiKey);

	/**
	 * 获取 API Key 的访问路径权限
	 *
	 * @param apiKey API Key
	 * @return apiPath
	 */
	@GetMapping(GET_API_PATH)
	String getApiPath(@RequestParam("apiKey") String apiKey);

	/**
	 * 保存 API Key 调用日志
	 *
	 * @param apiKeyLog API Key 调用日志
	 * @return 影响行数
	 */
	@PostMapping(SAVE_LOG)
	int saveLog(@RequestBody ApiKeyLogDTO apiKeyLog);

}
