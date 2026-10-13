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
package org.springblade.attendance.pojo.entity;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
/** 说明书表结构映射。 @author Chill */
@Data
@TableName("tb_at_duty_stats")
public class DutyStats implements java.io.Serializable {
@TableId(type=IdType.ASSIGN_ID)
@JsonSerialize(using=ToStringSerializer.class) private Long id;
@JsonSerialize(using=ToStringSerializer.class) private Long userId;
private String statYear;
private java.math.BigDecimal totalHours;
private Integer dutyCount;
@TableField(exist=false) private String userCode;
@TableField(exist=false) private String userName;
@TableField(exist=false) private Long organId;
}

