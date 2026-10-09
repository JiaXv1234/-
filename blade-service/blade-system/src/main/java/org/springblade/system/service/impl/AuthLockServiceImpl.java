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
package org.springblade.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.system.cache.ParamCache;
import org.springblade.system.cache.UserCache;
import org.springblade.system.mapper.AuthLockMapper;
import org.springblade.system.pojo.entity.AuthLock;
import org.springblade.system.pojo.entity.User;
import org.springblade.system.pojo.enums.AuthLockStatus;
import org.springblade.system.pojo.enums.AuthLockType;
import org.springblade.system.service.IAuthLockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static org.springblade.common.constant.ParamConstant.*;

/**
 * 认证锁定记录 服务实现类
 *
 * @author BladeX
 */
@Service
@AllArgsConstructor
public class AuthLockServiceImpl extends ServiceImpl<AuthLockMapper, AuthLock> implements IAuthLockService {

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void addAccountFailCount(String tenantId, String account, Long userId, String ip, String userAgent) {
		if (Func.hasEmpty(tenantId, account)) {
			return;
		}
		if (isAccountLocked(tenantId, account)) {
			return;
		}
		incrementFailCount(tenantId, account, userId, ip, userAgent);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void addIpFailCount(String tenantId, String ip, String userAgent) {
		if (Func.hasEmpty(tenantId, ip)) {
			return;
		}
		if (isIpLocked(tenantId, ip)) {
			return;
		}
		incrementFailCount(tenantId, ip, null, ip, userAgent);
	}

	@Override
	public boolean isAccountLocked(String tenantId, String account) {
		return hasActiveLock(tenantId, AuthLock::getLockTarget, account);
	}

	@Override
	public boolean isIpLocked(String tenantId, String ip) {
		return hasActiveLock(tenantId, AuthLock::getLockTarget, ip);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void releaseSystemLock(String tenantId, String account) {
		releaseByField(tenantId, AuthLock::getLockTarget, account);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void releaseSystemIpLock(String tenantId, String ip) {
		releaseByField(tenantId, AuthLock::getLockTarget, ip);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean manualLock(Long userId, String reason, Date beginTime, Date endTime) {
		User user = UserCache.getUser(userId);
		if (Func.isEmpty(user)) {
			throw new ServiceException("用户不存在!");
		}
		boolean hasManualLock = this.count(
			Wrappers.<AuthLock>lambdaQuery()
				.eq(AuthLock::getTenantId, user.getTenantId())
				.eq(AuthLock::getLockType, AuthLockType.MANUAL)
				.eq(AuthLock::getLockStatus, AuthLockStatus.LOCKED)
				.eq(AuthLock::getLockTarget, user.getAccount())
				.eq(AuthLock::getIsDeleted, 0)
				.and(m -> m
					.isNull(AuthLock::getLockEndTime)
					.or()
					.gt(AuthLock::getLockEndTime, DateUtil.now())
				)
		) > 0;
		if (hasManualLock) {
			throw new ServiceException("该用户已存在手动锁定记录，无需重复锁定!");
		}
		Date effectiveBeginTime = beginTime != null ? beginTime : DateUtil.now();
		if (endTime != null && endTime.before(effectiveBeginTime)) {
			throw new ServiceException("锁定结束时间不能早于" + (beginTime != null ? "开始时间" : "当前时间") + "!");
		}
		AuthLock lock = new AuthLock();
		lock.setTenantId(user.getTenantId());
		lock.setLockType(AuthLockType.MANUAL);
		lock.setLockStatus(AuthLockStatus.LOCKED);
		lock.setLockTarget(user.getAccount());
		lock.setUserId(user.getId());
		lock.setFailCount(0);
		lock.setLockBeginTime(effectiveBeginTime);
		lock.setLockEndTime(endTime);
		lock.setLockReason(reason);
		return this.save(lock);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean manualUnlock(Long lockId, String unlockReason) {
		AuthLock lock = this.getById(lockId);
		if (Func.isEmpty(lock)) {
			throw new ServiceException("锁定记录不存在!");
		}
		if (AuthLockStatus.UN_LOCKED.equals(lock.getLockStatus())) {
			throw new ServiceException("该记录已处于解锁状态!");
		}
		lock.setLockStatus(AuthLockStatus.UN_LOCKED);
		lock.setUnlockReason(unlockReason);
		return this.updateById(lock);
	}

	private void incrementFailCount(String tenantId, String target,
									Long userId, String ip, String userAgent) {
		Date now = DateUtil.now();
		int lockDuration = getLockDurationMinutes();
		Date windowStart = new Date(now.getTime() - lockDuration * 60 * 1000L);
		int threshold = getFailCountThreshold();

		// 构建滑动窗口内的计数记录查询条件
		LambdaQueryWrapper<AuthLock> windowQuery = Wrappers.<AuthLock>lambdaQuery()
			.eq(AuthLock::getTenantId, tenantId)
			.eq(AuthLock::getLockType, AuthLockType.SYSTEM)
			.eq(AuthLock::getLockStatus, AuthLockStatus.PRE_LOCKED)
			.eq(AuthLock::getLockTarget, target)
			.eq(AuthLock::getIsDeleted, 0)
			.gt(AuthLock::getLockBeginTime, windowStart);

		// 在滑动窗口内查找计数记录
		AuthLock counting = this.getOne(windowQuery.orderByDesc(AuthLock::getLockBeginTime), false);

		if (counting == null) {
			// 窗口内无记录，创建初始计数记录
			AuthLock lock = new AuthLock();
			lock.setTenantId(tenantId);
			lock.setLockType(AuthLockType.SYSTEM);
			lock.setLockTarget(target);
			lock.setRemoteIp(ip);
			lock.setUserAgent(userAgent);
			lock.setUserId(userId);
			lock.setFailCount(1);
			lock.setLockBeginTime(now);
			if (1 >= threshold) {
				lock.setLockStatus(AuthLockStatus.LOCKED);
				lock.setLockEndTime(lockExpireTime(now, lockDuration));
				lock.setLockReason(thresholdLockReason(threshold));
			} else {
				lock.setLockStatus(AuthLockStatus.PRE_LOCKED);
			}
			this.save(lock);
			// 并发防护：保存后检查是否因竞态产生了重复的 PRE_LOCKED 记录
			if (AuthLockStatus.PRE_LOCKED.equals(lock.getLockStatus())) {
				counting = deduplicateCountingRecords(tenantId, target, windowStart, lock);
			}
			// 无需继续递增（首次失败 fail_count 已为 1，或已合并到保留记录）
			if (counting == null) {
				return;
			}
		}

		// 递增计数器 + 更新元数据
		LambdaUpdateWrapper<AuthLock> increment = Wrappers.<AuthLock>lambdaUpdate()
			.set(AuthLock::getFailCount, counting.getFailCount() + 1)
			.eq(AuthLock::getId, counting.getId())
			.eq(AuthLock::getLockStatus, AuthLockStatus.PRE_LOCKED);
		if (Func.isNotEmpty(ip)) {
			increment.set(AuthLock::getRemoteIp, ip);
		}
		if (Func.isNotEmpty(userAgent)) {
			increment.set(AuthLock::getUserAgent, userAgent);
		}
		this.update(increment);
		// 达到阈值则条件升级（基于DB实际值判定，PRE_LOCKED卫兵防止并发双重升级）
		this.update(
			Wrappers.<AuthLock>lambdaUpdate()
				.set(AuthLock::getLockStatus, AuthLockStatus.LOCKED)
				.set(AuthLock::getLockEndTime, lockExpireTime(now, lockDuration))
				.set(AuthLock::getLockReason, thresholdLockReason(threshold))
				.eq(AuthLock::getId, counting.getId())
				.eq(AuthLock::getLockStatus, AuthLockStatus.PRE_LOCKED)
				.ge(AuthLock::getFailCount, threshold)
		);
	}

	/**
	 * 并发去重：检查窗口内是否存在多条 PRE_LOCKED 记录
	 */
	private AuthLock deduplicateCountingRecords(String tenantId, String target, Date windowStart, AuthLock self) {
		List<AuthLock> records = this.list(
			Wrappers.<AuthLock>lambdaQuery()
				.eq(AuthLock::getTenantId, tenantId)
				.eq(AuthLock::getLockType, AuthLockType.SYSTEM)
				.eq(AuthLock::getLockStatus, AuthLockStatus.PRE_LOCKED)
				.eq(AuthLock::getLockTarget, target)
				.eq(AuthLock::getIsDeleted, 0)
				.gt(AuthLock::getLockBeginTime, windowStart)
				.orderByAsc(AuthLock::getId)
		);
		if (records.size() <= 1) {
			return null;
		}
		AuthLock keeper = records.get(0);
		int totalCount = records.stream().mapToInt(AuthLock::getFailCount).sum();
		for (int i = 1; i < records.size(); i++) {
			this.removeById(records.get(i).getId());
		}
		this.update(
			Wrappers.<AuthLock>lambdaUpdate()
				.set(AuthLock::getFailCount, totalCount)
				.eq(AuthLock::getId, keeper.getId())
				.eq(AuthLock::getLockStatus, AuthLockStatus.PRE_LOCKED)
		);
		return self.getId().equals(keeper.getId()) ? null : keeper;
	}

	private boolean hasActiveLock(String tenantId, SFunction<AuthLock, ?> field, String value) {
		if (Func.hasEmpty(tenantId, value)) {
			return false;
		}
		Date now = DateUtil.now();
		return this.count(
			Wrappers.<AuthLock>lambdaQuery()
				.eq(AuthLock::getTenantId, tenantId)
				.eq(AuthLock::getLockStatus, AuthLockStatus.LOCKED)
				.eq(field, value)
				.eq(AuthLock::getIsDeleted, 0)
				.le(AuthLock::getLockBeginTime, now)
				.and(outer -> outer
					.and(sys -> sys
						.eq(AuthLock::getLockType, AuthLockType.SYSTEM)
						.isNotNull(AuthLock::getLockEndTime)
						.gt(AuthLock::getLockEndTime, now)
					)
					.or(manual -> manual
						.eq(AuthLock::getLockType, AuthLockType.MANUAL)
						.and(m -> m
							.isNull(AuthLock::getLockEndTime)
							.or()
							.gt(AuthLock::getLockEndTime, now)
						)
					)
				)
		) > 0;
	}

	private void releaseByField(String tenantId, SFunction<AuthLock, ?> field, String value) {
		if (Func.hasEmpty(tenantId, value)) {
			return;
		}
		this.update(
			Wrappers.<AuthLock>lambdaUpdate()
				.set(AuthLock::getLockStatus, AuthLockStatus.UN_LOCKED)
				.set(AuthLock::getUnlockReason, "用户认证成功，系统自动解锁")
				.eq(AuthLock::getTenantId, tenantId)
				.eq(AuthLock::getLockType, AuthLockType.SYSTEM)
				.in(AuthLock::getLockStatus, AuthLockStatus.PRE_LOCKED, AuthLockStatus.LOCKED)
				.eq(field, value)
				.eq(AuthLock::getIsDeleted, 0)
		);
	}

	private Date lockExpireTime(Date from, int durationMinutes) {
		return new Date(from.getTime() + durationMinutes * 60 * 1000L);
	}

	private String thresholdLockReason(int threshold) {
		return "连续认证失败次数达到阈值（" + threshold + "次），系统自动锁定";
	}

	private int getFailCountThreshold() {
		return Func.toInt(ParamCache.getValue(FAIL_COUNT_VALUE), FAIL_COUNT);
	}

	private int getLockDurationMinutes() {
		return Func.toInt(ParamCache.getValue(LOCK_DURATION_VALUE), LOCK_DURATION);
	}

}
