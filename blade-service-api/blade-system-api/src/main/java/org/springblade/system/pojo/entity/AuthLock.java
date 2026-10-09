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
package org.springblade.system.pojo.entity;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import org.springblade.system.pojo.enums.AuthLockStatus;
import org.springblade.system.pojo.enums.AuthLockType;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import lombok.Data;

/**
 * 认证锁定记录实体类
 *
 * @author BladeX
 */
@Data
@TableName("blade_auth_lock")
public class AuthLock implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@JsonSerialize(using = ToStringSerializer.class)
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	private Long id;
	private String tenantId;
	private AuthLockType lockType;
	private AuthLockStatus lockStatus;
	private String lockTarget;
	private String remoteIp;
	private String userAgent;
	private Long userId;
	private Date lockBeginTime;
	private Date lockEndTime;
	private String lockReason;
	private String unlockReason;
	private Integer failCount;
	private Integer status;
	@TableLogic
	private Integer isDeleted;

}
