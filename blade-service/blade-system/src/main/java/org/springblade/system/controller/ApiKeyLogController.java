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

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.annotation.IsAdmin;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.system.pojo.entity.ApiKeyLog;
import org.springblade.system.pojo.vo.ApiKeyLogVO;
import org.springblade.system.service.IApiKeyLogService;
import org.springblade.system.wrapper.ApiKeyLogWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 令牌调用日志 控制器
 *
 * @author Chill
 */
@NonDS
@IsAdmin
@RestController
@AllArgsConstructor
@RequestMapping("/api-key-log")
@Tag(name = "令牌调用日志", description = "令牌调用日志接口")
public class ApiKeyLogController {

	private final IApiKeyLogService apiKeyLogService;

	/**
	 * 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@Operation(summary = "详情", description = "传入apiKeyLog")
	public R<ApiKeyLog> detail(ApiKeyLog apiKeyLog) {
		QueryWrapper<ApiKeyLog> queryWrapper = Condition.getQueryWrapper(apiKeyLog);
		return R.data(queryWrapper.isEmptyOfWhere() ? null : apiKeyLogService.getOne(queryWrapper));
	}

	/**
	 * 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@Operation(summary = "分页", description = "传入apiKeyLog")
	public R<IPage<ApiKeyLogVO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> apiKeyLog, Query query) {
		IPage<ApiKeyLog> pages = apiKeyLogService.page(Condition.getPage(query.setDescs("create_time")), Condition.getQueryWrapper(apiKeyLog, ApiKeyLog.class));
		return R.data(ApiKeyLogWrapper.build().pageVO(pages));
	}

}
