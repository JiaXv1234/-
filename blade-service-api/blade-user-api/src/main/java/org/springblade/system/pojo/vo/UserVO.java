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
package org.springblade.system.pojo.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tool.jackson.BladeView;
import org.springblade.core.tool.jackson.Views;
import org.springblade.system.pojo.entity.User;

import java.io.Serial;

/**
 * 视图实体类
 *
 * @author Chill
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "UserVO对象")
public class UserVO extends User {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 主键ID
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	private Long id;

	/**
	 * 密码
	 */
	@JsonIgnore
	private String password;

	/**
	 * 租户名
	 */
	@BladeView(Views.Detail.class)
	private String tenantName;

	/**
	 * 用户平台名
	 */
	@BladeView(Views.Detail.class)
	private String userTypeName;

	/**
	 * 角色名
	 */
	@BladeView(Views.Summary.class)
	private String roleName;

	/**
	 * 部门名
	 */
	@BladeView(Views.Summary.class)
	private String deptName;

	/**
	 * 岗位名
	 */
	@BladeView(Views.Detail.class)
	private String postName;

	/**
	 * 性别
	 */
	@BladeView(Views.Detail.class)
	private String sexName;

	/**
	 * 拓展信息
	 */
	@BladeView(Views.Admin.class)
	private String userExt;
}
