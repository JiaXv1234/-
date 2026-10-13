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
package org.springblade.attendance.approval.integration;

import lombok.RequiredArgsConstructor;
import org.springblade.core.log.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.ObjectProvider;
import java.util.List;

/**
 * 未接入回写时关闭终态操作，防止流程已结束但业务数据未更新。
 * @author Chill
 */
@Component
@RequiredArgsConstructor
public class ApprovalHandlerRegistry {
	private final ObjectProvider<ApprovalBusinessHandler> handlers;
	public ApprovalBusinessHandler find(String bizType) {
		List<ApprovalBusinessHandler> matches = handlers.orderedStream().filter(handler -> handler.supports(bizType)).toList();
		if (matches.size() > 1) throw new ServiceException("同一业务类型存在多个审批回写处理器，请检查配置");
		return matches.isEmpty() ? null : matches.get(0);
	}
	public ApprovalBusinessHandler require(String bizType) {
		ApprovalBusinessHandler handler = find(bizType);
		if (handler == null) throw new ServiceException("该业务的额度/考勤回写处理器尚未接入，不能结束流程；本次操作未保存");
		return handler;
	}
}
