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
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.launch.props.BladeProperties;
import org.springblade.core.launch.server.ServerInfo;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.handler.IApiKeyLogHandler;
import org.springblade.core.tool.constant.BladeConstant;
import org.springblade.core.tool.support.IdGenerator;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.StringPool;
import org.springblade.core.tool.utils.UrlUtil;
import org.springblade.core.tool.utils.WebUtil;
import org.springblade.system.feign.IApiKeyClient;
import org.springblade.system.pojo.dto.ApiKeyLogDTO;
import org.springframework.lang.Nullable;

import java.util.Date;
import java.util.concurrent.CompletableFuture;

/**
 * API Key 调用日志处理器微服务实现。
 *
 * @author Chill
 */
@Slf4j
@RequiredArgsConstructor
public class ApiKeyLogHandler implements IApiKeyLogHandler {

	private final IApiKeyClient apiKeyClient;
	private final BladeProperties bladeProperties;
	private final ServerInfo serverInfo;

	@Override
	public void saveLog(BladeUser bladeUser, Long apiKeyId, long time, @Nullable HttpServletRequest request) {
		// 主线程采集上下文, 仅远程落库交由异步执行
		ApiKeyLogDTO apiKeyLog = buildLog(bladeUser, apiKeyId, time, request);
		CompletableFuture.runAsync(() -> {
			try {
				apiKeyClient.saveLog(apiKeyLog);
			} catch (Exception logException) {
				log.error("API Key日志保存失败: {}", logException.getMessage());
			}
		});
	}

	/**
	 * 构建 API Key 调用日志
	 *
	 * @param bladeUser 认证用户
	 * @param apiKeyId  API Key 主键ID
	 * @param time      认证耗时(ms)
	 * @param request   当前请求对象
	 * @return API Key 调用日志传输对象
	 */
	private ApiKeyLogDTO buildLog(BladeUser bladeUser, Long apiKeyId, long time, @Nullable HttpServletRequest request) {
		ApiKeyLogDTO apiKeyLog = new ApiKeyLogDTO();
		apiKeyLog.setId(IdGenerator.getId());
		apiKeyLog.setTenantId(Func.toStrWithEmpty(bladeUser.getTenantId(), BladeConstant.ADMIN_TENANT_ID));
		apiKeyLog.setServiceId(bladeProperties.getName());
		apiKeyLog.setServerIp(serverInfo.getIpWithPort());
		apiKeyLog.setServerHost(serverInfo.getHostName());
		apiKeyLog.setEnv(bladeProperties.getEnv());
		apiKeyLog.setApiKeyId(apiKeyId);
		apiKeyLog.setTime(time);
		apiKeyLog.setCreateBy(Func.toStrWithEmpty(bladeUser.getAccount(), bladeUser.getUserName()));
		apiKeyLog.setCreateTime(new Date());
		// 请求信息 (默认空串, 防止匿名/无请求上下文时落库空值)
		apiKeyLog.setRequestUri(StringPool.EMPTY);
		apiKeyLog.setMethod(StringPool.EMPTY);
		apiKeyLog.setRemoteIp(StringPool.EMPTY);
		apiKeyLog.setUserAgent(StringPool.EMPTY);
		apiKeyLog.setParams(StringPool.EMPTY);
		if (request != null) {
			apiKeyLog.setRequestUri(UrlUtil.getPath(request.getRequestURI()));
			apiKeyLog.setMethod(request.getMethod());
			apiKeyLog.setRemoteIp(WebUtil.getIP(request));
			apiKeyLog.setUserAgent(Func.toStr(request.getHeader(WebUtil.USER_AGENT_HEADER)));
			apiKeyLog.setParams(WebUtil.getRequestContent(request));
		}
		return apiKeyLog;
	}

}
