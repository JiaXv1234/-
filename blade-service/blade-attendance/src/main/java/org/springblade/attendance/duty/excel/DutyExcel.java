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
package org.springblade.attendance.duty.excel;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;
/**
 * 值班导入模板；所有日期时间以文本输入
 * @author Chill
 */
@Data
public class DutyExcel {
	@ExcelProperty(value = "工号", index = 0)
	private String userCode;
	@ExcelProperty(value = "值班日期(yyyy-MM-dd)", index = 1)
	private String dutyDate;
	@ExcelProperty(value = "开始时间(HH:mm)", index = 2)
	private String startTime;
	@ExcelProperty(value = "结束时间(HH:mm)", index = 3)
	private String endTime;
	@ExcelProperty(value = "跨天(0/1)", index = 4)
	private String acrossDay;
	@ExcelProperty(value = "类型(1生产/2设备/3应急/4安全)", index = 5)
	private String dutyType;
	@ExcelProperty(value = "值守职责", index = 6)
	private String dutyContent;
}

