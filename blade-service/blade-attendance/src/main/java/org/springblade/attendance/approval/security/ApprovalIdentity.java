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
package org.springblade.attendance.approval.security;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springblade.attendance.duty.mapper.DutyEmployeeMapper;
import org.springblade.attendance.duty.security.DutyScope;
import org.springblade.attendance.pojo.entity.DutyEmployee;
import org.springblade.attendance.pojo.vo.ApprovalIdentityVO;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.api.R;
import org.springblade.system.feign.IUserClient;
import org.springblade.system.pojo.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 登录用户与考勤人员明确关联。默认按用户编号 code 对应工号，可通过配置覆盖；
 * 不按姓名匹配、不将管理员映射为任意员工，也不直接混用两种用户ID。
 * @author Chill
 */
@Component
@RequiredArgsConstructor
public class ApprovalIdentity {
	private final IUserClient userClient;
	private final DutyEmployeeMapper employeeMapper;
	private final DutyScope scope;
	private final ObjectMapper objectMapper;
	@Value("${attendance.approval.identity-map-json:{}}")
	private String identityMapJson;

	public ApprovalIdentityVO current() {
		scope.organs();
		ApprovalIdentityVO view = new ApprovalIdentityVO();
		view.setLoginUserId(AuthUtil.getUserId());
		view.setAdministrator(AuthUtil.isAdministrator());
		R<User> response = userClient.userInfoById(AuthUtil.getUserId());
		if (response == null || response.getCode() != 200 || response.getData() == null)
			throw new ServiceException("无法校验登录账号，请检查blade-system服务");
		User user = response.getData();
		if (!Objects.equals(AuthUtil.getTenantId(), user.getTenantId())
			|| !Objects.equals(user.getStatus(), 1) || !Objects.equals(user.getIsDeleted(), 0))
			throw new ServiceException("登录账号不属于当前租户或已停用");
		Map<String, String> bindings;
		try { bindings = objectMapper.readValue(identityMapJson, new TypeReference<Map<String, String>>() {}); }
		catch (Exception exception) { throw new ServiceException("attendance.approval.identity-map-json必须是登录用户ID到工号的JSON对象"); }
		if (bindings == null) throw new ServiceException("审批账号映射配置不能为null");
		String code = bindings.getOrDefault(String.valueOf(user.getId()), user.getCode());
		if (code == null || code.isBlank()) {
			view.setMessage("登录账号未关联考勤人员：请将用户编号设置为真实工号，或配置attendance.approval.identity-map-json。");
			return view;
		}
		List<DutyEmployee> employees = employeeMapper.selectList(Wrappers.<DutyEmployee>lambdaQuery()
			.eq(DutyEmployee::getUserCode, code.trim()).eq(DutyEmployee::getOnJobStatus, 1));
		if (employees.size() != 1) {
			view.setMessage("账号关联的工号未找到唯一在职人员，不能处理个人审批任务。");
			return view;
		}
		DutyEmployee employee = employees.get(0);
		view.setEmployeeId(employee.getUserId());
		view.setEmployeeCode(employee.getUserCode());
		view.setEmployeeName(employee.getUserName());
		view.setMessage("已关联考勤人员，审批任务按考勤人员ID校验。");
		return view;
	}
	public Long requireEmployee() {
		ApprovalIdentityVO view = current();
		if (view.getEmployeeId() == null) throw new ServiceException(view.getMessage());
		return view.getEmployeeId();
	}
}
