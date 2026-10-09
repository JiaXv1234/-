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
package org.springblade.auth.handler;

import org.springblade.core.launch.props.BladeProperties;
import org.springblade.core.launch.server.ServerInfo;
import org.springblade.core.oauth2.provider.OAuth2Request;
import org.springblade.core.oauth2.service.OAuth2User;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.WebUtil;
import org.springblade.system.pojo.entity.AuthLog;
import org.springblade.system.feign.IAuthLogClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

/**
 * 认证日志处理器
 * 在用户认证成功时异步记录登录日志
 *
 * @author BladeX
 */
@Slf4j
@RequiredArgsConstructor
public class BladeLogHandler {

	private final IAuthLogClient authLogClient;
	private final BladeProperties bladeProperties;
	private final ServerInfo serverInfo;

	/**
	 * 记录认证成功日志
	 *
	 * @param user    用户信息
	 * @param request 请求信息
	 */
	public void handleAuthLog(OAuth2User user, OAuth2Request request) {
		// 异步记录日志，避免影响认证性能
		CompletableFuture.runAsync(() -> {
			try {
				AuthLog authLog = buildAuthLog(user, request);
				authLogClient.saveAuthLog(authLog);
			} catch (Exception exception) {
				log.error("记录认证日志异常：{}", exception.getMessage(), exception);
			}
		});
	}

	/**
	 * 构建认证日志实体
	 *
	 * @param user    用户信息
	 * @param request 请求信息
	 * @return AuthLog
	 */
	private AuthLog buildAuthLog(OAuth2User user, OAuth2Request request) {
		AuthLog authLog = new AuthLog();
		authLog.setUserId(Func.toLong(user.getUserId()));
		authLog.setTenantId(user.getTenantId());
		authLog.setServiceId(bladeProperties.getName());
		authLog.setServerIp(serverInfo.getIpWithPort());
		authLog.setServerHost(serverInfo.getHostName());
		authLog.setEnv(bladeProperties.getEnv());
		authLog.setAccount(user.getAccount());
		authLog.setRealName(user.getRealName());
		authLog.setGrantType(request.getGrantType());
		authLog.setRemoteIp(WebUtil.getIP(request.getHttpRequest()));
		authLog.setUserAgent(WebUtil.getUserAgent(request.getHttpRequest()));
		authLog.setLoginTime(DateUtil.now());
		return authLog;
	}

}
