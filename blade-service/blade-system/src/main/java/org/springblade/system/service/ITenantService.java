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

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseService;
import org.springblade.core.mp.support.Query;
import org.springblade.system.pojo.entity.Tenant;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 服务类
 *
 * @author Chill
 */
public interface ITenantService extends BaseService<Tenant> {

	/**
	 * 详情查询（含超管判定 + 当前租户隔离）
	 *
	 * @param tenant 查询条件
	 * @return 单条记录，非超管时仅命中当前会话租户
	 */
	Tenant getDetail(Tenant tenant);

	/**
	 * 分页列表（含超管判定 + 当前租户隔离）
	 *
	 * @param tenant 查询条件 Map
	 * @param query  分页参数
	 * @return 分页结果，非超管时仅命中当前会话租户
	 */
	IPage<Tenant> selectPage(Map<String, Object> tenant, Query query);

	/**
	 * 下拉数据源（含超管判定 + 当前租户隔离）
	 *
	 * @param tenant 查询条件
	 * @return 列表，非超管时仅命中当前会话租户
	 */
	List<Tenant> selectList(Tenant tenant);

	/**
	 * 自定义分页查询（含超管判定 + 当前租户隔离）
	 *
	 * @param page   分页参数
	 * @param tenant 查询条件
	 * @return 分页结果，非超管时仅命中当前会话租户
	 */
	IPage<Tenant> selectTenantPage(IPage<Tenant> page, Tenant tenant);

	/**
	 * 根据租户编号获取租户实体
	 *
	 * @param tenantId 租户编号
	 * @return 租户实体，不存在时返回 null
	 */
	Tenant getByTenantId(String tenantId);

	/**
	 * 新增或修改租户（新增时通过规则引擎初始化角色、菜单、部门、岗位、字典等基础数据）
	 *
	 * @param tenant 租户实体
	 * @return 是否成功
	 */
	boolean submitTenant(Tenant tenant);

	/**
	 * 配置租户授权（设置账号额度、过期时间并生成授权码）
	 *
	 * @param accountNumber 账号额度
	 * @param expireTime    过期时间
	 * @param ids           租户主键集合
	 * @return 是否成功
	 */
	boolean setting(Integer accountNumber, Date expireTime, String ids);

	/**
	 * 删除租户至回收站（同步停用其关联的角色、部门、岗位、字典及用户）
	 *
	 * @param ids 租户主键集合
	 * @return 是否成功
	 */
	boolean recycleTenant(List<Long> ids);

	/**
	 * 从回收站恢复租户（同步启用其关联的角色、部门、岗位、字典及用户）
	 *
	 * @param ids 租户主键集合
	 * @return 是否成功
	 */
	boolean passTenant(List<Long> ids);

	/**
	 * 从回收站彻底删除租户（同步清除其关联的角色、部门、岗位、字典及用户数据）
	 *
	 * @param ids 租户主键集合
	 * @return 是否成功
	 */
	boolean removeTenant(List<Long> ids);

}
