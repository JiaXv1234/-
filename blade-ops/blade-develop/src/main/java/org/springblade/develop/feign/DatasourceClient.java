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

import lombok.AllArgsConstructor;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.develop.pojo.entity.Datasource;
import org.springblade.develop.service.IDatasourceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 数据源远程调用服务
 *
 * @author Chill
 */
@NonDS
@RestController
@AllArgsConstructor
public class DatasourceClient implements IDatasourceClient {

	private final IDatasourceService datasourceService;

	/**
	 * 获取数据源详情
	 */
	@GetMapping(GET_DETAIL)
	public R<Datasource> detail(@RequestParam("id") Long id) {
		Datasource datasource = datasourceService.getById(id);
		if (datasource == null) {
			return R.fail("数据源不存在");
		}
		return R.data(datasource);
	}

	/**
	 * 获取所有数据源列表
	 */
	@GetMapping(GET_LIST)
	public R<List<Datasource>> list() {
		return R.data(datasourceService.list());
	}

}
