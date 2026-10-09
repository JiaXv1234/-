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
package org.springblade.system.service;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseService;
import org.springblade.core.mp.support.Query;
import org.springblade.system.pojo.entity.User;
import org.springblade.system.pojo.entity.UserInfo;
import org.springblade.system.pojo.entity.UserOauth;
import org.springblade.system.pojo.enums.UserType;
import org.springblade.system.excel.UserExcel;
import org.springblade.system.pojo.vo.UserVO;

import java.util.List;
import java.util.Map;

/**
 * 服务类
 *
 * @author Chill
 */
public interface IUserService extends BaseService<User> {

	/**
	 * 详情查询（含超管判定 + 当前租户隔离）
	 *
	 * @param user 查询条件
	 * @return 单条记录，非超管时仅命中当前会话租户
	 */
	User getDetail(User user);

	/**
	 * 分页列表（含超管判定 + 当前租户隔离）
	 * <p>
	 * 仅超级管理员可指定任意 tenantId 查询；其他用户传入的 tenantId 一律被忽略，
	 * 强制使用当前会话租户。
	 *
	 * @param user  查询条件 Map
	 * @param query 分页参数
	 * @return 分页结果 VO，非超管时仅命中当前会话租户
	 */
	IPage<UserVO> selectPage(Map<String, Object> user, Query query);

	/**
	 * 新增用户
	 * <p>
	 * 经会话租户守卫推断并校验 tenantId 后落库，含额度校验、密码加密与账号 / 手机查重。
	 *
	 * @param user 用户实体
	 * @return 是否成功
	 */
	boolean submit(User user);

	/**
	 * 修改用户（租户守卫校验用户归属，含账号 / 手机查重）
	 *
	 * @param user 用户实体
	 * @return 是否成功
	 */
	boolean updateUser(User user);

	/**
	 * 修改用户基本信息（仅限当前登录用户自身，忽略角色 / 部门 / 岗位 / 账号等敏感字段）
	 *
	 * @param user 用户实体
	 * @return 是否成功
	 */
	boolean updateUserInfo(User user);

	/**
	 * 自定义用户分页查询
	 *
	 * @param page     分页参数
	 * @param user     查询条件
	 * @param deptId   部门ID（含子部门）
	 * @param tenantId 租户ID（超管为空时查询全部）
	 * @return 用户分页结果
	 */
	IPage<User> selectUserPage(IPage<User> page, User user, Long deptId, String tenantId);

	/**
	 * 自定义用户搜索分页（按姓名 / 部门 / 岗位模糊匹配，限定当前会话租户）
	 *
	 * @param user  查询条件
	 * @param query 分页参数
	 * @return 用户分页结果 VO
	 */
	IPage<UserVO> selectUserSearch(UserVO user, Query query);


	/**
	 * 根据用户ID获取用户信息（默认 WEB 平台）
	 *
	 * @param userId 用户ID
	 * @return 用户信息（含角色与平台扩展）
	 */
	UserInfo userInfo(Long userId);

	/**
	 * 根据用户ID与平台类型获取用户信息
	 *
	 * @param userId   用户ID
	 * @param userType 平台类型
	 * @return 用户信息（含角色与平台扩展）
	 */
	UserInfo userInfo(Long userId, UserType userType);

	/**
	 * 根据租户与账号获取用户信息（默认 WEB 平台）
	 *
	 * @param tenantId 租户ID
	 * @param account  账号
	 * @return 用户信息（含角色与平台扩展）
	 */
	UserInfo userInfo(String tenantId, String account);

	/**
	 * 根据租户、账号与平台类型获取用户信息
	 *
	 * @param tenantId 租户ID
	 * @param account  账号
	 * @param userType 平台类型
	 * @return 用户信息（含角色与平台扩展）
	 */
	UserInfo userInfo(String tenantId, String account, UserType userType);

	/**
	 * 根据租户与手机号获取用户信息（默认 WEB 平台）
	 *
	 * @param tenantId 租户ID
	 * @param phone    手机号
	 * @return 用户信息（含角色与平台扩展）
	 */
	UserInfo userInfoByPhone(String tenantId, String phone);

	/**
	 * 根据租户、手机号与平台类型获取用户信息
	 *
	 * @param tenantId 租户ID
	 * @param phone    手机号
	 * @param userType 平台类型
	 * @return 用户信息（含角色与平台扩展）
	 */
	UserInfo userInfoByPhone(String tenantId, String phone, UserType userType);

	/**
	 * 根据第三方授权信息获取用户信息（未绑定时返回访客信息并落库授权记录）
	 *
	 * @param userOauth 第三方授权信息
	 * @return 用户信息
	 */
	UserInfo userInfo(UserOauth userOauth);

	/**
	 * 根据租户与账号获取用户
	 *
	 * @param tenantId 租户ID
	 * @param account  账号
	 * @return 用户实体，不存在时返回 null
	 */
	User userByAccount(String tenantId, String account);

