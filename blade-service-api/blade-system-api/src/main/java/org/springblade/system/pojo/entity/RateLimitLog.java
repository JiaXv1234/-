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
import lombok.Data;
import org.springblade.core.tool.utils.DateUtil;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 限流触发日志 实体类
 *
 * @author Chill
 */
@Data
@TableName("blade_rate_limit_log")
public class RateLimitLog implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 主键id
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	private Long id;

	/**
	 * 租户ID
	 */
	private String tenantId;

	/**
	 * 服务ID
	 */
	private String serviceId;

	/**
	 * 服务器IP
	 */
	private String serverIp;

	/**
	 * 服务器名
	 */
	private String serverHost;

	/**
	 * 环境
	 */
	private String env;

	/**
	 * 限流规则主键ID
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	private Long limitId;

	/**
	 * 规则名称
	 */
	private String limitName;

	/**
	 * 限流维度
	 */
	private String limitDimension;

	/**
	 * 请求阈值
	 */
	private Integer limitCount;

	/**
	 * 时间窗口
	 */
	private Integer limitPeriod;

	/**
	 * 时间单位
	 */
	private String timeUnit;

	/**
	 * 限流计数key
	 */
	private String limitKey;

	/**
	 * 请求URI
	 */
	private String requestUri;

	/**
	 * 操作方式
	 */
	private String method;

	/**
	 * 操作IP地址
	 */
	private String remoteIp;

	/**
	 * 用户代理
	 */
	private String userAgent;

	/**
	 * 操作提交的数据
	 */
	private String params;

	/**
	 * 创建人
	 */
	private String createBy;

	/**
	 * 创建时间
	 */
	@DateTimeFormat(pattern = DateUtil.PATTERN_DATETIME)
	@JsonFormat(pattern = DateUtil.PATTERN_DATETIME)
	private Date createTime;

}
