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

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 令牌调用日志 实体类
 *
 * @author Chill
 */
@Data
@TableName("blade_api_key_log")
@Schema(description = "ApiKeyLog对象")
public class ApiKeyLog implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	@Schema(description = "主键")
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	private Long id;

	/**
	 * 租户ID
	 */
	@Schema(description = "租户ID")
	private String tenantId;

	/**
	 * 服务ID
	 */
	@Schema(description = "服务ID")
	private String serviceId;

	/**
	 * 服务器IP
	 */
	@Schema(description = "服务器IP")
	private String serverIp;

	/**
	 * 服务器名
	 */
	@Schema(description = "服务器名")
	private String serverHost;

	/**
	 * 服务器环境
	 */
	@Schema(description = "服务器环境")
	private String env;

	/**
	 * API Key主键ID
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	@Schema(description = "API Key主键ID")
	private Long apiKeyId;

	/**
	 * 请求URI
	 */
	@Schema(description = "请求URI")
	private String requestUri;

	/**
	 * 操作方式
	 */
	@Schema(description = "操作方式")
	private String method;

	/**
	 * 操作IP地址
	 */
	@Schema(description = "操作IP地址")
	private String remoteIp;

	/**
	 * 用户代理
	 */
	@Schema(description = "用户代理")
	private String userAgent;

	/**
	 * 请求参数
	 */
	@Schema(description = "请求参数")
	private String params;

	/**
	 * 耗时
	 */
	@Schema(description = "耗时")
	private String time;

	/**
	 * 创建人
	 */
	@Schema(description = "创建人")
	private String createBy;

	/**
	 * 创建时间
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(description = "创建时间")
	private Date createTime;

}
