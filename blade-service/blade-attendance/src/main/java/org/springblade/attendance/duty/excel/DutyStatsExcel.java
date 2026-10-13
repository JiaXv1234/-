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
import java.math.BigDecimal;
/**
 * 值班年度汇总导出
 * @author Chill
 */
@Data
public class DutyStatsExcel {
	@ExcelProperty("年度")
	private String statYear;
	@ExcelProperty("工号")
	private String userCode;
	@ExcelProperty("姓名")
	private String userName;
	@ExcelProperty("组织ID")
	private String organId;
	@ExcelProperty("计划值班小时数")
	private BigDecimal totalHours;
	@ExcelProperty("次数")
	private Integer dutyCount;
}

