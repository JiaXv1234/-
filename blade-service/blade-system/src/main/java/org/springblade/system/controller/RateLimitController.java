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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.annotation.IsAdministrator;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.system.pojo.entity.RateLimit;
import org.springblade.system.pojo.vo.RateLimitVO;
import org.springblade.system.service.IRateLimitService;
import org.springblade.system.wrapper.RateLimitWrapper;
import org.springframework.web.bind.annotation.*;

/**
 * 限流规则 控制器
 *
 * @author Chill
 */
@NonDS
@IsAdministrator
@RestController
@AllArgsConstructor
@RequestMapping("/rate-limit")
@Tag(name = "限流规则", description = "限流规则接口")
public class RateLimitController extends BladeController {

	private final IRateLimitService rateLimitService;

	/**
	 * 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description = "传入rateLimit")
	public R<RateLimitVO> detail(RateLimit rateLimit) {
		RateLimit detail = rateLimitService.getOne(Condition.getQueryWrapper(rateLimit));
		return R.data(RateLimitWrapper.build().entityVO(detail));
	}

	/**
	 * 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description = "传入rateLimit")
	public R<IPage<RateLimitVO>> list(@Parameter(hidden = true) RateLimit rateLimit, Query query) {
		IPage<RateLimit> pages = rateLimitService.page(Condition.getPage(query), Condition.getQueryWrapper(rateLimit));
		return R.data(RateLimitWrapper.build().pageVO(pages));
	}

	/**
	 * 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 3)
	@Operation(summary = "新增", description = "传入rateLimit")
	public R<Boolean> save(@Valid @RequestBody RateLimit rateLimit) {
		return R.status(rateLimitService.submitRateLimit(rateLimit));
	}

	/**
	 * 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 4)
	@Operation(summary = "修改", description = "传入rateLimit")
	public R<Boolean> update(@Valid @RequestBody RateLimit rateLimit) {
		return R.status(rateLimitService.submitRateLimit(rateLimit));
	}

	/**
	 * 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 5)
	@Operation(summary = "新增或修改", description = "传入rateLimit")
	public R<Boolean> submit(@Valid @RequestBody RateLimit rateLimit) {
		return R.status(rateLimitService.submitRateLimit(rateLimit));
	}

	/**
	 * 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 6)
	@Operation(summary = "逻辑删除", description = "传入ids")
	public R<Boolean> remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(rateLimitService.removeRateLimit(ids));
	}

	/**
	 * 启用
	 */
	@PostMapping("/enable")
	@ApiOperationSupport(order = 7)
	@Operation(summary = "启用", description = "传入ids")
	public R<Boolean> enable(@RequestParam String ids) {
		return R.status(rateLimitService.enableRateLimit(ids));
	}

	/**
	 * 禁用
	 */
	@PostMapping("/disable")
	@ApiOperationSupport(order = 8)
	@Operation(summary = "禁用", description = "传入ids")
	public R<Boolean> disable(@RequestParam String ids) {
		return R.status(rateLimitService.disableRateLimit(ids));
	}

}
