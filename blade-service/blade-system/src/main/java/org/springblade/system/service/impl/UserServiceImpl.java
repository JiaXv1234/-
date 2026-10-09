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


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springblade.common.constant.ParamConstant;
import org.springblade.common.constant.TenantConstant;
import org.springblade.core.cache.utils.CacheUtil;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.mp.enums.StatusType;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.BladeTenantProperties;
import org.springblade.core.tenant.TenantGuard;
import org.springblade.core.tool.constant.BladeConstant;
import org.springblade.core.tool.constant.RoleConstant;
import org.springblade.core.tool.jackson.JsonUtil;
import org.springblade.core.tool.support.Kv;
import org.springblade.core.tool.utils.*;
import org.springblade.system.cache.DictCache;
import org.springblade.system.cache.ParamCache;
import org.springblade.system.cache.SysCache;
import org.springblade.system.cache.UserCache;
import org.springblade.system.excel.UserExcel;
import org.springblade.system.mapper.UserMapper;
import org.springblade.system.pojo.entity.*;
import org.springblade.system.pojo.enums.DictEnum;
import org.springblade.system.pojo.enums.UserType;
import org.springblade.system.pojo.vo.UserVO;
import org.springblade.system.service.IRoleService;
import org.springblade.system.service.IUserDeptService;
import org.springblade.system.service.IUserOauthService;
import org.springblade.system.service.IUserService;
import org.springblade.system.wrapper.UserWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.springblade.common.constant.ParamConstant.DEFAULT_PARAM_PASSWORD;
import static org.springblade.core.cache.constant.CacheConstant.USER_CACHE;
import static org.springblade.core.tenant.TenantGuard.EntityType.USER;

/**
 * 服务实现类
 *
 * @author Chill
 */
