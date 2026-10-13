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
import cn.idev.excel.annotation.ExcelIgnore;
import lombok.Data;
/**
 * 值班导入模板；所有日期时间以文本输入
 * @author Chill
 */
@Data
public class DutyExcel {
	@ExcelProperty(value = "工号", index = 0)
	private String userCode;
	@ExcelProperty(value = "姓名", index = 1)
	private String userName;
	@ExcelProperty(value = "值班日期", index = 2)
	private String dutyDate;
	@ExcelProperty(value = "开始时间", index = 3)
	private String startTime;
	@ExcelProperty(value = "结束时间", index = 4)
	private String endTime;
	@ExcelIgnore
	private String acrossDay;
	@ExcelProperty(value = "值班类型", index = 5)
	private String dutyType;
	@ExcelProperty(value = "工作内容", index = 6)
	private String dutyContent;
}

