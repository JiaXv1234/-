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
package org.springblade.system.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * API Key 调用日志传输对象。
 * <p>
 * 由认证服务在主请求线程采集调用上下文 (含调用方服务标识与登录态), 经 Feign 远程发往 blade-system 落库,
 * 故所有字段在调用端填充, 服务端不依赖其自身上下文。
 *
 * @author Chill
 */
@Data
@Schema(description = "API Key 调用日志传输对象")
public class ApiKeyLogDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "主键")
	private Long id;

	@Schema(description = "租户ID")
	private String tenantId;

	@Schema(description = "调用服务名")
	private String serviceId;

	@Schema(description = "服务器IP")
	private String serverIp;

	@Schema(description = "服务器主机名")
	private String serverHost;

	@Schema(description = "环境标识")
	private String env;

	@Schema(description = "API Key 主键ID")
	private Long apiKeyId;

	@Schema(description = "请求地址")
	private String requestUri;

	@Schema(description = "请求方法")
	private String method;

	@Schema(description = "请求IP")
	private String remoteIp;

	@Schema(description = "用户代理")
	private String userAgent;

	@Schema(description = "请求参数")
	private String params;

	@Schema(description = "认证耗时(ms)")
	private Long time;

	@Schema(description = "创建人")
	private String createBy;

	@Schema(description = "创建时间")
	private Date createTime;

}
