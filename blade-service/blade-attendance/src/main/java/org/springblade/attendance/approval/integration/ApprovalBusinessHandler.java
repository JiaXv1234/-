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

import org.springblade.attendance.pojo.entity.ApprovalFlow;
import org.springblade.attendance.pojo.vo.ApprovalBusinessVO;

/**
 * 业务模块接入契约。实现须作为同一考勤服务内的Spring Bean注册。
 * 回写与审批状态在同一数据库事务中完成；禁止在这里用异步通知冒充已完成回写。
 * 调用方业务模块负责规则匹配、申请校验、额度预占/核销/释放及考勤更新。
 * @author Chill
 */
public interface ApprovalBusinessHandler {
	boolean supports(String bizType);
	void onSubmitted(ApprovalFlow flow, ApprovalBusinessVO business);
	void onApproved(ApprovalFlow flow, ApprovalBusinessVO business);
	void onRejected(ApprovalFlow flow, ApprovalBusinessVO business);
	void onProgress(ApprovalFlow flow, ApprovalBusinessVO business, Integer currentLevel, Integer totalLevel);
}
