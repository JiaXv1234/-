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
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.system.pojo.enums.AuthLockStatus;
import org.springblade.system.pojo.entity.AuthLock;
import org.springblade.system.pojo.entity.User;
import org.springblade.system.pojo.vo.AuthLockVO;

import java.util.Objects;

/**
 * 认证锁定记录包装类
 *
 * @author BladeX
 */
public class AuthLockWrapper extends BaseEntityWrapper<AuthLock, AuthLockVO> {

	public static AuthLockWrapper build() {
		return new AuthLockWrapper();
	}

	@Override
	public AuthLockVO entityVO(AuthLock entity) {
		AuthLockVO vo = Objects.requireNonNull(BeanUtil.copyProperties(entity, AuthLockVO.class));
		if (entity.getLockType() != null) {
			vo.setLockTypeName(entity.getLockType().getDescription());
		}
		if (entity.getLockStatus() != null) {
			vo.setLockStatusName(entity.getLockStatus().getDescription());
		}
		if (entity.getUserId() != null) {
			User user = UserCache.getUser(entity.getUserId());
			if (user != null) {
				vo.setUserName(user.getName());
			}
		}
		// 惰性过期判定：LOCKED + lockEndTime 已过期 → 标记为已过期（适用于 SYSTEM 和 MANUAL）
		boolean expired = AuthLockStatus.LOCKED.equals(entity.getLockStatus())
			&& entity.getLockEndTime() != null
			&& entity.getLockEndTime().before(DateUtil.now());
		vo.setExpired(expired);
		if (expired) {
			vo.setLockStatusName("已过期");
		}
		return vo;
	}

}
