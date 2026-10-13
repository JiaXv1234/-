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
package org.springblade.attendance.duty.controller;
import cn.idev.excel.FastExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import lombok.AllArgsConstructor;
import org.springblade.attendance.duty.excel.DutyExcel;
import org.springblade.attendance.duty.excel.DutyStatsExcel;
import org.springblade.attendance.duty.service.IDutyService;
import org.springblade.attendance.duty.integration.DutyAccessService;
import org.springblade.attendance.pojo.dto.DutyDTO;
import org.springblade.attendance.pojo.entity.*;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.annotation.PreAuth;
import org.springblade.core.tenant.annotation.TenantDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.log.exception.ServiceException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
/**
 * 值班管理接口
 * @author Chill
 */
@TenantDS
@RestController
@AllArgsConstructor
@PreAuth(menu = "duty")
@RequestMapping("duty")
@Tag(name = "值班管理")
public class DutyController extends BladeController {
	private final IDutyService service;
	private final DutyAccessService access;
	@GetMapping("/list")
	public R<IPage<DutyPlan>> list(Query query, String month, String code, Long organId) { return R.data(service.page(query, month, code, organId)); }
	@GetMapping("/detail")
	@ApiOperationSupport(order = 11)
	@Operation(summary = "值班详情", description = "仅查询授权组织范围内的有效计划")
	public R<DutyPlan> detail(@RequestParam Long id) { return R.data(service.detail(id)); }
	@GetMapping("/departments")
	@ApiOperationSupport(order = 12)
	@Operation(summary = "值班部门选项", description = "从既有人事组织表读取授权部门")
	public R<List<DutyOrgan>> departments() { return R.data(service.departments()); }
	@GetMapping("/overview")
	@ApiOperationSupport(order = 13)
	@Operation(summary = "值班概览", description = "有效计划小时、本月安排人次和已有状态标记的今日在岗人数")
	public R<Map<String, Object>> overview(@RequestParam String month, @RequestParam(required = false) Long organId) {
		return R.data(service.overview(month, organId));
	}
	@PostMapping("/save")
	@PreAuth(permission = "duty_save")
	public R<DutyPlan> save(@RequestBody DutyDTO dto) { return R.data(service.save(dto)); }
	@PostMapping("/cancel")
	@PreAuth(permission = "duty_cancel")
	public R<Boolean> cancel(@RequestParam Long id) { service.cancel(id); return R.data(true); }
	@GetMapping("/employees")
	public R<List<DutyEmployee>> employees() { return R.data(service.employees()); }
	@PostMapping("/employee-bind")
	@PreAuth(permission = "duty_employee-bind")
	public R<Boolean> bind(@RequestBody DutyEmployee employee) { service.bind(employee); return R.data(true); }
	@GetMapping("/batches")
	public R<IPage<DutyImportLog>> batches(Query query) { return R.data(service.batches(query)); }
	@PostMapping("/rollback")
	@PreAuth(permission = "duty_rollback")
	public R<Boolean> rollback(@RequestParam Long id) { service.rollback(id); return R.data(true); }
	@GetMapping("/tasks")
	public R<IPage<DutyOutbox>> tasks(Query query) { return R.data(service.tasks(query)); }
	@PostMapping("/retry")
	@PreAuth(permission = "duty_retry")
	public R<String> retry(@RequestParam Long id) { return R.data(access.retry(id)); }
	@GetMapping("/statistics")
	public R<List<DutyStats>> statistics(@RequestParam String year) { return R.data(service.statistics(year)); }
	@GetMapping("/template")
	@PreAuth(permission = "duty_import")
	public void template(HttpServletResponse response) {
		ExcelUtil.export(response, "值班导入模板", "值班计划", Collections.emptyList(), DutyExcel.class);
	}
	@GetMapping("/export")
	@PreAuth(permission = "duty_export")
	public void export(HttpServletResponse response, String month, Long organId) {
		List<DutyExcel> rows = service.export(month, organId).stream().map(plan -> {
			DutyExcel row = new DutyExcel(); row.setUserCode(plan.getUserCode()); row.setUserName(plan.getUserName()); row.setDutyDate(plan.getDutyDate().toString());
			row.setStartTime(plan.getStartTime()); row.setEndTime(plan.getEndTime()); row.setAcrossDay(plan.getAcrossDay().toString());
			row.setDutyType(switch (plan.getDutyType()) {
				case "1" -> "生产值班"; case "2" -> "设备值班"; case "3" -> "应急值班"; case "4" -> "安全值班"; default -> plan.getDutyType();
			}); row.setDutyContent(plan.getDutyContent()); return row;
		}).toList();
		ExcelUtil.export(response, "值班计划", "值班计划", rows, DutyExcel.class);
	}
	@GetMapping("/statistics-export")
	@PreAuth(permission = "duty_export")
	public void exportStatistics(@RequestParam String year, HttpServletResponse response) {
		List<DutyStatsExcel> rows = service.statistics(year).stream().map(total -> {
			DutyStatsExcel row = new DutyStatsExcel(); row.setStatYear(total.getStatYear());
			row.setUserCode(total.getUserCode()); row.setUserName(total.getUserName());
			row.setOrganId(total.getOrganId() == null ? "" : total.getOrganId().toString()); row.setTotalHours(total.getTotalHours());
			row.setDutyCount(total.getDutyCount()); return row;
		}).toList();
		ExcelUtil.export(response, "值班年度统计", "计划值班小时汇总", rows, DutyStatsExcel.class);
	}
	@PostMapping("/import")
	@PreAuth(permission = "duty_import")
	public R<Map<String, Object>> importFile(@RequestParam MultipartFile file, @RequestParam String month,
		@RequestParam(required = false) Long organId, @RequestParam String mode,
		@RequestParam(defaultValue = "false") boolean confirm, @RequestParam(required = false) String previewHash) throws Exception {
		String filename = file.getOriginalFilename();
		if (filename == null || !(filename.toLowerCase(Locale.ROOT).endsWith(".xlsx") || filename.toLowerCase(Locale.ROOT).endsWith(".xls")))
			throw new ServiceException("请上传xlsx或xls文件");
		if (file.isEmpty() || file.getSize() > 5 * 1024 * 1024) throw new ServiceException("文件为空或超过5MB");
		byte[] bytes = file.getBytes();
		MessageDigest digest = MessageDigest.getInstance("SHA-256"); digest.update(bytes);
		digest.update((month + ":" + organId + ":" + mode).getBytes(StandardCharsets.UTF_8));
		String hash = HexFormat.of().formatHex(digest.digest());
		List<DutyExcel> rows;
		try {
			rows = readRows(bytes);
		} catch (ServiceException exception) { throw exception; }
		catch (Exception exception) { throw new ServiceException("Excel解析失败，请使用下载的模板和文本日期时间"); }
		if (confirm && !hash.equals(previewHash)) throw new ServiceException("文件或参数已变化，请重新预览");
		Map<String, Object> result = confirm ? service.commit(rows, month, organId, mode, filename, hash, bytes) : service.preview(rows, month, organId, mode);
		result.put("previewHash", hash); return R.data(result);
	}
	/**
	 * 根据标题读取列，兼容原七列模板；新版结束时间早于或等于开始时间时按次日计算。
	 */
	private List<DutyExcel> readRows(byte[] bytes) {
		List<Map<Integer, String>> cells = FastExcel.read(new ByteArrayInputStream(bytes)).headRowNumber(0).sheet().doReadSync();
		if (cells.size() < 2) throw new ServiceException("文件须包含表头和至少一条值班记录");
		if (cells.size() > 2001) throw new ServiceException("单次导入不能超过2000行");
		Map<String, Integer> columns = new HashMap<>();
		cells.get(0).forEach((index, title) -> {
			if (title != null) {
				String name = title.replaceAll("[（(].*$", "").trim();
				if ("类型".equals(name)) name = "值班类型";
				if ("值守职责".equals(name)) name = "工作内容";
				if (columns.putIfAbsent(name, index) != null) throw new ServiceException("模板存在重复列：" + name);
			}
		});
		for (String name : List.of("工号", "值班日期", "开始时间", "结束时间", "值班类型", "工作内容"))
			if (!columns.containsKey(name)) throw new ServiceException("模板缺少列：" + name);
		List<DutyExcel> rows = new ArrayList<>();
		for (int index = 1; index < cells.size(); index++) {
			Map<Integer, String> cell = cells.get(index);
			DutyExcel row = new DutyExcel();
			row.setUserCode(value(cell, columns, "工号")); row.setUserName(value(cell, columns, "姓名"));
			row.setDutyDate(value(cell, columns, "值班日期")); row.setStartTime(value(cell, columns, "开始时间"));
			row.setEndTime(value(cell, columns, "结束时间")); row.setAcrossDay(value(cell, columns, "跨天"));
			row.setDutyType(value(cell, columns, "值班类型")); row.setDutyContent(value(cell, columns, "工作内容"));
			rows.add(row);
		}
		return rows;
	}
	private String value(Map<Integer, String> cell, Map<String, Integer> columns, String name) {
		Integer index = columns.get(name);
		String value = index == null ? null : cell.get(index);
		return value == null ? null : value.trim();
	}
	@GetMapping("/original-file")
	@PreAuth(permission = "duty_export")
	public void originalFile(@RequestParam Long id, HttpServletResponse response) throws IOException {
		byte[] bytes = service.originalFile(id);
		if (bytes == null) throw new ServiceException("原始文件不存在");
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setHeader("Content-Disposition", "attachment; filename=duty-original.xlsx");
		response.getOutputStream().write(bytes);
	}
}

