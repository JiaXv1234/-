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

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springblade.core.cache.utils.CacheUtil;
import org.springblade.core.literule.engine.RuleEngineExecutor;
import org.springblade.core.literule.provider.LiteRuleResponse;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.mp.enums.StatusType;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.TenantId;
import org.springblade.core.tenant.TenantUtil;
import org.springblade.core.tool.constant.BladeConstant;
import org.springblade.core.tool.jackson.JsonUtil;
import org.springblade.core.tool.support.Kv;
import org.springblade.core.tool.utils.DesUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.system.mapper.TenantMapper;
import org.springblade.system.pojo.entity.*;
import org.springblade.system.rule.context.TenantContext;
import org.springblade.system.service.*;
import org.springblade.system.wrapper.TenantWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springblade.common.constant.TenantConstant.DES_KEY;
import static org.springblade.core.cache.constant.CacheConstant.SYS_CACHE;
import static org.springblade.system.cache.SysCache.TENANT_TENANT_ID;
import static org.springblade.system.rule.constant.TenantRuleConstant.TENANT_CHAIN_ID;

/**
 * 服务实现类
 *
 * @author Chill
 */
@Service
@AllArgsConstructor
public class TenantServiceImpl extends BaseServiceImpl<TenantMapper, Tenant> implements ITenantService {

	private final TenantId tenantIdGenerator;
	private final IRoleService roleService;
	private final IMenuService menuService;
	private final IDeptService deptService;
	private final IPostService postService;
	private final IRoleMenuService roleMenuService;
	private final IDictBizService dictBizService;
	private final IUserService userService;
	private final IUserDeptService userDeptService;
	private final RuleEngineExecutor ruleExecutor;

	@Override
	public Tenant getDetail(Tenant tenant) {
		QueryWrapper<Tenant> queryWrapper = Condition.getQueryWrapper(tenant);
		applyTenantScope(queryWrapper);
		return getOne(queryWrapper);
	}

	@Override
	public IPage<Tenant> selectPage(Map<String, Object> tenant, Query query) {
		TenantWrapper.build().entityQuery(tenant);
		QueryWrapper<Tenant> queryWrapper = Condition.getQueryWrapper(tenant, Tenant.class);
		applyTenantScope(queryWrapper);
		return page(Condition.getPage(query), queryWrapper);
	}

	@Override
	public List<Tenant> selectList(Tenant tenant) {
		QueryWrapper<Tenant> queryWrapper = Condition.getQueryWrapper(tenant);
		applyTenantScope(queryWrapper);
		return list(queryWrapper);
	}

	@Override
	public IPage<Tenant> selectTenantPage(IPage<Tenant> page, Tenant tenant) {
		QueryWrapper<Tenant> queryWrapper = Condition.getQueryWrapper(tenant);
		applyTenantScope(queryWrapper);
		return page(page, queryWrapper);
	}

