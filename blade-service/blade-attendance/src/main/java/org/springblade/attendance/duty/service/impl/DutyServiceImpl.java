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
package org.springblade.attendance.duty.service.impl;
import org.springblade.attendance.duty.service.IDutyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springblade.attendance.pojo.dto.DutyDTO;
import org.springblade.attendance.pojo.entity.*;
import org.springblade.attendance.duty.mapper.*;
import org.springblade.attendance.duty.security.DutyScope;
import org.springblade.attendance.duty.excel.DutyExcel;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.utils.AuthUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.math.*;
import java.util.*;
/**
	 * 值班计划与年度统计；严格使用说明书既有字段
 * @author Chill
 */
@Service
@RequiredArgsConstructor
public class DutyServiceImpl extends ServiceImpl<DutyPlanMapper, DutyPlan> implements IDutyService {
	private final DutyPlanMapper plans;
	private final DutyEmployeeMapper employees;
	private final DutyImportLogMapper logs;


	private final DutyLockMapper locks;
	private final DutyStatsMapper statsMapper;
	private final DutyOrganMapper organMapper;
	private final DutyScope scope;
	private void lock() { scope.organs(); locks.lock(); }
	private LambdaQueryWrapper<DutyPlan> base() {
		LambdaQueryWrapper<DutyPlan> wrapper = Wrappers.<DutyPlan>lambdaQuery();
		List<Long> organs = scope.organs();
		if (organs != null) {
			if (organs.isEmpty()) wrapper.eq(DutyPlan::getOrganId, -1L);
			else wrapper.in(DutyPlan::getOrganId, organs);
		}
		return wrapper;
	}
	public IPage<DutyPlan> page(Query query, String month, String code, Long organId) {
		if (organId != null) scope.check(organId);
		LambdaQueryWrapper<DutyPlan> wrapper = base().eq(DutyPlan::getDataStatus, 1)
			.eq(month != null && !month.isBlank(), DutyPlan::getPlanMonth, month)
			.eq(organId != null, DutyPlan::getOrganId, organId);
		if (code != null && !code.isBlank()) {
			DutyEmployee employee = employees.selectOne(Wrappers.<DutyEmployee>lambdaQuery().eq(DutyEmployee::getUserCode, code.trim()));
			wrapper.eq(DutyPlan::getUserId, employee == null ? -1L : employee.getUserId());
		}
		IPage<DutyPlan> result = plans.selectPage(Condition.getPage(query), wrapper.orderByDesc(DutyPlan::getDutyDate));
		result.getRecords().forEach(this::enrich); return result;
	}
	public DutyPlan detail(Long id) {
		DutyPlan plan = plans.selectOne(base().eq(DutyPlan::getId, id).eq(DutyPlan::getDataStatus, 1));
		if (plan == null) throw new ServiceException("计划不存在、已作废或不在授权范围内");
		enrich(plan);
		return plan;
	}
	public List<DutyOrgan> departments() {
		LambdaQueryWrapper<DutyOrgan> wrapper = Wrappers.<DutyOrgan>lambdaQuery();
		List<Long> organIds = scope.organs();
		if (organIds != null) wrapper.in(DutyOrgan::getOrganId, organIds.isEmpty() ? List.of(-1L) : organIds);
		return organMapper.selectList(wrapper.orderByAsc(DutyOrgan::getDeptCode));
	}
	/**
	 * 概览与年度统计保持有效计划口径，不把计划时间推算成实际打卡实绩。
	 */
	public Map<String, Object> overview(String month, Long organId) {
		if (month == null || !month.matches("\\d{6}")) throw new ServiceException("月份格式必须为yyyyMM");
		YearMonth selectedMonth = YearMonth.parse(month, DateTimeFormatter.ofPattern("yyyyMM"));
		if (organId != null) scope.check(organId);
		List<DutyPlan> annualPlans = plans.selectList(base().eq(DutyPlan::getDataStatus, 1)
			.eq(organId != null, DutyPlan::getOrganId, organId)
			.likeRight(DutyPlan::getPlanMonth, String.valueOf(selectedMonth.getYear())));
		LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
		List<DutyPlan> todayPlans = plans.selectList(base().eq(DutyPlan::getDataStatus, 1)
			.eq(organId != null, DutyPlan::getOrganId, organId)
			.eq(DutyPlan::getDutyStatus, "2")
			.between(DutyPlan::getDutyDate, now.toLocalDate().minusDays(1), now.toLocalDate()));
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("year", selectedMonth.getYear());
		result.put("totalHours", annualPlans.stream().map(DutyPlan::getDutyHours)
			.filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add));
		result.put("monthCount", annualPlans.stream().filter(plan -> month.equals(plan.getPlanMonth())).count());
		result.put("todayOnDuty", todayPlans.stream()
			.filter(plan -> !now.isBefore(plan.getStartsAt()) && now.isBefore(plan.getEndsAt()))
			.map(DutyPlan::getUserId).distinct().count());
		result.put("statisticsBasis", "PLAN");
		return result;
	}
	public List<DutyPlan> export(String month, Long organId) {
		if (organId != null) scope.check(organId);
		List<DutyPlan> result = plans.selectList(base().eq(DutyPlan::getDataStatus, 1)
			.eq(month != null && !month.isBlank(), DutyPlan::getPlanMonth, month)
			.eq(organId != null, DutyPlan::getOrganId, organId).orderByAsc(DutyPlan::getDutyDate));	result.forEach(this::enrich); return result;
	}
	public IPage<DutyImportLog> batches(Query query) {
		LambdaQueryWrapper<DutyImportLog> wrapper = Wrappers.<DutyImportLog>lambdaQuery();
		List<Long> organs = scope.organs();
		if (organs != null) { if (organs.isEmpty()) wrapper.eq(DutyImportLog::getOrganId, -1L); else wrapper.in(DutyImportLog::getOrganId, organs); }
		return logs.selectPage(Condition.getPage(query), wrapper.orderByDesc(DutyImportLog::getImportTime));
	}
	public IPage<DutyOutbox> tasks(Query query) { return Condition.getPage(query); }
	public List<DutyEmployee> employees() {
		LambdaQueryWrapper<DutyEmployee> wrapper=Wrappers.<DutyEmployee>lambdaQuery().eq(DutyEmployee::getOnJobStatus, 1);
		List<Long> organs=scope.organs(); if(organs!=null) wrapper.in(DutyEmployee::getOrganId,organs.isEmpty()?List.of(-1L):organs);
		return employees.selectList(wrapper);
	}
	public void bind(DutyEmployee input) { throw new ServiceException("人员信息直接读取 tb_g2_user，请在人事模块维护"); }
	private void enrich(DutyPlan plan) {
		DutyEmployee employee = employees.selectById(plan.getUserId());
		if (employee != null) { plan.setUserCode(employee.getUserCode()); plan.setUserName(employee.getUserName()); }
		DutyOrgan organ = plan.getOrganId() == null ? null : organMapper.selectById(plan.getOrganId());
		plan.setOrganName(organ == null ? "部门未匹配" : organ.getDeptName());
	}
	private DutyPlan build(DutyDTO dto) {
		if (dto.getUserCode() == null || dto.getUserCode().isBlank() || dto.getDutyDate() == null) throw new ServiceException("工号和值班日期必填");
		DutyEmployee employee = employees.selectOne(Wrappers.<DutyEmployee>lambdaQuery()
			.eq(DutyEmployee::getUserCode, dto.getUserCode().trim()));
		if (employee == null) throw new ServiceException("工号不存在");
		if (!Objects.equals(employee.getOnJobStatus(), 1)) throw new ServiceException("该工号不是在职人员");
		scope.check(employee.getOrganId());
		if (!Objects.equals(dto.getAcrossDay(), 0) && !Objects.equals(dto.getAcrossDay(), 1)) throw new ServiceException("跨天必须为0或1");
		if (dto.getStartTime() == null || dto.getEndTime() == null || !dto.getStartTime().matches("\\d{2}:\\d{2}") || !dto.getEndTime().matches("\\d{2}:\\d{2}"))
			throw new ServiceException("时间格式必须为HH:mm");
		LocalTime start;
		LocalTime end;
		try { start = LocalTime.parse(dto.getStartTime()); end = LocalTime.parse(dto.getEndTime()); }
		catch (DateTimeException exception) { throw new ServiceException("开始或结束时间无效，请填写HH:mm格式的有效时间"); }
		LocalDateTime starts = dto.getDutyDate().atTime(start);
		LocalDateTime ends = dto.getDutyDate().plusDays(dto.getAcrossDay()).atTime(end);
		long minutes = Duration.between(starts, ends).toMinutes();
		if (minutes <= 0 || minutes > 1440) throw new ServiceException("值班时长须大于0且不超过24小时");
		if (!Set.of("1", "2", "3", "4").contains(dto.getDutyType() == null ? "" : dto.getDutyType())) throw new ServiceException("值班类型必须为1至4");
		if (dto.getDutyContent() != null && dto.getDutyContent().length() > 500) throw new ServiceException("值守职责不能超过500字");
		DutyPlan plan = new DutyPlan();
		plan.setUserId(employee.getUserId()); plan.setUserCode(employee.getUserCode()); plan.setUserName(employee.getUserName());
		plan.setOrganId(employee.getOrganId()); plan.setDutyDate(dto.getDutyDate());
		plan.setPlanMonth(dto.getDutyDate().format(DateTimeFormatter.ofPattern("yyyyMM")));
		plan.setStartTime(dto.getStartTime()); plan.setEndTime(dto.getEndTime()); plan.setAcrossDay(dto.getAcrossDay());
		plan.setDutyHours(BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 1, RoundingMode.HALF_UP));
		plan.setDutyType(dto.getDutyType()); plan.setDutyContent(dto.getDutyContent()); plan.setDataStatus(1);
		plan.setDutyStatus("1"); plan.setSource("1"); plan.setStatsFlag("1");
		return plan;
	}
	private boolean overlaps(DutyPlan a, DutyPlan b) {
		return a.getUserId().equals(b.getUserId()) && a.getStartsAt().isBefore(b.getEndsAt()) && b.getStartsAt().isBefore(a.getEndsAt());
	}
	private List<DutyPlan> active() {
		return plans.selectList(Wrappers.<DutyPlan>lambdaQuery().eq(DutyPlan::getDataStatus, 1));
	}
	@Transactional(rollbackFor = Exception.class)
	public DutyPlan save(DutyDTO dto) {
		lock(); DutyPlan plan = build(dto);
		for (DutyPlan existing : active()) if (overlaps(plan, existing)) throw new ServiceException("该人员已有重叠值班计划");
		plan.setImportBatch("MANUAL"); plan.setSource("2"); plans.insert(plan); rebuild(); return plan;
	}
	@Transactional(rollbackFor = Exception.class)
	public void cancel(Long id) {
		lock(); DutyPlan plan = plans.selectOne(base().eq(DutyPlan::getId, id));
		if (plan == null || plan.getDataStatus() != 1) throw new ServiceException("计划不存在或已作废");
		plan.setDataStatus(0); plan.setStatsFlag("0"); plans.updateById(plan); rebuild();
	}
	public Map<String, Object> preview(List<DutyExcel> rows, String month, Long organId, String mode) {
		if (month == null || !month.matches("\\d{6}")) throw new ServiceException("归属月份格式必须为yyyyMM");
		YearMonth.parse(month, DateTimeFormatter.ofPattern("yyyyMM")); scope.check(organId);
		if (!Set.of("1", "2").contains(mode)) throw new ServiceException("导入方式必须为1覆盖或2追加");
		if (rows.isEmpty() || rows.size() > 2000) throw new ServiceException("导入须包含1至2000条记录");
		List<DutyPlan> accepted = new ArrayList<>(); List<Map<String, Object>> errors = new ArrayList<>();
		List<DutyPlan> existing = active();
		if ("1".equals(mode)) existing.removeIf(p -> month.equals(p.getPlanMonth()) && (organId == null || organId.equals(p.getOrganId())));
		for (int index = 0; index < rows.size(); index++) {
			try {
				DutyExcel row = rows.get(index); DutyDTO dto = new DutyDTO();
				dto.setUserCode(row.getUserCode()); dto.setDutyDate(LocalDate.parse(row.getDutyDate())); dto.setStartTime(row.getStartTime());
				dto.setEndTime(row.getEndTime());
				dto.setAcrossDay(row.getAcrossDay() == null || row.getAcrossDay().isBlank()
					? (LocalTime.parse(row.getEndTime()).isAfter(LocalTime.parse(row.getStartTime())) ? 0 : 1)
					: Integer.valueOf(row.getAcrossDay()));
				String dutyType = row.getDutyType();
				dto.setDutyType(switch (dutyType == null ? "" : dutyType.trim()) {
					case "生产值班" -> "1";
					case "设备值班" -> "2";
					case "应急值班" -> "3";
					case "安全值班" -> "4";
					default -> dutyType == null ? null : dutyType.trim();
				});
				dto.setDutyContent(row.getDutyContent());
				DutyPlan plan = build(dto);
				if (row.getUserName() != null && !row.getUserName().isBlank() && !row.getUserName().trim().equals(plan.getUserName()))
					throw new ServiceException("姓名与工号对应的人员不一致");
				if (!month.equals(plan.getPlanMonth())) throw new ServiceException("值班日期不属于导入月份");
				if (organId != null && !organId.equals(plan.getOrganId())) throw new ServiceException("人员不属于导入组织范围");
				for (DutyPlan other : existing) if (overlaps(plan, other)) throw new ServiceException("与已有值班时段重叠");
				for (DutyPlan other : accepted) if (overlaps(plan, other)) throw new ServiceException("文件内值班时段重复或重叠");
				accepted.add(plan);
			} catch (Exception exception) {
				errors.add(Map.of("row", index + 2, "message", exception instanceof ServiceException ? exception.getMessage() : "日期、时间或跨天字段格式错误"));
			}
		}
		Map<String, Object> result = new LinkedHashMap<>(); result.put("totalRows", rows.size()); result.put("successRows", accepted.size());
		result.put("failRows", errors.size()); result.put("errors", errors); result.put("plans", accepted); return result;
	}
	@Transactional(rollbackFor = Exception.class)
	@SuppressWarnings("unchecked")
	public Map<String, Object> commit(List<DutyExcel> rows, String month, Long organId, String mode, String filename, String hash, byte[] fileContent) {
		lock();


		Map<String, Object> result = preview(rows, month, organId, mode);
		DutyImportLog log = new DutyImportLog(); log.setImportBatch("ZB-" + UUID.randomUUID().toString().replace("-", "").substring(0, 28));
		log.setFileName(filename); log.setPlanMonth(month); log.setOrganId(organId); log.setTotalRows(rows.size());
		log.setSuccessRows((Integer) result.get("successRows")); log.setFailRows((Integer) result.get("failRows")); log.setImportMode(mode); log.setRollbackFlag(0);


		log.setImportUserId(AuthUtil.getUserId()); log.setImportTime(LocalDateTime.now());

		if (log.getFailRows() != 0) log.setSuccessRows(0);
		logs.insert(log); result.put("importBatch", log.getImportBatch());
		if (log.getFailRows() != 0) { result.put("committed", false); return result; }
		if ("1".equals(mode)) {
			for (DutyPlan old : active()) if (month.equals(old.getPlanMonth()) && (organId == null || organId.equals(old.getOrganId()))) {
				old.setDataStatus(0); old.setStatsFlag("0"); plans.updateById(old);
			}
		}
		for (DutyPlan plan : (List<DutyPlan>) result.get("plans")) { plan.setImportBatch(log.getImportBatch()); plans.insert(plan);  }
		rebuild(); result.put("committed", true); return result;
	}
	@Transactional(rollbackFor = Exception.class)
	public void rollback(Long id) {
		lock(); DutyImportLog log=logs.selectById(id);
		if(log==null || log.getRollbackFlag()==1) throw new ServiceException("批次不存在或已回滚"); scope.check(log.getOrganId());
		if("1".equals(log.getImportMode())) throw new ServiceException("现有表未记录被覆盖批次关系，不能安全恢复整月覆盖；请重新导入原计划");
		List<DutyPlan> imported=plans.selectList(base().eq(DutyPlan::getImportBatch,log.getImportBatch()));
		if(imported.stream().anyMatch(p->p.getDataStatus()!=1)) throw new ServiceException("批次已被后续操作修改，无法回滚");
		for(DutyPlan plan:imported){plan.setDataStatus(0);plan.setStatsFlag("0");plans.updateById(plan);}
		log.setRollbackFlag(1);logs.updateById(log);rebuild();
	}
	private void rebuild() {
		List<DutyPlan> all=plans.selectList(Wrappers.<DutyPlan>lambdaQuery().eq(DutyPlan::getDataStatus,1));
		Set<String> keys=new HashSet<>(); for(DutyPlan plan:plans.selectList(Wrappers.<DutyPlan>lambdaQuery())) keys.add(plan.getUserId()+":"+plan.getPlanMonth().substring(0,4));
		for(String key:keys){String[] parts=key.split(":");Long user=Long.valueOf(parts[0]);String year=parts[1];
			List<DutyStats> existing=statsMapper.selectList(Wrappers.<DutyStats>lambdaQuery().eq(DutyStats::getUserId,user).eq(DutyStats::getStatYear,year));
			DutyStats total=existing.isEmpty()?new DutyStats():existing.get(0);total.setUserId(user);total.setStatYear(year);
			List<DutyPlan> matched=all.stream().filter(p->user.equals(p.getUserId())&&p.getPlanMonth().startsWith(year)).toList();
			total.setTotalHours(matched.stream().map(DutyPlan::getDutyHours).reduce(BigDecimal.ZERO,BigDecimal::add)); total.setDutyCount(matched.size());
			if(existing.isEmpty())statsMapper.insert(total);else statsMapper.updateById(total);
		}
	}
	public List<DutyStats> statistics(String year) {
		if(year==null||!year.matches("\\d{4}"))throw new ServiceException("年度格式必须为yyyy");
		List<DutyStats> result=statsMapper.selectList(Wrappers.<DutyStats>lambdaQuery().eq(DutyStats::getStatYear,year));
		List<Long> organs=scope.organs();
		return result.stream().filter(total->{DutyEmployee employee=employees.selectById(total.getUserId());if(employee==null)return organs==null;total.setUserCode(employee.getUserCode());total.setUserName(employee.getUserName());total.setOrganId(employee.getOrganId());return organs==null||organs.contains(employee.getOrganId());}).toList();
	}
	public byte[] originalFile(Long id) { throw new ServiceException("现有表仅定义 FILE_URL，原文件存储尚未对接"); }
}


