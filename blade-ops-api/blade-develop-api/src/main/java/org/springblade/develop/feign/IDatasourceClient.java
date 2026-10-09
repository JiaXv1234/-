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
package org.springblade.develop.feign;

import org.springblade.core.launch.constant.AppConstant;
import org.springblade.core.tool.api.R;
import org.springblade.develop.pojo.entity.Datasource;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * IDatasourceClient
 *
 * @author Chill
 */
@FeignClient(
	value = AppConstant.APPLICATION_DEVELOP_NAME,
	fallback = IDatasourceClientFallback.class
)
public interface IDatasourceClient {
	String API_PREFIX = "/feign/client/datasource";
	String GET_DETAIL = API_PREFIX + "/detail";
	String GET_LIST = API_PREFIX + "/list";

	/**
	 * 获取数据源详情
	 *
	 * @param id 数据源ID
	 * @return R
	 */
	@GetMapping(GET_DETAIL)
	R<Datasource> detail(@RequestParam("id") Long id);

	/**
	 * 获取所有数据源列表
	 *
	 * @return R
	 */
	@GetMapping(GET_LIST)
	R<List<Datasource>> list();

}
