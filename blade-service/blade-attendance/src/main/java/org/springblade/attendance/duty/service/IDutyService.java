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
package org.springblade.attendance.duty.service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springblade.core.mp.support.Query;
import org.springblade.attendance.pojo.entity.*;
import org.springblade.attendance.pojo.dto.DutyDTO;
import org.springblade.attendance.duty.excel.DutyExcel;
import java.util.List;
import java.util.Map;
/**
 * 值班管理业务契约
 * @author Chill
 */
public interface IDutyService extends IService<DutyPlan> {
	IPage<DutyPlan> page(Query query, String month, String code);
	List<DutyPlan> export(String month);
	IPage<DutyImportLog> batches(Query query);
	IPage<DutyOutbox> tasks(Query query);
	List<DutyEmployee> employees();
	void bind(DutyEmployee input);
	DutyPlan save(DutyDTO dto);
	void cancel(Long id);
	Map<String, Object> preview(List<DutyExcel> rows, String month, Long organId, String mode);
	Map<String, Object> commit(List<DutyExcel> rows, String month, Long organId, String mode, String filename, String hash, byte[] fileContent);
	void rollback(Long id);
	List<DutyStats> statistics(String year);
	byte[] originalFile(Long id);
}

