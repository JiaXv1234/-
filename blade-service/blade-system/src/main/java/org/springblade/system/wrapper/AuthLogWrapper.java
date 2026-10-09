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
package org.springblade.system.wrapper;

import org.springblade.system.cache.UserCache;
import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.system.pojo.entity.AuthLog;
import org.springblade.system.pojo.entity.User;
import org.springblade.system.pojo.vo.AuthLogVO;

import java.util.Objects;

/**
 * 认证日志包装类
 *
 * @author BladeX
 */
public class AuthLogWrapper extends BaseEntityWrapper<AuthLog, AuthLogVO> {

	public static AuthLogWrapper build() {
		return new AuthLogWrapper();
	}

	@Override
	public AuthLogVO entityVO(AuthLog entity) {
		AuthLogVO vo = Objects.requireNonNull(BeanUtil.copyProperties(entity, AuthLogVO.class));
		if (entity.getUserId() != null) {
			User user = UserCache.getUser(entity.getUserId());
			if (user != null) {
				vo.setUserName(user.getName());
			}
		}
		return vo;
	}

}
