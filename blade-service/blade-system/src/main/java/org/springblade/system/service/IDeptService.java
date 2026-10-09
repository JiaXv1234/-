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

import com.baomidou.mybatisplus.extension.service.IService;
import org.springblade.system.pojo.entity.Dept;
import org.springblade.system.pojo.vo.DeptVO;
import org.springblade.system.pojo.vo.UserVO;

import java.util.List;
import java.util.Map;

/**
 * 服务类
 *
 * @author Chill
 */
public interface IDeptService extends IService<Dept> {

	/**
	 * 详情查询（含超管判定 + 当前租户隔离）
	 *
	 * @param dept 查询条件
	 * @return 单条记录，非超管时仅命中当前会话租户
	 */
	Dept getDetail(Dept dept);

	/**
	 * 列表（含超管判定 + 当前租户隔离）
	 *
	 * @param dept 查询条件 Map
	 * @return 列表 VO，非超管时仅命中当前会话租户
	 */
	List<DeptVO> selectList(Map<String, Object> dept);

	/**
	 * 懒加载部门列表（按父级逐层加载，含超管判定 + 当前租户隔离）
	 *
	 * @param tenantId 入参 tenantId（仅超管生效）
	 * @param parentId 父级部门ID
	 * @param param    查询条件
	 * @return 当前层级的部门列表 VO
	 */
	List<DeptVO> lazyList(String tenantId, Long parentId, Map<String, Object> param);

	/**
	 * 树形结构（含超管判定 + 当前租户隔离）
	 * <p>
	 * 仅超级管理员可指定任意 tenantId 查询；其他用户传入的 tenantId 一律被忽略，
	 * 强制使用当前会话租户。
	 *
	 * @param tenantId 入参 tenantId（仅超管生效）
	 * @return 部门树
	 */
	List<DeptVO> tree(String tenantId);

	/**
	 * 懒加载树形结构（含超管判定 + 当前租户隔离）
	 * <p>
	 * 仅超级管理员可指定任意 tenantId，空值表示查询全部；其他用户强制使用当前会话租户。
	 *
	 * @param tenantId 入参 tenantId（仅超管生效）
	 * @param parentId 父级部门ID
	 * @return 当前层级的部门树
	 */
	List<DeptVO> lazyTree(String tenantId, Long parentId);

	/**
	 * 根据部门名称精确匹配获取部门ID集合
	 *
	 * @param tenantId  租户ID
	 * @param deptNames 部门名称（逗号分隔）
	 * @return 部门ID集合（逗号分隔），无匹配时返回 null
	 */
	String getDeptIds(String tenantId, String deptNames);

	/**
	 * 根据部门名称模糊匹配获取部门ID集合
	 *
	 * @param tenantId  租户ID
	 * @param deptNames 部门名称（逗号分隔）
	 * @return 部门ID集合（逗号分隔），无匹配时返回 null
	 */
	String getDeptIdsByFuzzy(String tenantId, String deptNames);

	/**
	 * 根据部门ID集合获取部门名称
	 *
	 * @param deptIds 部门ID集合（逗号分隔）
	 * @return 部门名称列表
	 */
	List<String> getDeptNames(String deptIds);

	/**
	 * 获取指定部门下的所有子部门
	 *
	 * @param deptId 部门ID
	 * @return 子部门列表
	 */
	List<Dept> getDeptChild(Long deptId);

	/**
	 * 删除部门（批量租户守卫校验，存在子节点时拒绝删除）
	 *
	 * @param ids 部门主键集合（逗号分隔）
	 * @return 是否成功
	 */
	boolean removeDept(String ids);

	/**
	 * 新增或修改部门
	 * <p>
	 * 顶级部门绑定当前会话租户；子部门继承父级租户，禁止跨租户迁移。
	 *
	 * @param dept 部门实体
	 * @return 是否成功
	 */
	boolean submit(Dept dept);

	/**
	 * 按名称与父级查询部门列表（限定当前会话租户）
	 *
	 * @param deptName 部门名称（模糊匹配）
	 * @param parentId 父级部门ID
	 * @return 部门列表 VO
	 */
	List<DeptVO> search(String deptName, Long parentId);

	/**
	 * 获取部门的主管信息（租户守卫校验部门归属，主管列表限定本租户）
	 *
	 * @param deptId 部门id
	 * @return 主管用户列表 VO
	 */
	List<UserVO> deptLeaderInfo(Long deptId);

}
