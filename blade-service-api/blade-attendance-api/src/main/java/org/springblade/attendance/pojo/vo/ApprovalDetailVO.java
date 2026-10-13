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
package org.springblade.attendance.pojo.vo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
/**
 * 审批中心视图数据。
 * @author Chill
 */
@Data
@Schema(description = "ApprovalDetailVO")
public class ApprovalDetailVO {
	private ApprovalItemVO item;
	private java.util.List<ApprovalNodeVO> nodes;
	private java.util.List<ApprovalCcVO> copies;
	private boolean canSeeChain;
	private boolean canHandle;
	private boolean finalStep;
	private boolean callbackReady;
	private String blockReason;
	@JsonSerialize(using = ToStringSerializer.class)
	private Long activeTaskId;
}
