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
package org.springblade.system.controller;

import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.annotation.IsAdmin;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.system.pojo.entity.AuthLog;
import org.springblade.system.pojo.vo.AuthLogVO;
import org.springblade.system.service.IAuthLogService;
import org.springblade.system.wrapper.AuthLogWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

/**
 * 认证日志 控制器
 *
 * @author BladeX
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/auth-log")
@Tag(name = "认证日志", description = "认证日志")
public class AuthLogController {

	private final IAuthLogService authLogService;

	/**
	 * 分页查询认证日志
	 */
	@IsAdmin
	@GetMapping("/page")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "分页查询", description = "传入查询条件")
	public R<IPage<AuthLogVO>> page(@Parameter(hidden = true) AuthLog authLog, Query query) {
		IPage<AuthLog> pages = authLogService.page(Condition.getPage(query), Condition.getQueryWrapper(authLog).orderByDesc("login_time"));
		return R.data(AuthLogWrapper.build().pageVO(pages));
	}

	/**
	 * 查看认证日志详情
	 */
	@IsAdmin
	@GetMapping("/detail")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "查看详情", description = "传入id")
	public R<AuthLogVO> detail(AuthLog authLog) {
		AuthLog detail = authLogService.getOne(Condition.getQueryWrapper(authLog));
		return R.data(AuthLogWrapper.build().entityVO(detail));
	}

}
