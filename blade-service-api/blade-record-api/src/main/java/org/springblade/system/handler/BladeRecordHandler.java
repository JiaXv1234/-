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
package org.springblade.system.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.datarecord.annotation.DataRecord;
import org.springblade.core.datarecord.annotation.DataRecordLevel;
import org.springblade.core.datarecord.model.DataRecordInfo;
import org.springblade.core.datarecord.processor.DataRecordHandler;
import org.springblade.system.feign.IDataRecordClient;

/**
 * 日志数据审计处理器
 * <p>
 * 将数据审计输出到日志
 *
 * @author BladeX
 */
@Slf4j
@RequiredArgsConstructor
public class BladeRecordHandler implements DataRecordHandler {

	private final IDataRecordClient dataRecordClient;

	@Override
	public void handle(DataRecordInfo recordInfo, DataRecord dataRecord) {
		// 获取格式化的记录信息
		String recordMessage = formatRecordInfo(recordInfo);
		// 获取数据审计级别
		DataRecordLevel level = dataRecord.level();

		// 数据审计入库处理
		int affectedRows = dataRecordClient.saveRecordData(recordInfo, level.name(), recordMessage);
		if (affectedRows > 0) {
			// 根据配置的日志级别输出
			switch (level) {
				case DEBUG:
					log.debug("数据审计[{}]: {}", recordInfo.getModule(), recordMessage);
					break;
				case INFO:
					log.info("数据审计[{}]: {}", recordInfo.getModule(), recordMessage);
					break;
				case WARN:
					log.warn("数据审计[{}]: {}", recordInfo.getModule(), recordMessage);
					break;
				case ERROR:
					log.error("数据审计[{}]: {}", recordInfo.getModule(), recordMessage);
					break;
			}
		} else {
			// 如果入库失败，记录错误日志
			log.error("数据审计[{}] 保存入库失败", recordInfo.getTableName());
		}

	}

	/**
	 * 格式化记录信息
	 *
	 * @param recordInfo 记录信息
	 * @return 格式化后的字符串
	 */
	private String formatRecordInfo(DataRecordInfo recordInfo) {
		StringBuilder sb = new StringBuilder();
		// 添加模块信息
		sb.append("\n").append("[表名]: ").append(recordInfo.getTableName()).append("\n");
		sb.append("[操作]: ").append(recordInfo.getOperation()).append("\n");
		if (recordInfo.getPrimaryKeyValue() != null) {
			sb.append("[主键]: ").append(recordInfo.getPrimaryKeyValue()).append("\n");
		}
		// 添加用户信息
		if (recordInfo.getUserName() != null) {
			sb.append("[用户]: ").append(recordInfo.getUserName()).append("\n");
		}
		// 添加IP信息
		if (recordInfo.getRemoteIp() != null) {
			sb.append("[IP]: ").append(recordInfo.getRemoteIp()).append("\n");
		}
		// 添加请求URI
		if (recordInfo.getRequestUri() != null) {
			sb.append("[URI]: ").append(recordInfo.getRequestUri()).append("\n");
		}
		if (recordInfo.getChangedFields() != null && !recordInfo.getChangedFields().isEmpty()) {
			sb.append("[变更]: ").append(recordInfo.getChangedFields()).append("\n");
		}
		if (recordInfo.getCost() != null) {
			sb.append("[耗时]: ").append(recordInfo.getCost()).append("ms").append("\n");
		}
		// 如果需要详细信息，添加变更详情
		if (recordInfo.getChangeData() != null && !recordInfo.getChangeData().isEmpty()) {
			sb.append("[详情]: {");
			recordInfo.getChangeData().forEach((field, change) -> sb.append(change.getChangeDescription()).append(", "));
			if (sb.toString().endsWith(", ")) {
				sb.setLength(sb.length() - 2);
			}
			sb.append("}");
		}
		return sb.append("\n").toString();
	}
}