	/**
	 * 给用户分配角色（批量租户守卫校验）
	 *
	 * @param userIds 用户ID集合（逗号分隔）
	 * @param roleIds 角色ID集合（逗号分隔）
	 * @return 是否成功
	 */
	boolean grant(String userIds, String roleIds);

	/**
	 * 重置用户密码为系统默认密码（批量租户守卫校验）
	 *
	 * @param userIds 用户ID集合（逗号分隔）
	 * @return 是否成功
	 */
	boolean resetPassword(String userIds);

	/**
	 * 修改当前用户密码
	 *
	 * @param userId       用户ID
	 * @param oldPassword  原密码
	 * @param newPassword  新密码
	 * @param newPassword1 确认新密码
	 * @return 是否成功
	 */
	boolean updatePassword(Long userId, String oldPassword, String newPassword, String newPassword1);

	/**
	 * 删除用户（批量租户守卫校验，禁止删除本账号，并清理关联平台数据）
	 *
	 * @param userIds 用户ID集合（逗号分隔）
	 * @return 是否成功
	 */
	boolean removeUser(String userIds);

	/**
	 * 导入用户数据（仅超管 + Excel 显式指定时允许跨租户，否则归属当前会话租户）
	 *
	 * @param data      Excel 用户数据
	 * @param isCovered 是否覆盖同账号的存量用户
	 */
	void importUser(List<UserExcel> data, Boolean isCovered);

	/**
	 * 根据查询条件导出用户数据（含字典翻译与角色 / 部门 / 岗位名称回填）
	 *
	 * @param queryWrapper 查询条件
	 * @return 导出数据列表
	 */
	List<UserExcel> exportUser(Wrapper<User> queryWrapper);

	/**
	 * 获取导出用户数据（含超管判定 + 当前租户隔离）
	 * <p>
	 * 仅超级管理员可导出全量数据；其他用户被强制限定在自身租户范围内。
	 * 自动叠加未删除条件。
	 *
	 * @param user 查询条件 Map
	 * @return 导出数据，非超管时仅命中当前会话租户
	 */
	List<UserExcel> exportUser(Map<String, Object> user);

	/**
	 * 第三方注册用户（以 OAuth 授权记录为锚点确定并校验租户后落库）
	 *
	 * @param user    用户实体
	 * @param oauthId 第三方授权记录ID
	 * @return 是否成功
	 */
	boolean registerGuest(User user, Long oauthId);

	/**
	 * 注册用户（按入参指定并已校验的租户落库）
	 *
	 * @param user 用户实体
	 * @return 是否成功
	 */
	boolean registerUser(User user);

	/**
	 * 配置用户平台扩展信息（租户守卫校验用户归属）
	 *
	 * @param userId   用户ID
	 * @param userType 平台类型
	 * @param userExt  平台扩展信息
	 * @return 是否成功
	 */
	boolean updatePlatform(Long userId, Integer userType, String userExt);

	/**
	 * 查询用户平台详情（含超管判定 + 当前租户隔离）
	 *
	 * @param user 查询条件
	 * @return 用户详情 VO，非超管时仅命中当前会话租户
	 */
	UserVO platformDetail(User user);

	/**
	 * 审核通过（批量租户守卫校验，要求已分配角色 / 部门 / 岗位）
	 *
	 * @param userIds 用户ID集合（逗号分隔）
	 * @return 是否成功
	 */
	boolean auditPass(String userIds);

	/**
	 * 审核拒绝（批量租户守卫校验，置为禁用状态）
	 *
	 * @param userIds 用户ID集合（逗号分隔）
	 * @return 是否成功
	 */
	boolean auditRefuse(String userIds);

	/**
	 * 设置用户为主管（租户守卫校验用户归属）
	 *
	 * @param userId 用户id
	 * @return 是否成功
	 */
	boolean setLeader(Long userId);

	/**
	 * 获取用户的主管信息（租户守卫校验用户归属，主管列表限定本租户）
	 *
	 * @param userId 用户id
	 * @return 主管用户列表 VO
	 */
	List<UserVO> leaderInfo(Long userId);

	/**
	 * 获取主管列表（含超管判定 + 当前租户隔离）
	 *
	 * @param tenantId 租户编号（仅超管生效）
	 * @param realName 用户姓名（模糊匹配）
	 * @return 主管用户列表 VO
	 */
	List<UserVO> leaderList(String tenantId, String realName);

	/**
	 * 分页查询非管理员角色用户（用于 API Key 绑定选择）
	 * <p>
	 * 内部自动处理租户隔离（非超管仅查看本租户用户）和管理员角色排除
	 *
	 * @param page    分页参数
	 * @param account 账号（模糊搜索）
	 * @param name    姓名（模糊搜索）
	 * @return 用户分页（UserVO 包含角色、部门、岗位名称）
	 */
	IPage<UserVO> userPage(IPage<User> page, String account, String name);
}
