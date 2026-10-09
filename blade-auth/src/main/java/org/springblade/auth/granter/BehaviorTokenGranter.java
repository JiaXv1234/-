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
package org.springblade.auth.granter;

import org.springblade.core.oauth2.constant.OAuth2TokenConstant;
import org.springblade.core.oauth2.exception.UserInvalidException;
import org.springblade.core.oauth2.granter.PasswordTokenGranter;
import org.springblade.core.oauth2.handler.PasswordHandler;
import org.springblade.core.oauth2.provider.OAuth2Request;
import org.springblade.core.oauth2.service.OAuth2ClientService;
import org.springblade.core.oauth2.service.OAuth2User;
import org.springblade.core.oauth2.service.OAuth2UserService;
import org.springblade.core.redis.cache.BladeRedis;
import org.springblade.core.tool.utils.StringUtil;
import org.springframework.stereotype.Component;

/**
 * BehaviorTokenGranter
 *
 * <p>行为验证码授权模式。行为验证(点选/滑块/旋转)在专用端点完成并换取一次性通行票据，
 * 登录请求经由验证码请求头携带票据，本授权器核销票据后进入密码认证链路。</p>
 *
 * @author Chill
 */
@Component
public class BehaviorTokenGranter extends PasswordTokenGranter {

	private final BladeRedis bladeRedis;

	public BehaviorTokenGranter(OAuth2ClientService clientService, OAuth2UserService userService, PasswordHandler passwordHandler, BladeRedis bladeRedis) {
		super(clientService, userService, passwordHandler);
		this.bladeRedis = bladeRedis;
	}

	@Override
	public String type() {
		return BEHAVIOR;
	}

	@Override
	public OAuth2User user(OAuth2Request request) {
		// 获取行为验证票据信息
		String key = request.getCaptchaKey();
		String ticket = request.getCaptchaCode();
		// 票据一次性核销，防止同一票据重复登录
		String cacheTicket = bladeRedis.getAndDel(OAuth2TokenConstant.BEHAVIOR_TICKET_KEY + key);
		// 判断票据有效性
		if (StringUtil.isBlank(ticket) || !StringUtil.equals(cacheTicket, ticket)) {
			throw new UserInvalidException(OAuth2TokenConstant.BEHAVIOR_NOT_CORRECT);
		}
		return super.user(request);
	}
}
