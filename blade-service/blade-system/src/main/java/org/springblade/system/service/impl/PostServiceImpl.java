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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springblade.core.cache.utils.CacheUtil;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.TenantGuard;
import org.springblade.core.tool.utils.Func;
import org.springblade.system.pojo.entity.Post;
import org.springblade.system.mapper.PostMapper;
import org.springblade.system.service.IPostService;
import org.springblade.system.pojo.vo.PostVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static org.springblade.core.cache.constant.CacheConstant.SYS_CACHE;
import static org.springblade.core.tenant.TenantGuard.EntityType.POST;

/**
 * 岗位表 服务实现类
 *
 * @author Chill
 */
@Service
public class PostServiceImpl extends BaseServiceImpl<PostMapper, Post> implements IPostService {

	@Override
	public IPage<PostVO> selectPostPage(IPage<PostVO> page, PostVO post) {
		return page.setRecords(baseMapper.selectPostPage(page, post));
	}

	@Override
	public String getPostIds(String tenantId, String postNames) {
		List<Post> postList = baseMapper.selectList(Wrappers.<Post>query().lambda().eq(Post::getTenantId, tenantId).in(Post::getPostName, Func.toStrList(postNames)));
		if (postList != null && postList.size() > 0) {
			return postList.stream().map(post -> Func.toStr(post.getId())).distinct().collect(Collectors.joining(","));
		}
		return null;
	}

	@Override
	public String getPostIdsByFuzzy(String tenantId, String postNames) {
		LambdaQueryWrapper<Post> queryWrapper = Wrappers.<Post>query().lambda().eq(Post::getTenantId, tenantId);
		queryWrapper.and(wrapper -> {
			List<String> names = Func.toStrList(postNames);
			names.forEach(name -> wrapper.like(Post::getPostName, name).or());
		});
		List<Post> postList = baseMapper.selectList(queryWrapper);
		if (postList != null && postList.size() > 0) {
			return postList.stream().map(post -> Func.toStr(post.getId())).distinct().collect(Collectors.joining(","));
		}
		return null;
	}

	@Override
	public List<String> getPostNames(String postIds) {
		return baseMapper.getPostNames(Func.toLongArray(postIds));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean submit(Post post) {
		// 租户守卫：非超管强制写入当前会话 tenantId，修改时校验目标岗位归属
		TenantGuard.bindTenant(this, post, POST);
		if (Func.isEmpty(post.getTenantId())) {
			// 超管新增未指定租户时默认归属当前会话租户
			if (AuthUtil.isAdministrator()) {
				post.setTenantId(AuthUtil.getTenantId());
			} else {
				throw new ServiceException("租户ID不能为空");
			}
		}
		CacheUtil.clear(SYS_CACHE);
		return saveOrUpdate(post);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean remove(List<Long> ids) {
		// 租户守卫：批量校验目标岗位全部归属当前会话租户
		TenantGuard.verifyBatch(this, ids, POST);
		CacheUtil.clear(SYS_CACHE);
		return deleteLogic(ids);
	}

	@Override
	public List<Post> selectByTenant(String tenantId) {
		String resolvedTenantId = AuthUtil.isAdministrator()
			? Func.toStrWithEmpty(tenantId, AuthUtil.getTenantId())
			: AuthUtil.getTenantId();
		return list(Wrappers.<Post>query().lambda().eq(Post::getTenantId, resolvedTenantId));
	}

}
