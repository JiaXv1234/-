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
package org.springblade.attendance.duty.security;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.log.exception.ServiceException;
import org.springframework.stereotype.Component;
import java.util.List;
/**
 * 默认限制为登录用户所属部门；全公司操作仅管理员开放
 * @author Chill
 */
@Component
public class DutyScope {
	@org.springframework.beans.factory.annotation.Value("${attendance.tenant-id:000000}")
	private String attendanceTenant;
	public String tenant() {
		String tenant = AuthUtil.getTenantId();
		if (tenant == null || tenant.isBlank()) throw new ServiceException("请先登录");
		return tenant;
	}
	public List<Long> organs() {
		if (!attendanceTenant.equals(tenant())) throw new ServiceException("当前账号租户未绑定此独立考勤数据库");
		return AuthUtil.isAdministrator() ? null : Func.toLongList(AuthUtil.getUser().getDeptId());
	}
	public void check(Long organId) {
		List<Long> organs = organs();
		if (organId == null ? organs != null : organs != null && !organs.contains(organId))
			throw new ServiceException("无权操作该组织范围");
	}
}

