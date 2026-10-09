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
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.TenantGuard;
import org.springblade.core.tool.constant.BladeConstant;
import org.springblade.core.tool.node.ForestNodeMerger;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.StringPool;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.system.cache.SysCache;
import org.springblade.system.mapper.DeptMapper;
import org.springblade.system.pojo.entity.Dept;
import org.springblade.system.pojo.entity.User;
import org.springblade.system.pojo.vo.DeptVO;
import org.springblade.system.pojo.vo.UserVO;
import org.springblade.system.service.IDeptService;
import org.springblade.system.service.IUserService;
import org.springblade.system.wrapper.DeptWrapper;
import org.springblade.system.wrapper.UserWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springblade.core.tenant.TenantGuard.EntityType.DEPT;
import static org.springblade.core.tenant.TenantGuard.EntityType.DEPT_PARENT;

/**
 * 服务实现类
 *
 * @author Chill
 */
@Service
@RequiredArgsConstructor
public class DeptServiceImpl extends ServiceImpl<DeptMapper, Dept> implements IDeptService {

	private static final String TENANT_ID = "tenantId";
	private static final String PARENT_ID = "parentId";

	private final IUserService userService;

	@Override
	public Dept getDetail(Dept dept) {
		QueryWrapper<Dept> queryWrapper = Condition.getQueryWrapper(dept);
		applyTenantScope(queryWrapper);
		return getOne(queryWrapper);
	}

	@Override
	public List<DeptVO> selectList(Map<String, Object> dept) {
		QueryWrapper<Dept> queryWrapper = Condition.getQueryWrapper(dept, Dept.class);
		applyTenantScope(queryWrapper);
		return DeptWrapper.build().listNodeVO(list(queryWrapper));
	}

	@Override
	public List<DeptVO> lazyList(String tenantId, Long parentId, Map<String, Object> param) {
		// 设置租户ID
		if (AuthUtil.isAdministrator()) {
			tenantId = StringPool.EMPTY;
		}
		String paramTenantId = Func.toStr(param.get(TENANT_ID));
		if (Func.isNotEmpty(paramTenantId) && AuthUtil.isAdministrator()) {
			tenantId = paramTenantId;
		}
		// 判断点击搜索但是没有查询条件的情况
		if (Func.isEmpty(param.get(PARENT_ID)) && param.size() == 1) {
			parentId = 0L;
		}
		// 判断数据权限控制,非超管角色只可看到本级及以下数据
		if (Func.toLong(parentId) == 0L && !AuthUtil.isAdministrator()) {
			Long deptId = Func.firstLong(AuthUtil.getDeptId());
			Dept dept = SysCache.getDept(deptId);
			if (dept.getParentId() != 0) {
				parentId = dept.getParentId();
			}
		}
		// 判断点击搜索带有查询条件的情况
		if (Func.isEmpty(param.get(PARENT_ID)) && param.size() > 1 && Func.toLong(parentId) == 0L) {
			parentId = null;
		}
		return baseMapper.lazyList(tenantId, parentId, param);
	}


	@Override
	public List<DeptVO> tree(String tenantId) {
		String resolvedTenantId = AuthUtil.isAdministrator()
			? Func.toStrWithEmpty(tenantId, AuthUtil.getTenantId())
			: AuthUtil.getTenantId();
		return ForestNodeMerger.merge(baseMapper.tree(resolvedTenantId));
	}

	@Override
	public List<DeptVO> lazyTree(String tenantId, Long parentId) {
		// 仅超管可指定任意 tenantId，且空值表示查询全部；其他用户强制使用当前会话租户
		String resolvedTenantId = AuthUtil.isAdministrator()
			? Func.toStr(tenantId, StringPool.EMPTY)
			: AuthUtil.getTenantId();
		return ForestNodeMerger.merge(baseMapper.lazyTree(resolvedTenantId, parentId));
	}

	@Override
	public String getDeptIds(String tenantId, String deptNames) {
		List<Dept> deptList = baseMapper.selectList(Wrappers.<Dept>query().lambda().eq(Dept::getTenantId, tenantId).in(Dept::getDeptName, Func.toStrList(deptNames)));
		if (deptList != null && deptList.size() > 0) {
			return deptList.stream().map(dept -> Func.toStr(dept.getId())).distinct().collect(Collectors.joining(","));
		}
		return null;
	}

	@Override
	public String getDeptIdsByFuzzy(String tenantId, String deptNames) {
		LambdaQueryWrapper<Dept> queryWrapper = Wrappers.<Dept>query().lambda().eq(Dept::getTenantId, tenantId);
		queryWrapper.and(wrapper -> {
			List<String> names = Func.toStrList(deptNames);
			names.forEach(name -> wrapper.like(Dept::getDeptName, name).or());
		});
		List<Dept> deptList = baseMapper.selectList(queryWrapper);
		if (deptList != null && deptList.size() > 0) {
			return deptList.stream().map(dept -> Func.toStr(dept.getId())).distinct().collect(Collectors.joining(","));
		}
		return null;
	}

