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
import org.springblade.core.ratelimit.IRateLimitLogHandler;
import org.springblade.core.ratelimit.RateLimitRule;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.constant.BladeConstant;
import org.springblade.core.tool.support.IdGenerator;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.StringPool;
import org.springblade.core.tool.utils.UrlUtil;
import org.springblade.core.tool.utils.WebUtil;
import org.springblade.system.feign.IRateLimitClient;
import org.springblade.system.pojo.dto.RateLimitLogDTO;

import java.util.Date;
import java.util.concurrent.CompletableFuture;

/**
 * 限流触发日志处理器微服务实现。
 *
 * @author Chill
 */
@Slf4j
@RequiredArgsConstructor
public class RateLimitLogHandler implements IRateLimitLogHandler {

	private final IRateLimitClient rateLimitClient;
	private final BladeProperties bladeProperties;
	private final ServerInfo serverInfo;

	@Override
	public void saveLog(RateLimitRule rule, String limitKey, HttpServletRequest request) {
		// 主线程采集上下文, 仅远程落库交由异步执行
		RateLimitLogDTO rateLimitLog = buildLog(rule, limitKey, request);
		CompletableFuture.runAsync(() -> {
			try {
				rateLimitClient.saveLog(rateLimitLog);
			} catch (Exception logException) {
				log.error("限流日志保存失败: {}", logException.getMessage());
			}
		});
	}

	/**
	 * 构建限流触发日志
	 *
	 * @param rule     触发的限流规则
	 * @param limitKey 实际限流计数 key
	 * @param request  当前请求对象
	 * @return 限流触发日志传输对象
	 */
	private RateLimitLogDTO buildLog(RateLimitRule rule, String limitKey, HttpServletRequest request) {
		RateLimitLogDTO rateLimitLog = new RateLimitLogDTO();
		rateLimitLog.setId(IdGenerator.getId());
		rateLimitLog.setTenantId(Func.toStrWithEmpty(AuthUtil.getTenantId(), BladeConstant.ADMIN_TENANT_ID));
		rateLimitLog.setServiceId(bladeProperties.getName());
		rateLimitLog.setServerIp(serverInfo.getIpWithPort());
		rateLimitLog.setServerHost(serverInfo.getHostName());
		rateLimitLog.setEnv(bladeProperties.getEnv());
		rateLimitLog.setLimitId(rule.getId());
		rateLimitLog.setLimitName(rule.getLimitName());
		rateLimitLog.setLimitDimension(rule.getLimitDimension());
		rateLimitLog.setLimitCount(rule.getLimitCount());
		rateLimitLog.setLimitPeriod(rule.getLimitPeriod());
		rateLimitLog.setTimeUnit(rule.getTimeUnit());
		rateLimitLog.setLimitKey(limitKey);
		rateLimitLog.setRequestUri(UrlUtil.getPath(request.getRequestURI()));
		rateLimitLog.setMethod(request.getMethod());
		rateLimitLog.setRemoteIp(WebUtil.getIP(request));
		rateLimitLog.setUserAgent(Func.toStr(request.getHeader(WebUtil.USER_AGENT_HEADER)));
		rateLimitLog.setParams(WebUtil.getRequestContent(request));
		rateLimitLog.setCreateBy(Func.toStrWithEmpty(AuthUtil.getUserAccount(), StringPool.EMPTY));
		rateLimitLog.setCreateTime(new Date());
		return rateLimitLog;
	}

}
