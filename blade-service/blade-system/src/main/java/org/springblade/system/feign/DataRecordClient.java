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
package org.springblade.system.feign;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springblade.core.datarecord.model.DataRecordInfo;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.constant.BladeConstant;
import org.springblade.core.tool.jackson.JsonUtil;
import org.springblade.core.tool.utils.Func;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springblade.core.datarecord.constant.DataRecordConstant.RECORD_INSERT_SQL;

/**
 * 数据审计Feign实现类
 *
 * @author BladeX
 */
@NonDS
@Hidden
@RestController
@RequiredArgsConstructor
public class DataRecordClient implements IDataRecordClient {

	private final JdbcTemplate jdbcTemplate;

	@Override
	@PostMapping(SAVE_RECORD_DATA)
	public int saveRecordData(@RequestBody DataRecordInfo recordInfo, String recordLevel, String recordMessage) {
		return jdbcTemplate.update(RECORD_INSERT_SQL,
			// 主键
			recordInfo.getRecordId(),
			// 基础服务信息
			recordInfo.getServiceId(),
			recordInfo.getServerHost(),
			recordInfo.getServerIp(),
			recordInfo.getEnv(),
			// 审计级别
			recordLevel,
			// 请求信息
			recordInfo.getMethod(),
			recordInfo.getRequestUri(),
			recordInfo.getUserAgent(),
			recordInfo.getRemoteIp(),
			// 操作信息
			recordInfo.getOperation(),
			recordInfo.getTableName(),
			// 数据转换：将 Map 转换为 JSON 字符串
			JsonUtil.toJson(recordInfo.getOldData()),
			JsonUtil.toJson(recordInfo.getNewData()),
			// 审计消息
			recordMessage,
			// 审计结果
			recordInfo.getRecordResult(),
			// 记录耗时
			Func.toStr(recordInfo.getCost()),
			// 记录时间
			recordInfo.getRecordTime(),
			// 记录人
			Func.toLong(recordInfo.getUserId()),
			// 业务状态：默认为正常状态
			BladeConstant.DB_STATUS_NORMAL,
			// 逻辑删除状态：默认为未删除状态
			BladeConstant.DB_NOT_DELETED
		);
	}

}
