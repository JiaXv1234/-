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
package org.springblade.system.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;

import java.io.Serial;

/**
 * 限流规则 实体类
 *
 * @author Chill
 */
@Data
@TableName("blade_rate_limit")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "RateLimit对象")
public class RateLimit extends TenantEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 规则名称
	 */
	@Schema(description = "规则名称")
	private String limitName;

	/**
	 * URL匹配模式, 支持 Ant 风格, 多个以逗号分隔 (如 /blade-system/user/**,/blade-system/role/**)
	 */
	@Schema(description = "URL匹配模式")
	private String limitPattern;

	/**
	 * 请求方法, GET/POST/PUT/DELETE/ALL, ALL 表示不限方法
	 */
	@Schema(description = "请求方法")
	private String requestMethod;

	/**
	 * 限流维度, GLOBAL/TENANT/IP/USER, 决定限流计数的隔离粒度
	 */
	@Schema(description = "限流维度")
	private String limitDimension;

	/**
	 * 时间窗口内允许的最大请求数
	 */
	@Schema(description = "请求阈值")
	private Integer limitCount;

	/**
	 * 时间窗口大小
	 */
	@Schema(description = "时间窗口")
	private Integer limitPeriod;

	/**
	 * 时间单位, SECONDS/MINUTES/HOURS, 映射 java.util.concurrent.TimeUnit
	 */
	@Schema(description = "时间单位")
	private String timeUnit;

	/**
	 * 优先级, 升序匹配, 数值越小越先命中
	 */
	@Schema(description = "优先级")
	private Integer priority;

	/**
	 * 备注
	 */
	@Schema(description = "备注")
	private String remark;

}