	@Override
	public List<String> getDeptNames(String deptIds) {
		return baseMapper.getDeptNames(Func.toLongArray(deptIds));
	}

	@Override
	public List<Dept> getDeptChild(Long deptId) {
		return baseMapper.selectList(Wrappers.<Dept>query().lambda().like(Dept::getAncestors, deptId));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean removeDept(String ids) {
		List<Long> idList = Func.toLongList(ids);
		// 租户守卫：批量校验目标部门全部归属当前会话租户
		TenantGuard.verifyBatch(this, idList, DEPT);
		Long cnt = baseMapper.selectCount(Wrappers.<Dept>query().lambda().in(Dept::getParentId, idList));
		if (cnt > 0L) {
			throw new ServiceException("请先删除子节点!");
		}
		return removeByIds(idList);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean submit(Dept dept) {
		Long parentId = dept.getParentId();
		// 顶级语义：parentId 为 null 或 TOP_PARENT_ID (0L) 均视为顶级部门
		if (parentId == null || parentId.equals(BladeConstant.TOP_PARENT_ID)) {
			// 顶级部门：租户守卫统一处理新增 / 修改路径的租户绑定
			TenantGuard.bindTenant(this, dept, DEPT);
			dept.setParentId(BladeConstant.TOP_PARENT_ID);
			dept.setAncestors(String.valueOf(BladeConstant.TOP_PARENT_ID));
		} else {
			// 子部门：先校验自身归属（修改路径），再校验父节点归属并继承其 tenantId
			if (parentId.equals(dept.getId())) {
				throw new ServiceException("父节点不可选择自身!");
			}
			Dept self = Func.isNotEmpty(dept.getId())
				? TenantGuard.verify(this, dept.getId(), DEPT) : null;
			Dept parent = TenantGuard.verify(this, dept.getParentId(), DEPT_PARENT);
			if (parent == null) {
				throw new ServiceException("上级部门不存在!");
			}
			// 显式禁止跨租户迁移：保护现有子树的数据一致性，self 租户为空时放行并继承父租户
			if (self != null && Func.isNotEmpty(self.getTenantId()) && !self.getTenantId().equals(parent.getTenantId())) {
				throw new ServiceException("不允许跨租户迁移部门，请联系运维处理");
			}
			dept.setTenantId(parent.getTenantId());
			dept.setAncestors(parent.getAncestors() + StringPool.COMMA + dept.getParentId());
		}
		dept.setIsDeleted(BladeConstant.DB_NOT_DELETED);
		if (Func.isEmpty(dept.getTenantId())) {
			throw new ServiceException("租户ID不能为空");
		}
		return saveOrUpdate(dept);
	}

	@Override
	public List<DeptVO> search(String deptName, Long parentId) {
		String tenantId = AuthUtil.getTenantId();
		LambdaQueryWrapper<Dept> queryWrapper = Wrappers.<Dept>query().lambda();
		if (Func.isNotEmpty(tenantId)) {
			queryWrapper.eq(Dept::getTenantId, tenantId);
		}
		if (Func.isNotEmpty(deptName)) {
			queryWrapper.like(Dept::getDeptName, deptName);
		}
		if (Func.isNotEmpty(parentId) && parentId > 0L) {
			queryWrapper.eq(Dept::getParentId, parentId);
		}
		List<Dept> deptList = baseMapper.selectList(queryWrapper);
		return DeptWrapper.build().listNodeVO(deptList);
	}

	@Override
	public List<UserVO> deptLeaderInfo(Long deptId) {
		// 租户守卫：校验目标部门归属当前会话租户，禁止跨租户读取部门主管信息
		Dept dept = TenantGuard.verify(this, deptId, DEPT);
		if (Func.isEmpty(dept)) {
			throw new ServiceException("部门不存在!");
		}
		if (StringUtil.isBlank(dept.getLeaderId())) {
			return new ArrayList<>();
		}
		List<Long> leaderIds = Func.toLongList(dept.getLeaderId());
		LambdaQueryWrapper<User> queryWrapper = Wrappers.<User>query().lambda()
			.in(User::getId, leaderIds)
			.eq(User::getStatus, BladeConstant.DB_STATUS_NORMAL)
			.eq(User::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		// 主管列表同样限定本租户，避免污染的 leaderId 跨租户回显
		if (!AuthUtil.isAdministrator()) {
			queryWrapper.eq(User::getTenantId, AuthUtil.getTenantId());
		}
		List<User> userList = userService.list(queryWrapper);
		return UserWrapper.build().listVO(userList);
	}

	/**
	 * 统一的租户范围过滤：超管放行，其他用户强制限定为当前会话租户。
	 */
	private void applyTenantScope(QueryWrapper<Dept> queryWrapper) {
		if (!AuthUtil.isAdministrator()) {
			queryWrapper.lambda().eq(Dept::getTenantId, AuthUtil.getTenantId());
		}
	}

}
