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
package org.springblade.system.wrapper;

import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.tool.sensitive.SensitiveUtil;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.system.pojo.entity.Param;
import org.springblade.system.pojo.vo.ParamVO;

import java.util.List;
import java.util.Objects;

/**
 * 参数配置包装类,返回视图层所需的字段
 *
 * @author Chill
 */
public class ParamWrapper extends BaseEntityWrapper<Param, ParamVO> {

	/**
	 * 敏感参数关键词
	 */
	private static final List<String> SENSITIVE_KEYWORDS = List.of("security", "auth", "password");

	public static ParamWrapper build() {
		return new ParamWrapper();
	}

	@Override
	public ParamVO entityVO(Param param) {
		ParamVO paramVO = Objects.requireNonNull(BeanUtil.copyProperties(param, ParamVO.class));
		// 对包含敏感关键词的参数值进行脱敏处理
		if (isSensitiveParam(paramVO.getParamKey())) {
			paramVO.setParamValue(SensitiveUtil.processWithRegex(paramVO.getParamValue(), ".+"));
		}
		return paramVO;
	}

	/**
	 * 判断参数键是否包含敏感关键词
	 */
	private boolean isSensitiveParam(String paramKey) {
		if (Func.isEmpty(paramKey)) {
			return false;
		}
		String lowerKey = paramKey.toLowerCase();
		return SENSITIVE_KEYWORDS.stream().anyMatch(lowerKey::contains);
	}

}
