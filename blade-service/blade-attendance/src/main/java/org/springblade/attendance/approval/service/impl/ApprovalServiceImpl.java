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
package org.springblade.attendance.approval.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springblade.attendance.approval.integration.ApprovalBusinessHandler;
import org.springblade.attendance.approval.integration.ApprovalHandlerRegistry;
import org.springblade.attendance.approval.mapper.*;
import org.springblade.attendance.approval.security.ApprovalIdentity;
import org.springblade.attendance.approval.service.IApprovalService;
import org.springblade.attendance.duty.mapper.DutyEmployeeMapper;
import org.springblade.attendance.pojo.dto.ApprovalActionDTO;
import org.springblade.attendance.pojo.entity.*;
import org.springblade.attendance.pojo.vo.*;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * 表驱动审批。流程行锁串行化同一流程内的操作；管理员也必须是实际待办审批人才能操作。
 * 未注册业务回写处理器时只允许中间级同意和抄送，不允许进入通过/退回终态。
 * @author Chill
 */
@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements IApprovalService {
	private final ApprovalFlowMapper flowMapper;
	private final ApprovalNodeMapper nodeMapper;
	private final ApprovalTaskMapper taskMapper;
	private final ApprovalCcMapper ccMapper;
	private final ApprovalQueryMapper queryMapper;
	private final DutyEmployeeMapper employeeMapper;
	private final ApprovalIdentity identity;
	private final ApprovalHandlerRegistry registry;

	private LocalDateTime now() { return LocalDateTime.now(ZoneId.of("Asia/Shanghai")); }
	@Override
	public ApprovalIdentityVO identity() { return identity.current(); }
	private void monitorCheck(ApprovalIdentityVO actor, boolean monitor) {
		if (monitor && !actor.isAdministrator()) throw new ServiceException("只有管理员可以使用只读管理查看");
	}
	private List<ApprovalNode> nodes(String flowId) {
		return nodeMapper.selectList(Wrappers.<ApprovalNode>lambdaQuery()
			.eq(ApprovalNode::getFlowId, flowId).orderByAsc(ApprovalNode::getLevelNo));
	}
	private void validateChain(List<ApprovalNode> chain) {
		if (chain.isEmpty() || chain.size() > 7) throw new ServiceException("审批节点须为1至7级");
		Set<Integer> levels = new HashSet<>();
		for (ApprovalNode node : chain) {
			if (node.getLevelNo() == null || node.getLevelNo() < 1 || node.getLevelNo() > 7
				|| !levels.add(node.getLevelNo()) || node.getApproverId() == null
				|| !Set.of("1", "2", "3", "4").contains(node.getNodeStatus()))
				throw new ServiceException("审批节点级数或审批人配置不正确");
		}
		for (int level = 1; level <= chain.size(); level++) {
			if (!levels.contains(level)) throw new ServiceException("审批链级数必须连续");
		}
	}
	private ApprovalBusinessVO business(ApprovalFlow flow, boolean required) {
		ApprovalBusinessVO value = queryMapper.business(flow.getBizType(), flow.getBizNo());
		if (required && (value == null || value.getRequesterId() == null
			|| !Objects.equals(value.getSourceFlowId(), flow.getFlowId())
			|| !"1".equals(value.getApproveStatus())))
			throw new ServiceException("业务申请不存在、已变更或未处于本流程的审批中状态");
		return value;
	}
	private void enrich(ApprovalItemVO item) { item.setBusiness(business(item, false)); }
	@Override
	public IPage<ApprovalItemVO> page(Query query, String tab, boolean monitor) {
		if (!Set.of("todo", "done", "cc").contains(tab)) throw new ServiceException("列表类型不正确");
		ApprovalIdentityVO actor = identity.current();
		monitorCheck(actor, monitor);
		IPage<ApprovalItemVO> page = Condition.getPage(query);
		page.setSize(Math.min(100, Math.max(1, page.getSize())));
		page.setCurrent(Math.max(1, page.getCurrent()));
		IPage<ApprovalItemVO> result = queryMapper.selectCenterPage(page, tab,
			actor.getEmployeeId() == null ? -1L : actor.getEmployeeId(), monitor);
		result.getRecords().forEach(this::enrich);
		return result;
	}
	private String employeeName(Long id) {
		DutyEmployee employee = id == null ? null : employeeMapper.selectById(id);
		return employee == null ? "人员未匹配" : employee.getUserName();
	}
	@Override
	public ApprovalDetailVO detail(String flowId, boolean monitor) {
		ApprovalIdentityVO actor = identity.current();
		monitorCheck(actor, monitor);
		ApprovalFlow flow = flowMapper.selectById(flowId);
		if (flow == null) throw new ServiceException("流程不存在");
		List<ApprovalNode> chain = nodes(flowId);
		List<ApprovalCc> copies = ccMapper.selectList(Wrappers.<ApprovalCc>lambdaQuery()
			.eq(ApprovalCc::getFlowId, flowId).orderByDesc(ApprovalCc::getCcTime));
		ApprovalBusinessVO source = business(flow, false);
		Long actorId = actor.getEmployeeId();
		boolean assigned = actorId != null && chain.stream().anyMatch(node -> actorId.equals(node.getApproverId()));
		boolean copied = actorId != null && copies.stream().anyMatch(copy -> actorId.equals(copy.getToUserId()));
		boolean requester = actorId != null && source != null && actorId.equals(source.getRequesterId());
		if (!(monitor || assigned || copied || requester)) throw new ServiceException("无权查看此流程");
		ApprovalDetailVO result = new ApprovalDetailVO();
		ApprovalItemVO item = new ApprovalItemVO();
		BeanUtils.copyProperties(flow, item);
		item.setBusiness(source);
		List<ApprovalTask> historyTasks = taskMapper.selectList(Wrappers.<ApprovalTask>lambdaQuery()
			.eq(ApprovalTask::getFlowId, flowId).orderByAsc(ApprovalTask::getCreateTime));
		if (!historyTasks.isEmpty()) item.setArrivedTime(historyTasks.get(0).getCreateTime());
		List<ApprovalNode> activeNodes = chain.stream().filter(node -> "2".equals(node.getNodeStatus())).toList();
		ApprovalNode active = activeNodes.size() == 1 ? activeNodes.get(0) : null;
		item.setLevelNo(active == null ? null : active.getLevelNo());
		result.setItem(item);
		result.setCanSeeChain(monitor || (assigned && !requester));
		List<ApprovalNodeVO> nodeViews = new ArrayList<>();
		if (result.isCanSeeChain()) for (ApprovalNode node : chain) {
			ApprovalNodeVO nodeView = new ApprovalNodeVO();
			BeanUtils.copyProperties(node, nodeView);
			nodeView.setApproverName(employeeName(node.getApproverId()));
			nodeViews.add(nodeView);
		}
		result.setNodes(nodeViews);
		List<ApprovalCcVO> copyViews = new ArrayList<>();
		for (ApprovalCc copy : copies) {
			if (!result.isCanSeeChain() && !Objects.equals(copy.getToUserId(), actorId)) continue;
			ApprovalCcVO view = new ApprovalCcVO();
			BeanUtils.copyProperties(copy, view);
			view.setFromUserName(employeeName(copy.getFromUserId()));
			view.setToUserName(employeeName(copy.getToUserId()));
			copyViews.add(view);
		}
		result.setCopies(copyViews);
		List<ApprovalTask> activeTasks = historyTasks.stream().filter(task -> "1".equals(task.getTaskStatus())).toList();
		ApprovalTask task = activeTasks.size() == 1 ? activeTasks.get(0) : null;
		boolean linked = source != null && Objects.equals(source.getSourceFlowId(), flowId) && "1".equals(source.getApproveStatus());
		boolean validActive = active != null && task != null && "1".equals(task.getTaskType()) && Objects.equals(task.getNodeId(), active.getNodeId())
			&& Objects.equals(task.getApproverId(), active.getApproverId()) && "1".equals(flow.getFlowStatus());
		boolean self = actorId != null && source != null && actorId.equals(source.getRequesterId());
		String chainIssue = active == null ? "流程没有唯一的当前节点" : sequenceIssue(chain, active.getLevelNo());
		result.setCanHandle(!monitor && linked && validActive && chainIssue.isEmpty() && !self && actorId != null && actorId.equals(task.getApproverId()));
		if (result.isCanHandle()) result.setActiveTaskId(task.getTaskId());
		result.setFinalStep(active != null && chainIssue.isEmpty() && chain.stream().noneMatch(node -> "3".equals(node.getNodeStatus())
			&& node.getLevelNo() > active.getLevelNo() && (source == null || !Objects.equals(node.getApproverId(), source.getRequesterId()))));
		result.setCallbackReady(registry.find(flow.getBizType()) != null);
		result.setBlockReason(monitor ? "管理员管理查看为只读，不代替实际审批人操作"
			: actorId == null ? actor.getMessage()
			: self ? "不能审批本人发起的申请；提交模块须按规则跳过自审节点"
			: !linked ? "业务申请未关联当前流程或已变更"
			: "1".equals(flow.getFlowStatus()) && !chainIssue.isEmpty() ? chainIssue
			: task != null && !"1".equals(task.getTaskType()) ? "此记录为代申请确认待办，须由对应业务模块处理"
			: !result.isCanHandle() ? "只有当前待办审批人可以操作"
			: !result.isCallbackReady() ? "业务回写尚未接入：末级同意及退回结束流程被禁用；中间级同意和抄送可用"
			: "");
		return result;
	}
	private String sequenceIssue(List<ApprovalNode> chain, Integer currentLevel) {
		try { validateChain(chain); }
		catch (ServiceException exception) { return exception.getMessage(); }
		for (ApprovalNode node : chain) {
			if (node.getLevelNo() < currentLevel && !("4".equals(node.getNodeStatus())
				|| ("1".equals(node.getNodeStatus()) && "1".equals(node.getOpResult()))))
				return "当前节点之前存在未同意或未跳过的节点，禁止继续流转";
			if (node.getLevelNo() > currentLevel && !Set.of("3", "4").contains(node.getNodeStatus()))
				return "后续节点状态异常，禁止继续流转";
		}
		return "";
	}
	private void validateAction(ApprovalActionDTO action, boolean opinionRequired) {
		if (action == null || action.getTaskId() == null) throw new ServiceException("请选择有效待办");
		String opinion = action.getOpinion();
		if (opinion != null && opinion.length() > 500) throw new ServiceException("审批意见不能超过500字");
		if (opinionRequired && (opinion == null || opinion.isBlank())) throw new ServiceException("退回必须填写原因");
	}
	private ApprovalTask lockTask(ApprovalActionDTO action, Long actorId) {
		ApprovalTask initial = taskMapper.selectById(action.getTaskId());
		if (initial == null || !Objects.equals(initial.getApproverId(), actorId)) throw new ServiceException("待办不存在或不属于当前账号");
		ApprovalFlow flow = flowMapper.lock(initial.getFlowId());
		ApprovalTask current = taskMapper.selectById(action.getTaskId());
		ApprovalNode node = current == null ? null : nodeMapper.selectById(current.getNodeId());
		if (flow == null || !"1".equals(flow.getFlowStatus()) || current == null || !"1".equals(current.getTaskStatus())
			|| !Objects.equals(current.getApproverId(), actorId) || node == null || !"2".equals(node.getNodeStatus())
			|| !Objects.equals(node.getFlowId(), flow.getFlowId()) || !Objects.equals(node.getApproverId(), actorId))
			throw new ServiceException("待办已处理或流程状态已变化，请刷新后重试");
		List<ApprovalTask> active = taskMapper.selectList(Wrappers.<ApprovalTask>lambdaQuery()
			.eq(ApprovalTask::getFlowId, flow.getFlowId()).eq(ApprovalTask::getTaskStatus, "1"));
		if (active.size() != 1) throw new ServiceException("流程存在多个当前待办，禁止继续流转");
		if (!"1".equals(current.getTaskType())) throw new ServiceException("代申请确认待办须由对应业务模块处理，不能按普通审批流转");
		ApprovalBusinessVO source = business(flow, true);
		if (Objects.equals(source.getRequesterId(), actorId)) throw new ServiceException("不能审批本人发起的申请");
		List<ApprovalNode> chain = nodes(flow.getFlowId());
		validateChain(chain);
		if (chain.stream().filter(candidate -> "2".equals(candidate.getNodeStatus())).count() != 1)
			throw new ServiceException("流程存在多个当前节点，禁止继续流转");
		String sequenceIssue = sequenceIssue(chain, node.getLevelNo());
		if (!sequenceIssue.isEmpty()) throw new ServiceException(sequenceIssue);
		return current;
	}
	private void finishTask(ApprovalTask task, String status, String result, String opinion) {
		LocalDateTime time = now();
		int changed = taskMapper.update(null, Wrappers.<ApprovalTask>lambdaUpdate()
			.eq(ApprovalTask::getTaskId, task.getTaskId()).eq(ApprovalTask::getTaskStatus, "1")
			.set(ApprovalTask::getTaskStatus, status).set(ApprovalTask::getFinishTime, time));
		if (changed != 1) throw new ServiceException("待办已被处理，请刷新");
		int nodeChanged = nodeMapper.update(null, Wrappers.<ApprovalNode>lambdaUpdate()
			.eq(ApprovalNode::getNodeId, task.getNodeId()).eq(ApprovalNode::getNodeStatus, "2")
			.set(ApprovalNode::getNodeStatus, "1").set(ApprovalNode::getOpResult, result)
			.set(ApprovalNode::getOpTime, time).set(ApprovalNode::getOpinion, opinion));
		if (nodeChanged != 1) throw new ServiceException("当前节点已发生变化，请刷新");
	}
	private void verifyOutcome(ApprovalFlow flow, String expectedStatus) {
		ApprovalBusinessVO updated = business(flow, false);
		if (updated == null || !Objects.equals(updated.getSourceFlowId(), flow.getFlowId())
			|| !expectedStatus.equals(updated.getApproveStatus()))
			throw new ServiceException("业务回写未同步更新申请状态，本次操作回滚");
	}
	private void advance(ApprovalFlow flow, ApprovalBusinessVO source, int afterLevel) {
		List<ApprovalNode> chain = nodes(flow.getFlowId());
		for (ApprovalNode next : chain) {
			if (next.getLevelNo() <= afterLevel || !"3".equals(next.getNodeStatus())) continue;
			if (Objects.equals(next.getApproverId(), source.getRequesterId())) {
				next.setNodeStatus("4"); next.setOpTime(now()); next.setOpinion("发起人节点自动跳过，防止自审");
				nodeMapper.updateById(next);
				continue;
			}
			DutyEmployee approver = employeeMapper.selectById(next.getApproverId());
			if (approver == null || !Objects.equals(approver.getOnJobStatus(), 1)) throw new ServiceException("下一级审批人不在职，请修正审批链");
			next.setNodeStatus("2"); nodeMapper.updateById(next);
			ApprovalTask todo = new ApprovalTask();
			todo.setFlowId(flow.getFlowId()); todo.setNodeId(next.getNodeId()); todo.setApproverId(next.getApproverId());
			todo.setTaskStatus("1"); todo.setTaskType("1"); todo.setCreateTime(now()); taskMapper.insert(todo);
			ApprovalBusinessHandler handler = registry.find(flow.getBizType());
			if (handler != null) handler.onProgress(flow, source, next.getLevelNo(), chain.size());
			return;
		}
		ApprovalBusinessHandler handler = registry.require(flow.getBizType());
		handler.onApproved(flow, source);
		verifyOutcome(flow, "2");
		flow.setFlowStatus("2"); flowMapper.updateById(flow);
	}
	@Override
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public void agree(ApprovalActionDTO action) {
		validateAction(action, false);
		Long actorId = identity.requireEmployee();
		ApprovalTask task = lockTask(action, actorId);
		ApprovalFlow flow = flowMapper.selectById(task.getFlowId());
		ApprovalBusinessVO source = business(flow, true);
		ApprovalNode node = nodeMapper.selectById(task.getNodeId());
		boolean hasNext = nodes(flow.getFlowId()).stream().anyMatch(next -> "3".equals(next.getNodeStatus())
			&& next.getLevelNo() > node.getLevelNo() && !Objects.equals(next.getApproverId(), source.getRequesterId()));
		if (!hasNext) registry.require(flow.getBizType());
		finishTask(task, "2", "1", action.getOpinion());
		advance(flow, source, node.getLevelNo());
	}
	@Override
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public void reject(ApprovalActionDTO action) {
		validateAction(action, true);
		Long actorId = identity.requireEmployee();
		ApprovalTask task = lockTask(action, actorId);
		ApprovalFlow flow = flowMapper.selectById(task.getFlowId());
		ApprovalBusinessVO source = business(flow, true);
		ApprovalBusinessHandler handler = registry.require(flow.getBizType());
		handler.onRejected(flow, source);
		verifyOutcome(flow, "3");
		finishTask(task, "3", "2", action.getOpinion());
		taskMapper.update(null, Wrappers.<ApprovalTask>lambdaUpdate().eq(ApprovalTask::getFlowId, flow.getFlowId())
			.eq(ApprovalTask::getTaskStatus, "1").set(ApprovalTask::getTaskStatus, "4").set(ApprovalTask::getFinishTime, now()));
		flow.setFlowStatus("3"); flowMapper.updateById(flow);
	}
	@Override
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public void copy(ApprovalActionDTO action) {
		validateAction(action, false);
		Long actorId = identity.requireEmployee();
		ApprovalTask task = lockTask(action, actorId);
		if (action.getRecipientIds() == null || action.getRecipientIds().isEmpty() || action.getRecipientIds().size() > 20)
			throw new ServiceException("请选择1至20名抄送接收人");
		Set<Long> recipients = new LinkedHashSet<>(action.getRecipientIds());
		if (recipients.contains(null) || recipients.contains(actorId)) throw new ServiceException("抄送接收人无效或包含自己");
		Set<String> displayed = new LinkedHashSet<>();
		if (task.getCcUserIds() != null && !task.getCcUserIds().isBlank()) displayed.addAll(Arrays.asList(task.getCcUserIds().split(",")));
		ApprovalFlow flow = flowMapper.selectById(task.getFlowId());
		for (Long recipientId : recipients) {
			DutyEmployee employee = employeeMapper.selectById(recipientId);
			if (employee == null || !Objects.equals(employee.getOnJobStatus(), 1)) throw new ServiceException("抄送接收人不存在或已离职");
			Long count = ccMapper.selectCount(Wrappers.<ApprovalCc>lambdaQuery().eq(ApprovalCc::getFlowId, flow.getFlowId())
				.eq(ApprovalCc::getFromUserId, actorId).eq(ApprovalCc::getToUserId, recipientId));
			if (count == 0) {
				ApprovalCc record = new ApprovalCc();
				record.setFlowId(flow.getFlowId()); record.setBizNo(flow.getBizNo()); record.setFromUserId(actorId);
				record.setToUserId(recipientId); record.setIsRead("0"); record.setCcTime(now()); ccMapper.insert(record);
			}
			displayed.add(String.valueOf(recipientId));
		}
		String display = String.join(",", displayed);
		if (display.length() > 500) throw new ServiceException("当前待办累计抄送人数超出限制");
		task.setCcUserIds(display); taskMapper.updateById(task);
	}
	@Override
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public void markRead(Long ccId) {
		Long actorId = identity.requireEmployee();
		ApprovalCc record = ccMapper.selectById(ccId);
		if (record == null || !actorId.equals(record.getToUserId())) throw new ServiceException("无权标记此抄送记录");
		ccMapper.update(null, Wrappers.<ApprovalCc>lambdaUpdate().eq(ApprovalCc::getId, ccId)
			.eq(ApprovalCc::getToUserId, actorId).eq(ApprovalCc::getIsRead, "0").set(ApprovalCc::getIsRead, "1"));
	}
	@Override
	public List<DutyEmployee> recipients(String keyword) {
		identity.requireEmployee();
		if (keyword == null || keyword.trim().length() < 1) return List.of();
		if (keyword.length() > 32) throw new ServiceException("搜索关键字不能超过32字");
		return employeeMapper.selectList(Wrappers.<DutyEmployee>lambdaQuery().eq(DutyEmployee::getOnJobStatus, 1)
			.and(wrapper -> wrapper.like(DutyEmployee::getUserCode, keyword.trim()).or().like(DutyEmployee::getUserName, keyword.trim()))
			.orderByAsc(DutyEmployee::getUserCode).last("LIMIT 100"));
	}
	@Override
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public ApprovalFlow start(String bizType, String bizNo, List<Long> resolvedApprovers) {
		Long actorId = identity.requireEmployee();
		if (bizType == null || !Set.of("1", "2", "3", "4", "5").contains(bizType)) throw new ServiceException("不支持此审批业务类型；值班不进入审批");
		if (bizNo == null || bizNo.isBlank() || bizNo.length() > 32) throw new ServiceException("业务单号无效");
		if (resolvedApprovers == null || resolvedApprovers.isEmpty() || resolvedApprovers.size() > 7) throw new ServiceException("审批链必须为1至7级");
		ApprovalBusinessHandler handler = registry.require(bizType);
		// 锁定发起人既有记录，串行化提交；READ_COMMITTED 避免锁等待后读取旧快照。
		if (queryMapper.lockApplicant(actorId) == null) throw new ServiceException("发起人不存在");
		ApprovalBusinessVO source = queryMapper.business(bizType, bizNo);
		if (source == null || !actorId.equals(source.getRequesterId())) throw new ServiceException("无权提交此业务申请");
		if (!Set.of("0", "3", "4").contains(source.getApproveStatus())) throw new ServiceException("申请不处于可提交状态");
		if (source.getSourceFlowId() != null && !source.getSourceFlowId().isBlank()) {
			ApprovalFlow old = flowMapper.lock(source.getSourceFlowId());
			if (old == null || "1".equals(old.getFlowStatus()) || "2".equals(old.getFlowStatus())) throw new ServiceException("已有审批中或通过的流程");
		}
		List<ApprovalFlow> duplicates = flowMapper.selectList(Wrappers.<ApprovalFlow>lambdaQuery()
			.eq(ApprovalFlow::getBizType, bizType).eq(ApprovalFlow::getBizNo, bizNo).eq(ApprovalFlow::getFlowStatus, "1"));
		if (!duplicates.isEmpty()) throw new ServiceException("该申请已有进行中的流程");
		ApprovalFlow flow = new ApprovalFlow();
		flow.setFlowId(UUID.randomUUID().toString().replace("-", "")); flow.setBizType(bizType); flow.setBizNo(bizNo);
		flow.setMatchOrganId(source.getOrganId()); flow.setMatchVacId(source.getVacId()); flow.setMatchUserAttr(source.getRequesterAttr());
		flow.setFlowStatus("1"); flowMapper.insert(flow);
		for (int index = 0; index < resolvedApprovers.size(); index++) {
			Long approverId = resolvedApprovers.get(index);
			DutyEmployee employee = approverId == null ? null : employeeMapper.selectById(approverId);
			if (employee == null || !Objects.equals(employee.getOnJobStatus(), 1)) throw new ServiceException("审批链包含无效或离职人员");
			ApprovalNode node = new ApprovalNode();
			node.setFlowId(flow.getFlowId()); node.setLevelNo(index + 1); node.setApproverId(approverId); node.setNodeStatus("3");
			nodeMapper.insert(node);
		}
		handler.onSubmitted(flow, source);
		ApprovalBusinessVO linked = business(flow, true);
		advance(flow, linked, 0);
		return flow;
	}
}
