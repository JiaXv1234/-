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
import org.springblade.system.pojo.entity.Post;
import org.springblade.system.pojo.vo.PostVO;

import java.util.List;

/**
 * 岗位表 服务类
 *
 * @author Chill
 */
public interface IPostService extends BaseService<Post> {

	/**
	 * 自定义分页
	 *
	 * @param page
	 * @param post
	 * @return
	 */
	IPage<PostVO> selectPostPage(IPage<PostVO> page, PostVO post);

	/**
	 * 获取岗位ID
	 *
	 * @param tenantId
	 * @param postNames
	 * @return
	 */
	String getPostIds(String tenantId, String postNames);

	/**
	 * 获取岗位ID
	 *
	 * @param tenantId
	 * @param postNames
	 * @return
	 */
	String getPostIdsByFuzzy(String tenantId, String postNames);

	/**
	 * 获取岗位名
	 *
	 * @param postIds
	 * @return
	 */
	List<String> getPostNames(String postIds);

	/**
	 * 新增或修改岗位（带租户归属校验）
	 *
	 * @param post 岗位实体
	 * @return 是否成功
	 */
	boolean submit(Post post);

	/**
	 * 删除岗位（带租户归属校验）
	 *
	 * @param ids 岗位主键集合
	 * @return 是否成功
	 */
	boolean remove(List<Long> ids);

	/**
	 * 下拉数据源（含超管判定 + 当前租户隔离）
	 * <p>
	 * 仅超级管理员可指定任意 tenantId 查询；其他用户传入的 tenantId 一律被忽略，
	 * 强制使用当前会话租户。
	 *
	 * @param tenantId 入参 tenantId（仅超管生效）
	 * @return 岗位列表
	 */
	List<Post> selectByTenant(String tenantId);

}
