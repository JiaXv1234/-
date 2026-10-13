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
package org.springblade.attendance.approval.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.attendance.pojo.dto.ApprovalActionDTO;
import org.springblade.attendance.pojo.entity.ApprovalFlow;
import org.springblade.attendance.pojo.entity.DutyEmployee;
import org.springblade.attendance.pojo.vo.*;
import org.springblade.core.mp.support.Query;
import java.util.List;

/**
 * 审批中心与通用流转契约。start仅供服务端业务模块调用，不向前端开放自选审批链。
 * @author Chill
 */
public interface IApprovalService {
	ApprovalIdentityVO identity();
	IPage<ApprovalItemVO> page(Query query, String tab, boolean monitor);
	ApprovalDetailVO detail(String flowId, boolean monitor);
	void agree(ApprovalActionDTO action);
	void reject(ApprovalActionDTO action);
	void copy(ApprovalActionDTO action);
	void markRead(Long ccId);
	List<DutyEmployee> recipients(String keyword);
	ApprovalFlow start(String bizType, String bizNo, List<Long> resolvedApprovers);
}
