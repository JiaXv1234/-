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
	public R<IPage<DutyPlan>> list(Query query, String month, String code) { return R.data(service.page(query, month, code)); }
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
	public void export(HttpServletResponse response, String month) {
		List<DutyExcel> rows = service.export(month).stream().map(plan -> {
			DutyExcel row = new DutyExcel(); row.setUserCode(plan.getUserCode()); row.setDutyDate(plan.getDutyDate().toString());
			row.setStartTime(plan.getStartTime()); row.setEndTime(plan.getEndTime()); row.setAcrossDay(plan.getAcrossDay().toString());
			row.setDutyType(plan.getDutyType()); row.setDutyContent(plan.getDutyContent()); return row;
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
		if (filename == null || !filename.toLowerCase(Locale.ROOT).endsWith(".xlsx")) throw new ServiceException("请上传xlsx文件");
		if (file.isEmpty() || file.getSize() > 5 * 1024 * 1024) throw new ServiceException("文件为空或超过5MB");
		byte[] bytes = file.getBytes();
		MessageDigest digest = MessageDigest.getInstance("SHA-256"); digest.update(bytes);
		digest.update((month + ":" + organId + ":" + mode).getBytes(StandardCharsets.UTF_8));
		String hash = HexFormat.of().formatHex(digest.digest());
		List<DutyExcel> rows;
		try {
			rows = FastExcel.read(new ByteArrayInputStream(bytes)).head(DutyExcel.class).sheet().doReadSync();
		} catch (Exception exception) { throw new ServiceException("Excel解析失败，请使用下载的模板和文本日期时间"); }
		if (confirm && !hash.equals(previewHash)) throw new ServiceException("文件或参数已变化，请重新预览");
		Map<String, Object> result = confirm ? service.commit(rows, month, organId, mode, filename, hash, bytes) : service.preview(rows, month, organId, mode);
		result.put("previewHash", hash); return R.data(result);
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