	@Override
	public Tenant getByTenantId(String tenantId) {
		return getOne(Wrappers.<Tenant>query().lambda().eq(Tenant::getTenantId, tenantId));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean submitTenant(Tenant tenant) {
		if (Func.isEmpty(tenant.getId())) {
			LiteRuleResponse<TenantContext> resp = ruleExecutor.execute(
				TENANT_CHAIN_ID,
				TenantContext.builder()
					.tenantIdGenerator(tenantIdGenerator)
					.tenant(tenant)
					.menuService(menuService)
					.dictBizService(dictBizService)
					.tenantService(this)
					.build()
			);
			if (resp.isSuccess()) {
				TenantContext tenantContext = resp.getContext();
				Role role = tenantContext.getRole();
				roleService.save(role);

				Long roleId = role.getId();
				List<RoleMenu> roleMenuList = tenantContext.getRoleMenuList();
				roleMenuList.forEach(roleMenu -> roleMenu.setRoleId(roleId));
				roleMenuService.saveBatch(roleMenuList);

				Dept dept = tenantContext.getDept();
				deptService.save(dept);

				Post post = tenantContext.getPost();
				postService.save(post);

				List<DictBiz> dictBizList = tenantContext.getDictBizList();
				dictBizService.saveBatch(dictBizList);

				User user = tenantContext.getUser();
				user.setRoleId(String.valueOf(role.getId()));
				user.setDeptId(String.valueOf(dept.getId()));
				user.setPostId(String.valueOf(post.getId()));
				userService.submit(user);
			} else {
				throw new ServiceException("租户业务数据构建异常");
			}
		}
		CacheUtil.clear(SYS_CACHE, tenant.getTenantId());
		CacheUtil.evict(SYS_CACHE, TENANT_TENANT_ID, tenant.getTenantId(), Boolean.FALSE);
		return super.saveOrUpdate(tenant);
	}

	@Override
	public boolean setting(Integer accountNumber, Date expireTime, String ids) {
		List<Long> idList = Func.toLongList(ids);
		List<String> tenantIds = this.list(Wrappers.<Tenant>query().lambda().in(Tenant::getId, idList))
			.stream().map(tenant -> Func.toStr(tenant.getTenantId())).distinct().toList();
		tenantIds.forEach(tenantId -> {
			CacheUtil.clear(SYS_CACHE, tenantId);
			CacheUtil.evict(SYS_CACHE, TENANT_TENANT_ID, tenantId, Boolean.FALSE);
		});
		idList.forEach(id -> {
			Kv kv = Kv.create().set("accountNumber", accountNumber).set("expireTime", expireTime).set("id", id);
			String licenseKey = DesUtil.encryptToHex(JsonUtil.toJson(kv), DES_KEY);
			update(
				Wrappers.<Tenant>update().lambda()
					.set(Tenant::getAccountNumber, accountNumber)
					.set(Tenant::getExpireTime, expireTime)
					.set(Tenant::getLicenseKey, licenseKey)
					.eq(Tenant::getId, id)
			);
		});
		return true;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean recycleTenant(List<Long> ids) {
		int active = StatusType.ACTIVE.getType();
		int disabled = StatusType.DISABLED.getType();
		return executeTenantOperation(ids, TenantOperationType.RECYCLE,
			id -> this.changeStatus(id, disabled),
			(tenantIds, userIds) -> updateRelatedStatus(tenantIds, userIds, active, disabled),
			"删除租户失败!");
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean passTenant(List<Long> ids) {
		int active = StatusType.ACTIVE.getType();
		int disabled = StatusType.DISABLED.getType();
		return executeTenantOperation(ids, TenantOperationType.RESTORE,
			id -> this.changeStatus(id, active),
			(tenantIds, userIds) -> updateRelatedStatus(tenantIds, userIds, disabled, active),
			"恢复租户失败!");
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean removeTenant(List<Long> ids) {
		return executeTenantOperation(ids, TenantOperationType.REMOVE,
			this::deleteLogic,
			this::removeRelatedEntities,
			"删除租户失败!");
	}

	/**
	 * 租户批量操作统一模板
	 *
	 * @param ids               租户主键集合
	 * @param type              操作类型
	 * @param tenantAction      租户主表操作
	 * @param relatedDataAction 关联数据操作
	 * @param errorMessage      错误提示信息
	 * @return 操作结果
	 */
	private boolean executeTenantOperation(List<Long> ids, TenantOperationType type,
										   Function<List<Long>, Boolean> tenantAction,
										   BiConsumer<List<String>, List<Long>> relatedDataAction,
										   String errorMessage) {
		// 1. 获取租户ID列表
		List<String> tenantIds = this.list(Wrappers.<Tenant>query().lambda().in(Tenant::getId, ids))
			.stream().map(tenant -> Func.toStr(tenant.getTenantId())).distinct().collect(Collectors.toList());

		// 2. 回收和删除操作需要校验管理租户
		if (type != TenantOperationType.RESTORE && tenantIds.contains(BladeConstant.ADMIN_TENANT_ID)) {
			throw new ServiceException("不可删除管理租户!");
		}

		// 3. 执行租户主表操作
		if (!tenantAction.apply(ids)) {
			throw new ServiceException(errorMessage);
		}

		// 4. 获取用户ID列表
		var userWrapper = Wrappers.<User>query().lambda().in(User::getTenantId, tenantIds).select(User::getId);
		if (type.getStatusFilter() != null) {
			userWrapper.eq(User::getStatus, type.getStatusFilter());
		}
		List<Long> userIds = userService.list(userWrapper).stream().map(User::getId).collect(Collectors.toList());

		// 5. 执行关联数据操作
		relatedDataAction.accept(tenantIds, userIds);

		// 6. 清理缓存
		CacheUtil.clear(SYS_CACHE, tenantIds);
		return true;
	}

	/**
	 * 更新关联实体状态（用于回收/恢复）
	 *
	 * @param tenantIds  租户ID列表
	 * @param userIds    用户ID列表
	 * @param fromStatus 原状态
	 * @param toStatus   目标状态
	 */
	private void updateRelatedStatus(List<String> tenantIds, List<Long> userIds, int fromStatus, int toStatus) {
		TenantUtil.ignore(() -> {
			roleService.update(Wrappers.<Role>update().lambda().set(Role::getStatus, toStatus).eq(Role::getStatus, fromStatus).in(Role::getTenantId, tenantIds));
			deptService.update(Wrappers.<Dept>update().lambda().set(Dept::getStatus, toStatus).eq(Dept::getStatus, fromStatus).in(Dept::getTenantId, tenantIds));
			postService.update(Wrappers.<Post>update().lambda().set(Post::getStatus, toStatus).eq(Post::getStatus, fromStatus).in(Post::getTenantId, tenantIds));
			dictBizService.update(Wrappers.<DictBiz>update().lambda().set(DictBiz::getStatus, toStatus).eq(DictBiz::getStatus, fromStatus).in(DictBiz::getTenantId, tenantIds));
			if (!userIds.isEmpty()) {
				userService.update(Wrappers.<User>update().lambda().set(User::getStatus, toStatus).in(User::getId, userIds));
				userDeptService.update(Wrappers.<UserDept>update().lambda().set(UserDept::getStatus, toStatus).in(UserDept::getUserId, userIds));
				new UserOauth().update(Wrappers.<UserOauth>update().lambda().set(UserOauth::getStatus, toStatus).in(UserOauth::getUserId, userIds));
				new UserWeb().update(Wrappers.<UserWeb>update().lambda().set(UserWeb::getStatus, toStatus).in(UserWeb::getUserId, userIds));
				new UserApp().update(Wrappers.<UserApp>update().lambda().set(UserApp::getStatus, toStatus).in(UserApp::getUserId, userIds));
				new UserOther().update(Wrappers.<UserOther>update().lambda().set(UserOther::getStatus, toStatus).in(UserOther::getUserId, userIds));
			}
		});
	}

	/**
	 * 删除关联实体（用于彻底删除）
	 *
	 * @param tenantIds 租户ID列表
	 * @param userIds   用户ID列表
	 */
	private void removeRelatedEntities(List<String> tenantIds, List<Long> userIds) {
		TenantUtil.ignore(() -> {
			roleService.remove(Wrappers.<Role>query().lambda().in(Role::getTenantId, tenantIds));
			deptService.remove(Wrappers.<Dept>query().lambda().in(Dept::getTenantId, tenantIds));
			postService.remove(Wrappers.<Post>query().lambda().in(Post::getTenantId, tenantIds));
			dictBizService.remove(Wrappers.<DictBiz>query().lambda().in(DictBiz::getTenantId, tenantIds));
			if (!userIds.isEmpty()) {
				userService.removeByIds(userIds);
				userDeptService.remove(Wrappers.<UserDept>query().lambda().in(UserDept::getUserId, userIds));
				new UserOauth().delete(Wrappers.<UserOauth>query().lambda().in(UserOauth::getUserId, userIds));
				new UserWeb().delete(Wrappers.<UserWeb>query().lambda().in(UserWeb::getUserId, userIds));
				new UserApp().delete(Wrappers.<UserApp>query().lambda().in(UserApp::getUserId, userIds));
				new UserOther().delete(Wrappers.<UserOther>query().lambda().in(UserOther::getUserId, userIds));
			}
		});
	}

	/**
	 * 租户操作类型枚举
	 */
	@Getter
	@AllArgsConstructor
	private enum TenantOperationType {
		/**
		 * 回收至回收站
		 */
		RECYCLE(StatusType.ACTIVE.getType()),
		/**
		 * 从回收站恢复
		 */
		RESTORE(StatusType.DISABLED.getType()),
		/**
		 * 彻底删除
		 */
		REMOVE(null);

		private final Integer statusFilter;
	}

	/**
	 * 统一的租户范围过滤：超管放行，其他用户强制限定为当前会话租户。
	 */
	private void applyTenantScope(QueryWrapper<Tenant> queryWrapper) {
		if (!AuthUtil.isAdministrator()) {
			queryWrapper.lambda().eq(Tenant::getTenantId, AuthUtil.getTenantId());
		}
	}

}
