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
package org.springblade.attendance.approval.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springblade.attendance.approval.service.IApprovalService;
import org.springblade.attendance.pojo.dto.ApprovalActionDTO;
import org.springblade.attendance.pojo.entity.DutyEmployee;
import org.springblade.attendance.pojo.vo.ApprovalDetailVO;
import org.springblade.attendance.pojo.vo.ApprovalIdentityVO;
import org.springblade.attendance.pojo.vo.ApprovalItemVO;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.annotation.PreAuth;
import org.springblade.core.tenant.annotation.TenantDS;
import org.springblade.core.tool.api.R;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 考勤审批中心。流程启动仅提供服务内集成入口，前端不能自行指定审批链。
 * @author Chill
 */
@TenantDS
@RestController
@AllArgsConstructor
@PreAuth(menu = "attendance_approval")
@RequestMapping("approval")
@Tag(name = "考勤审批中心")
public class ApprovalController extends BladeController {
	private final IApprovalService service;

	@GetMapping("/identity")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "当前审批身份")
	public R<ApprovalIdentityVO> identity() { return R.data(service.identity()); }

	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "待审批、已审批和抄送列表")
	public R<IPage<ApprovalItemVO>> list(Query query, @RequestParam(defaultValue = "todo") String tab,
		@RequestParam(defaultValue = "false") boolean monitor) {
		return R.data(service.page(query, tab, monitor));
	}

	@GetMapping("/detail")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "审批详情及可见节点")
	public R<ApprovalDetailVO> detail(@RequestParam String flowId,
		@RequestParam(defaultValue = "false") boolean monitor) {
		return R.data(service.detail(flowId, monitor));
	}

	@PostMapping("/agree")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "同意当前审批待办")
	@PreAuth(permission = "attendance_approval_agree")
	public R<Boolean> agree(@RequestBody ApprovalActionDTO action) {
		service.agree(action); return R.data(true);
	}

	@PostMapping("/reject")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "退回申请人，必须填写原因")
	@PreAuth(permission = "attendance_approval_reject")
	public R<Boolean> reject(@RequestBody ApprovalActionDTO action) {
		service.reject(action); return R.data(true);
	}

	@PostMapping("/cc")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "抄送，不改变审批状态")
	@PreAuth(permission = "attendance_approval_cc")
	public R<Boolean> copy(@RequestBody ApprovalActionDTO action) {
		service.copy(action); return R.data(true);
	}

	@PostMapping("/read")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "本人抄送标记已读")
	@PreAuth(permission = "attendance_approval_read")
	public R<Boolean> read(@RequestParam Long ccId) {
		service.markRead(ccId); return R.data(true);
	}

	@GetMapping("/recipients")
	@ApiOperationSupport(order = 8)
	@Operation(summary = "按工号或姓名检索在职抄送人员")
	@PreAuth(permission = "attendance_approval_cc")
	public R<List<DutyEmployee>> recipients(@RequestParam String keyword) {
		return R.data(service.recipients(keyword));
	}
}

