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

import org.springblade.core.secure.BladeUser;
import org.springblade.core.tool.constant.BladeConstant;
import org.springblade.system.pojo.dto.ApiKeyLogDTO;
import org.springframework.stereotype.Component;

/**
 * IApiKeyClientFallback
 *
 * @author Chill
 */
@Component
public class IApiKeyClientFallback implements IApiKeyClient {

	@Override
	public BladeUser getUser(String apiKey) {
		return null;
	}

	@Override
	public String getApiPath(String apiKey) {
		return null;
	}

	@Override
	public int saveLog(ApiKeyLogDTO apiKeyLog) {
		// 降级不落库
		return BladeConstant.DB_STATUS_0;
	}

}
