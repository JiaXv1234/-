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

import org.springblade.core.datarecord.model.DataRecordInfo;
import org.springblade.core.launch.constant.AppConstant;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 数据审计Feign接口类
 *
 * @author BladeX
 */
@FeignClient(
	value = AppConstant.APPLICATION_SYSTEM_NAME,
	fallback = IDataRecordClientFallback.class
)
public interface IDataRecordClient {

	String API_PREFIX = "/feign/client/data-record";
	String SAVE_RECORD_DATA = API_PREFIX + "/save-record-data";

	/**
	 * 保存数据审计信息
	 *
	 * @param recordInfo    数据审计信息
	 * @param recordLevel   审计级别
	 * @param recordMessage 审计消息
	 * @return int
	 */
	@PostMapping(SAVE_RECORD_DATA)
	int saveRecordData(@RequestBody DataRecordInfo recordInfo, @RequestParam("recordLevel") String recordLevel, @RequestParam("recordMessage") String recordMessage);

}