@Service
@AllArgsConstructor
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User> implements IUserService {
	private static final String GUEST_NAME = "guest";

	private final IUserDeptService userDeptService;
	private final IUserOauthService userOauthService;
	private final IRoleService roleService;
	private final BladeTenantProperties tenantProperties;

	@Override
	public User getDetail(User user) {
		QueryWrapper<User> queryWrapper = Condition.getQueryWrapper(user);
		applyTenantScope(queryWrapper);
		return getOne(queryWrapper);
	}

	@Override
	public IPage<UserVO> selectPage(Map<String, Object> user, Query query) {
		QueryWrapper<User> queryWrapper = Condition.getQueryWrapper(user, User.class);
		applyTenantScope(queryWrapper);
		return UserWrapper.build().pageVO(page(Condition.getPage(query), queryWrapper));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean submit(User user) {
		bindSessionTenant(user);
		return saveUser(user);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean updateUser(User user) {
		// 租户守卫：校验目标用户归属当前会话租户，并把已存在 tenantId 回写到入参防止篡改
		TenantGuard.bindTenant(this, user, USER);
		String tenantId = user.getTenantId();
		Long userCount = baseMapper.selectCount(
			Wrappers.<User>query().lambda()
				.eq(User::getTenantId, tenantId)
				.eq(User::getAccount, user.getAccount())
				.ne(User::getId, user.getId())
		);
		if (userCount > 0L) {
			throw new ServiceException(StringUtil.format("当前用户 [{}] 已存在!", user.getAccount()));
		}
		Long phoneCount = baseMapper.selectCount(
			Wrappers.<User>query().lambda()
				.eq(User::getTenantId, tenantId)
				.eq(User::getPhone, user.getPhone())
				.ne(User::getId, user.getId())
		);
		if (phoneCount > 0L && StringUtil.isNotBlank(user.getPhone())) {
			throw new ServiceException(StringUtil.format("当前手机 [{}] 已存在!", user.getPhone()));
		}
		CacheUtil.clear(USER_CACHE);
		return submitUserInfo(user) && submitUserDept(user);
	}

	@Override
	public boolean updateUserInfo(User user) {
		// 用户修改自身信息强制指定当前请求账号的ID
		user.setId(AuthUtil.getUserId());
		// 用户修改自身信息强制忽略角色、部门、岗位、账号等字段
		user.setRoleId(null);
		user.setDeptId(null);
		user.setPostId(null);
		user.setAccount(null);
		user.setIsLeader(null);
		CacheUtil.clear(USER_CACHE);
		return submitUserInfo(user);
	}

	private boolean submitUserInfo(User user) {
		Long phoneCount = baseMapper.selectCount(
			Wrappers.<User>query().lambda()
				.eq(User::getTenantId, AuthUtil.getTenantId())
				.eq(User::getPhone, user.getPhone())
				.ne(User::getId, user.getId())
		);
		if (phoneCount > 0L && StringUtil.isNotBlank(user.getPhone())) {
			throw new ServiceException(StringUtil.format("当前手机 [{}] 已存在!", user.getPhone()));
		}
		user.setPassword(null);
		return updateById(user);
	}

	private boolean submitUserDept(User user) {
		List<Long> deptIdList = Func.toLongList(user.getDeptId());
		List<UserDept> userDeptList = new ArrayList<>();
		deptIdList.forEach(deptId -> {
			UserDept userDept = new UserDept();
			userDept.setUserId(user.getId());
			userDept.setDeptId(deptId);
			userDeptList.add(userDept);
		});
		userDeptService.remove(Wrappers.<UserDept>update().lambda().eq(UserDept::getUserId, user.getId()));
		return userDeptService.saveBatch(userDeptList);
	}

	@Override
	public IPage<User> selectUserPage(IPage<User> page, User user, Long deptId, String tenantId) {
		List<Long> deptIdList = SysCache.getDeptChildIds(deptId);
		return page.setRecords(baseMapper.selectUserPage(page, user, deptIdList, tenantId));
	}

	@Override
	public IPage<UserVO> selectUserSearch(UserVO user, Query query) {
		LambdaQueryWrapper<User> queryWrapper = Wrappers.<User>query().lambda();
		String tenantId = AuthUtil.getTenantId();
		if (StringUtil.isNotBlank(tenantId)) {
			queryWrapper.eq(User::getTenantId, tenantId);
		}
		if (StringUtil.isNotBlank(user.getName())) {
			queryWrapper.like(User::getName, user.getName());
		}
		if (StringUtil.isNotBlank(user.getDeptName())) {
			String deptIds = SysCache.getDeptIdsByFuzzy(AuthUtil.getTenantId(), user.getDeptName());
			if (StringUtil.isNotBlank(deptIds)) {
				queryWrapper.and(wrapper -> {
					List<String> ids = Func.toStrList(deptIds);
					ids.forEach(id -> wrapper.like(User::getDeptId, id).or());
				});
			}
		}
		if (StringUtil.isNotBlank(user.getPostName())) {
			String postIds = SysCache.getPostIdsByFuzzy(AuthUtil.getTenantId(), user.getPostName());
			if (StringUtil.isNotBlank(postIds)) {
				queryWrapper.and(wrapper -> {
					List<String> ids = Func.toStrList(postIds);
					ids.forEach(id -> wrapper.like(User::getPostId, id).or());
				});
			}
		}
		IPage<User> pages = this.page(Condition.getPage(query), queryWrapper);
		return UserWrapper.build().pageVO(pages);
	}

	@Override
	public User userByAccount(String tenantId, String account) {
		return baseMapper.selectOne(Wrappers.<User>query().lambda().eq(User::getTenantId, tenantId).eq(User::getAccount, account).eq(User::getIsDeleted, BladeConstant.DB_NOT_DELETED));
	}

	@Override
	public UserInfo userInfo(Long userId) {
		User user = baseMapper.selectById(userId);
		return buildUserInfo(user);
	}

	@Override
	public UserInfo userInfo(Long userId, UserType userType) {
		User user = baseMapper.selectById(userId);
		return buildUserInfo(user, userType);
	}

	@Override
	public UserInfo userInfo(String tenantId, String account) {
		User user = baseMapper.getUser(tenantId, account);
		return buildUserInfo(user);
	}

	@Override
	public UserInfo userInfo(String tenantId, String account, UserType userType) {
		User user = baseMapper.getUser(tenantId, account);
		return buildUserInfo(user, userType);
	}

	@Override
	public UserInfo userInfoByPhone(String tenantId, String phone) {
		User user = baseMapper.getUserByPhone(tenantId, phone);
		return buildUserInfo(user);
	}

	@Override
	public UserInfo userInfoByPhone(String tenantId, String phone, UserType userType) {
		User user = baseMapper.getUserByPhone(tenantId, phone);
		return buildUserInfo(user, userType);
	}

	private UserInfo buildUserInfo(User user) {
		return buildUserInfo(user, UserType.WEB);
	}

	private UserInfo buildUserInfo(User user, UserType userType) {
		if (ObjectUtil.isEmpty(user)) {
			return null;
		}
		UserInfo userInfo = new UserInfo();
		userInfo.setUser(user);
		if (Func.isNotEmpty(user)) {
			List<String> roleAlias = roleService.getRoleAliases(user.getRoleId());
			if (!roleAlias.isEmpty()) {
				userInfo.setRoles(roleAlias);
			}
		}
		// 根据每个用户平台，建立对应的detail表，通过查询将结果集写入到detail字段
		Kv detail = Kv.create().set("type", userType.getName());
		if (userType == UserType.WEB) {
			UserWeb userWeb = new UserWeb();
			UserWeb query = userWeb.selectOne(Wrappers.<UserWeb>lambdaQuery().eq(UserWeb::getUserId, user.getId()));
			if (ObjectUtil.isNotEmpty(query)) {
				detail.set("ext", query.getUserExt());
			}
		} else if (userType == UserType.APP) {
			UserApp userApp = new UserApp();
			UserApp query = userApp.selectOne(Wrappers.<UserApp>lambdaQuery().eq(UserApp::getUserId, user.getId()));
			if (ObjectUtil.isNotEmpty(query)) {
				detail.set("ext", query.getUserExt());
			}
		} else {
			UserOther userOther = new UserOther();
			UserOther query = userOther.selectOne(Wrappers.<UserOther>lambdaQuery().eq(UserOther::getUserId, user.getId()));
			if (ObjectUtil.isNotEmpty(query)) {
				detail.set("ext", query.getUserExt());
			}
		}
		userInfo.setDetail(detail);
		return userInfo;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public UserInfo userInfo(UserOauth userOauth) {
		UserOauth uo = userOauthService.getOne(Wrappers.<UserOauth>query().lambda().eq(UserOauth::getUuid, userOauth.getUuid()).eq(UserOauth::getSource, userOauth.getSource()));
		UserInfo userInfo;
		if (Func.isNotEmpty(uo) && Func.isNotEmpty(uo.getUserId())) {
			userInfo = this.userInfo(uo.getUserId());
			userInfo.setOauthId(Func.toStr(uo.getId()));
		} else {
			userInfo = new UserInfo();
			if (Func.isEmpty(uo)) {
				userOauthService.save(userOauth);
				userInfo.setOauthId(Func.toStr(userOauth.getId()));
			} else {
				userInfo.setOauthId(Func.toStr(uo.getId()));
			}
			User user = new User();
			user.setAccount(userOauth.getUsername());
			user.setTenantId(userOauth.getTenantId());
			userInfo.setUser(user);
			userInfo.setRoles(Collections.singletonList(GUEST_NAME));
		}
		return userInfo;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean grant(String userIds, String roleIds) {
		List<Long> idList = Func.toLongList(userIds);
		// 租户守卫：批量校验目标用户全部归属当前会话租户
		TenantGuard.verifyBatch(this, idList, USER);
		User user = new User();
		user.setRoleId(roleIds);
		return this.update(user, Wrappers.<User>update().lambda().in(User::getId, idList));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean resetPassword(String userIds) {
		List<Long> idList = Func.toLongList(userIds);
		// 租户守卫：批量校验目标用户全部归属当前会话租户
		TenantGuard.verifyBatch(this, idList, USER);
		User user = new User();
		String initPassword = ParamCache.getValue(DEFAULT_PARAM_PASSWORD);// 获取默认密码配置
		user.setPassword(DigestUtil.encrypt(Func.toStr(initPassword, ParamConstant.DEFAULT_PASSWORD)));
		user.setUpdateTime(DateUtil.now());
		return this.update(user, Wrappers.<User>update().lambda().in(User::getId, idList));
	}

	@Override
	public boolean updatePassword(Long userId, String oldPassword, String newPassword, String newPassword1) {
		User user = getById(userId);
		if (!newPassword.equals(newPassword1)) {
			throw new ServiceException("请输入正确的确认密码!");
		}
		if (!user.getPassword().equals(DigestUtil.hex(oldPassword))) {
			throw new ServiceException("原密码不正确!");
		}
		return this.update(Wrappers.<User>update().lambda().set(User::getPassword, DigestUtil.hex(newPassword)).eq(User::getId, userId));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean removeUser(String userIds) {
		List<Long> idList = Func.toLongList(userIds);
		// 租户守卫：批量校验目标用户全部归属当前会话租户
		TenantGuard.verifyBatch(this, idList, USER);
		if (Func.contains(Func.toLongArray(userIds), AuthUtil.getUserId())) {
			throw new ServiceException("不能删除本账号!");
		}
		boolean tempUser = this.deleteLogic(Func.toLongList(userIds));
		boolean tempUserDept = userDeptService.remove(Wrappers.<UserDept>lambdaQuery().in(UserDept::getUserId, Func.toLongList(userIds)));
		if (tempUser && tempUserDept) {
			UserOauth userOauth = new UserOauth();
			userOauth.delete(Wrappers.<UserOauth>lambdaQuery().in(UserOauth::getUserId, Func.toLongList(userIds)));
			UserWeb userWeb = new UserWeb();
			userWeb.delete(Wrappers.<UserWeb>lambdaQuery().in(UserWeb::getUserId, Func.toLongList(userIds)));
			UserApp userApp = new UserApp();
			userApp.delete(Wrappers.<UserApp>lambdaQuery().in(UserApp::getUserId, Func.toLongList(userIds)));
			UserOther userOther = new UserOther();
			userOther.delete(Wrappers.<UserOther>lambdaQuery().in(UserOther::getUserId, Func.toLongList(userIds)));
			CacheUtil.clear(USER_CACHE);
			return true;
		} else {
			throw new ServiceException("删除用户失败!");
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void importUser(List<UserExcel> data, Boolean isCovered) {
		String currentTenantId = AuthUtil.getTenantId();
		boolean administrator = AuthUtil.isAdministrator();
		data.forEach(userExcel -> {
			// 1. 决策租户：仅超管 + Excel 显式指定时允许跨租户导入，否则强制使用当前会话租户
			String tenantId = (administrator && StringUtil.isNotBlank(userExcel.getTenantId()))
				? userExcel.getTenantId() : currentTenantId;
			// 2. 构建用户实体（部门/岗位/角色 ID 均在 tenantId 范围内解析，杜绝跨租户脏读）
			User user = buildImportUser(userExcel, tenantId);
			// 3. 覆盖模式：用同一 tenantId 定位同账号的存量用户
			if (Boolean.TRUE.equals(isCovered)) {
				User oldUser = UserCache.getUser(tenantId, userExcel.getAccount());
				if (oldUser != null && oldUser.getId() != null) {
					user.setId(oldUser.getId());
				}
			}
			// 4. 落库：submit / updateUser 内部已通过 TenantGuard.bindTenant 完成租户守卫，此处不重复调用
			if (user.getId() != null) {
				this.updateUser(user);
			} else {
				user.setPassword(ParamCache.getValue(DEFAULT_PARAM_PASSWORD));
				this.submit(user);
			}
		});
	}

	/**
	 * 基于 Excel 行构建待落库的 User 实体
	 * <p>
	 * 部门 / 岗位 / 角色 ID 均在指定 tenantId 范围内解析，确保导入数据的租户归属一致。
	 *
	 * @param userExcel Excel 行
	 * @param tenantId  本行有效租户 ID
	 * @return 已填充基础信息但未设置主键 / 密码的 User
	 */
	private User buildImportUser(UserExcel userExcel, String tenantId) {
		User user = Objects.requireNonNull(BeanUtil.copyProperties(userExcel, User.class));
		user.setTenantId(tenantId);
		user.setUserType(Func.toInt(DictCache.getKey(DictEnum.USER_TYPE, userExcel.getUserTypeName()), 1));
		user.setDeptId(Func.toStrWithEmpty(SysCache.getDeptIds(tenantId, userExcel.getDeptName()), StringPool.EMPTY));
		user.setPostId(Func.toStrWithEmpty(SysCache.getPostIds(tenantId, userExcel.getPostName()), StringPool.EMPTY));
		user.setRoleId(Func.toStrWithEmpty(SysCache.getRoleIds(tenantId, userExcel.getRoleName()), StringPool.EMPTY));
		return user;
	}

	@Override
	public List<UserExcel> exportUser(Wrapper<User> queryWrapper) {
		List<UserExcel> userList = baseMapper.exportUser(queryWrapper);
		userList.forEach(user -> {
			user.setUserTypeName(DictCache.getValue(DictEnum.USER_TYPE, user.getUserType()));
			user.setRoleName(StringUtil.join(SysCache.getRoleNames(user.getRoleId())));
			user.setDeptName(StringUtil.join(SysCache.getDeptNames(user.getDeptId())));
			user.setPostName(StringUtil.join(SysCache.getPostNames(user.getPostId())));
		});
		return userList;
	}

	@Override
	public List<UserExcel> exportUser(Map<String, Object> user) {
		QueryWrapper<User> queryWrapper = Condition.getQueryWrapper(user, User.class);
		applyTenantScope(queryWrapper);
		queryWrapper.lambda().eq(User::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		return exportUser(queryWrapper);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean registerGuest(User user, Long oauthId) {
		// 第一步：先取出 OAuth 上下文。oauthId 由 OAuth 第一步授权回调写回前端，
		// 是攻击者难以伪造的可信关联——以此为锚点反推 tenantId。
		UserOauth userOauth = userOauthService.getById(oauthId);
		if (userOauth == null || userOauth.getId() == null) {
			throw new ServiceException("第三方登陆信息错误!");
		}
		// ⚡ 优先采用 OAuth 上下文中已绑定的 tenantId（来自 OAuth 第一步可信源），
		// 强制覆盖前端传入的 user.tenantId，防止匿名接口被用于跨租户植入账户。
		// 仅当 OAuth 流程未绑定 tenantId 时才回退到入参（向后兼容历史 OAuth 流程）。
		if (Func.isNotEmpty(userOauth.getTenantId())) {
			user.setTenantId(userOauth.getTenantId());
		}
		Tenant tenant = SysCache.getTenant(user.getTenantId());
		if (tenant == null || tenant.getId() == null) {
			throw new ServiceException("租户信息错误!");
		}
		user.setRealName(user.getName());
		user.setAvatar(userOauth.getAvatar());
		user.setRoleId(StringPool.MINUS_ONE);
		user.setDeptId(StringPool.MINUS_ONE);
		user.setPostId(StringPool.MINUS_ONE);
		user.setUserType(UserType.OTHER.getCategory());
		user.setStatus(StatusType.IN_ACTIVE.getType());
		// 租户由 OAuth 锚点确定并校验，绕开会话守卫直接落库
		boolean userTemp = saveUser(user);
		userOauth.setUserId(user.getId());
		userOauth.setTenantId(user.getTenantId());
		boolean oauthTemp = userOauthService.updateById(userOauth);
		return (userTemp && oauthTemp);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean registerUser(User user) {
		Tenant tenant = SysCache.getTenant(user.getTenantId());
		if (tenant == null || tenant.getId() == null) {
			throw new ServiceException("租户信息错误!");
		}
		user.setRealName(user.getName());
		user.setRoleId(StringPool.MINUS_ONE);
		user.setDeptId(StringPool.MINUS_ONE);
		user.setPostId(StringPool.MINUS_ONE);
		user.setStatus(StatusType.IN_ACTIVE.getType());
		// 租户由注册请求指定并已校验，绕开会话守卫直接落库
		return saveUser(user);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean updatePlatform(Long userId, Integer userType, String userExt) {
		// 租户守卫：校验目标用户归属当前会话租户，禁止跨租户修改平台扩展信息
		User user = TenantGuard.verify(this, userId, USER);
		if (Func.isEmpty(user)) {
			throw new ServiceException("用户不存在!");
		}
		if (userType.equals(UserType.WEB.getCategory())) {
			UserWeb userWeb = new UserWeb();
			UserWeb query = userWeb.selectOne(Wrappers.<UserWeb>lambdaQuery().eq(UserWeb::getUserId, userId));
			if (ObjectUtil.isNotEmpty(query)) {
				userWeb.setId(query.getId());
			}
			userWeb.setUserId(userId);
			userWeb.setUserExt(userExt);
			return userWeb.insertOrUpdate();
		} else if (userType.equals(UserType.APP.getCategory())) {
			UserApp userApp = new UserApp();
			UserApp query = userApp.selectOne(Wrappers.<UserApp>lambdaQuery().eq(UserApp::getUserId, userId));
			if (ObjectUtil.isNotEmpty(query)) {
				userApp.setId(query.getId());
			}
			userApp.setUserId(userId);
			userApp.setUserExt(userExt);
			return userApp.insertOrUpdate();
		} else {
			UserOther userOther = new UserOther();
			UserOther query = userOther.selectOne(Wrappers.<UserOther>lambdaQuery().eq(UserOther::getUserId, userId));
			if (ObjectUtil.isNotEmpty(query)) {
				userOther.setId(query.getId());
			}
			userOther.setUserId(userId);
			userOther.setUserExt(userExt);
			return userOther.insertOrUpdate();
		}
	}

	@Override
	public UserVO platformDetail(User user) {
		QueryWrapper<User> queryWrapper = Condition.getQueryWrapper(user);
		applyTenantScope(queryWrapper);
		User detail = baseMapper.selectOne(queryWrapper);
		if (Func.isEmpty(detail)) {
			return null;
		}
		UserVO userVO = UserWrapper.build().entityVO(detail);
		if (userVO.getUserType().equals(UserType.WEB.getCategory())) {
			UserWeb userWeb = new UserWeb();
			UserWeb query = userWeb.selectOne(Wrappers.<UserWeb>lambdaQuery().eq(UserWeb::getUserId, user.getId()));
			if (ObjectUtil.isNotEmpty(query)) {
				userVO.setUserExt(query.getUserExt());
			}
		} else if (userVO.getUserType().equals(UserType.APP.getCategory())) {
			UserApp userApp = new UserApp();
			UserApp query = userApp.selectOne(Wrappers.<UserApp>lambdaQuery().eq(UserApp::getUserId, user.getId()));
			if (ObjectUtil.isNotEmpty(query)) {
				userVO.setUserExt(query.getUserExt());
			}
		} else {
			UserOther userOther = new UserOther();
			UserOther query = userOther.selectOne(Wrappers.<UserOther>lambdaQuery().eq(UserOther::getUserId, user.getId()));
			if (ObjectUtil.isNotEmpty(query)) {
				userVO.setUserExt(query.getUserExt());
			}
		}
		return userVO;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean auditPass(String userIds) {
		if (StringUtil.isBlank(userIds)) {
			throw new ServiceException("请至少选择一个用户!");
		}
		List<Long> idList = Func.toLongList(userIds);
		// 租户守卫：批量校验目标用户全部归属当前会话租户，复用返回列表避免二次查询
		List<User> userList = TenantGuard.verifyBatch(this, idList, USER);
		userList.forEach(user -> {
			boolean roleTemp = StringUtil.isBlank(user.getRoleId()) || user.getRoleId().equals(StringPool.MINUS_ONE);
			boolean deptTemp = StringUtil.isBlank(user.getDeptId()) || user.getDeptId().equals(StringPool.MINUS_ONE);
			boolean postTemp = StringUtil.isBlank(user.getPostId()) || user.getPostId().equals(StringPool.MINUS_ONE);
			if (roleTemp || deptTemp || postTemp) {
				throw new ServiceException("请先给账号 [" + user.getAccount() + "] 分配角色、部门、岗位后再审批通过!");
			}
		});
		return changeStatus(idList, StatusType.ACTIVE.getType());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean auditRefuse(String userIds) {
		if (StringUtil.isBlank(userIds)) {
			throw new ServiceException("请至少选择一个用户!");
		}
		List<Long> idList = Func.toLongList(userIds);
		// 租户守卫：批量校验目标用户全部归属当前会话租户
		TenantGuard.verifyBatch(this, idList, USER);
		return changeStatus(idList, StatusType.DISABLED.getType());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean setLeader(Long userId) {
		// 租户守卫：校验目标用户归属当前会话租户，禁止跨租户设置主管
		User user = TenantGuard.verify(this, userId, USER);
		if (Func.isEmpty(user)) {
			throw new ServiceException("用户不存在!");
		}
		Integer isLeader = user.getIsLeader();
		return this.update(Wrappers.<User>update().lambda().set(
			User::getIsLeader, isLeader == 0 ? BladeConstant.DB_STATUS_1 : BladeConstant.DB_STATUS_0
		).eq(User::getId, userId));
	}

	@Override
	public List<UserVO> leaderInfo(Long userId) {
		// 租户守卫：校验目标用户归属当前会话租户，禁止跨租户读取主管信息
		User user = TenantGuard.verify(this, userId, USER);
		if (Func.isEmpty(user)) {
			throw new ServiceException("用户不存在!");
		}
		if (StringUtil.isBlank(user.getLeaderId())) {
			return new ArrayList<>();
		}
		List<Long> leaderIds = Func.toLongList(user.getLeaderId());
		// 主管列表同样限定本租户，避免污染的 leaderId 跨租户回显
		LambdaQueryWrapper<User> queryWrapper = Wrappers.<User>query().lambda().in(User::getId, leaderIds);
		if (!AuthUtil.isAdministrator()) {
			queryWrapper.eq(User::getTenantId, AuthUtil.getTenantId());
		}
		return UserWrapper.build().listVO(this.list(queryWrapper));
	}

	@Override
	public List<UserVO> leaderList(String tenantId, String realName) {
		// 仅超管可指定任意 tenantId，其他用户强制限定当前会话租户
		String resolvedTenantId = AuthUtil.isAdministrator()
			? Func.toStrWithEmpty(tenantId, AuthUtil.getTenantId())
			: AuthUtil.getTenantId();
		LambdaQueryWrapper<User> queryWrapper = Wrappers.<User>query().lambda()
			.select(User::getId, User::getRealName)
			.like(StringUtil.isNoneBlank(realName), User::getRealName, realName)
			.eq(User::getTenantId, resolvedTenantId)
			.eq(User::getIsLeader, BladeConstant.DB_STATUS_1)
			.eq(User::getStatus, BladeConstant.DB_STATUS_NORMAL)
			.eq(User::getIsDeleted, BladeConstant.DB_NOT_DELETED);
		return UserWrapper.build().listVO(this.list(queryWrapper));
	}

	@Override
	public IPage<UserVO> userPage(IPage<User> page, String account, String name) {
		// 查询管理员角色ID列表
		List<Long> adminRoleIds = roleService.list(
			Wrappers.<Role>lambdaQuery().in(Role::getRoleAlias, RoleConstant.ADMINISTRATOR, RoleConstant.ADMIN)
		).stream().map(Role::getId).toList();
		// 构建用户分页查询（非超管自动隔离租户）
		LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery();
		if (!AuthUtil.isAdministrator()) {
			queryWrapper.eq(User::getTenantId, AuthUtil.getTenantId());
		}
		// 排除包含管理员角色的用户
		for (Long adminRoleId : adminRoleIds) {
			queryWrapper.notLike(User::getRoleId, adminRoleId.toString());
		}
		// 模糊查询账号
		if (Func.isNotBlank(account)) {
			queryWrapper.like(User::getAccount, account);
		}
		// 模糊查询姓名
		if (Func.isNotBlank(name)) {
			queryWrapper.like(User::getName, name);
		}
		// 按创建时间降序排序
		queryWrapper.orderByDesc(User::getCreateTime);
		return UserWrapper.build().pageVO(this.page(page, queryWrapper));
	}

	/**
	 * 会话租户守卫（仅用于后台 CRUD 写入）
	 * <p>
	 * 非超管强制写入当前会话 tenantId 防注入；空租户时超管兜底为管理租户、非超管拒绝。
	 * 依赖登录会话，不适用于注册等匿名链路。
	 *
	 * @param user 待落库用户
	 */
	private void bindSessionTenant(User user) {
		TenantGuard.bindTenant(this, user, USER);
		if (StringUtil.isBlank(user.getTenantId())) {
			if (AuthUtil.isAdministrator()) {
				user.setTenantId(BladeConstant.ADMIN_TENANT_ID);
			} else {
				throw new ServiceException("会话租户不可识别，拒绝创建用户");
			}
		}
	}

	/**
	 * 用户落库核心：额度校验、密码加密、账号 / 手机查重、缓存清理与主从表保存
	 * <p>
	 * 不含会话守卫，租户归属由调用方保证：{@link #submit} 经会话推断，
	 * 注册链路以外部已校验的 tenantId 落库。调用前须确保 tenantId 合法。
	 *
	 * @param user 待落库用户
	 * @return 是否成功
	 */
	private boolean saveUser(User user) {
		String tenantId = user.getTenantId();
		Tenant tenant = SysCache.getTenant(tenantId);
		if (Func.isNotEmpty(tenant)) {
			Integer accountNumber = tenant.getAccountNumber();
			if (tenantProperties.getLicense()) {
				String licenseKey = tenant.getLicenseKey();
				String decrypt = DesUtil.decryptFormHex(licenseKey, TenantConstant.DES_KEY);
				accountNumber = JsonUtil.parse(decrypt, Tenant.class).getAccountNumber();
			}
			Long tenantCount = baseMapper.selectCount(Wrappers.<User>query().lambda().eq(User::getTenantId, tenantId));
			if (accountNumber != null && accountNumber > 0 && accountNumber <= tenantCount) {
				throw new ServiceException("当前租户已到最大账号额度!");
			}
		}
		if (Func.isNotEmpty(user.getPassword())) {
			user.setPassword(DigestUtil.encrypt(user.getPassword()));
		}
		Long userCount = baseMapper.selectCount(Wrappers.<User>query().lambda().eq(User::getTenantId, tenantId).eq(User::getAccount, user.getAccount()));
		if (userCount > 0L && Func.isEmpty(user.getId())) {
			throw new ServiceException(StringUtil.format("当前用户 [{}] 已存在!", user.getAccount()));
		}
		Long phoneCount = baseMapper.selectCount(Wrappers.<User>query().lambda().eq(User::getTenantId, tenantId).eq(User::getPhone, user.getPhone()));
		if (phoneCount > 0L && StringUtil.isNotBlank(user.getPhone())) {
			throw new ServiceException(StringUtil.format("当前手机 [{}] 已存在!", user.getPhone()));
		}
		CacheUtil.clear(USER_CACHE);
		return save(user) && submitUserDept(user);
	}

	/**
	 * 统一的租户范围过滤：超管放行，其他用户强制限定为当前会话租户。
	 */
	private void applyTenantScope(QueryWrapper<User> queryWrapper) {
		if (!AuthUtil.isAdministrator()) {
			queryWrapper.lambda().eq(User::getTenantId, AuthUtil.getTenantId());
		}
	}

}
