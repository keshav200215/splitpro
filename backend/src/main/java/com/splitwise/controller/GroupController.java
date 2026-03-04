package com.splitwise.controller;

import com.splitwise.dto.*;
import com.splitwise.entity.*;
import com.splitwise.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;

    /* =====================================================
       CREATE GROUP
    ===================================================== */

    @PostMapping
    public GroupResponse createGroup(@RequestBody CreateGroupRequest request) {

        User currentUser = getCurrentUser();

        Group group = Group.builder()
                .name(request.getName())
                .createdBy(currentUser.getId())
                .build();

        group = groupRepository.save(group);

        groupMemberRepository.save(
                GroupMember.builder()
                        .groupId(group.getId())
                        .userId(currentUser.getId())
                        .build()
        );

        return buildGroupResponse(group, currentUser);
    }

    /* =====================================================
       GET GROUP MEMBERS
    ===================================================== */

    @GetMapping("/{groupId}/members")
    public List<UserResponse> getGroupMembers(@PathVariable Long groupId) {

        User currentUser = getCurrentUser();

        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, currentUser.getId())) {
            throw new RuntimeException("Not part of this group");
        }

        List<GroupMember> memberships =
                groupMemberRepository.findByGroupId(groupId);

        return memberships.stream()
                .map(m -> userRepository.findById(m.getUserId()).orElse(null))
                .filter(Objects::nonNull)
                .map(u -> UserResponse.builder()
                        .id(u.getId())
                        .name(u.getName())
                        .email(u.getEmail())
                        .build())
                .toList();
    }

    /* =====================================================
       GET MY GROUPS
    ===================================================== */

    @GetMapping
    public List<GroupResponse> getMyGroups() {

        User currentUser = getCurrentUser();

        List<GroupMember> memberships =
                groupMemberRepository.findByUserId(currentUser.getId());

        List<Long> groupIds = memberships.stream()
                .map(GroupMember::getGroupId)
                .toList();

        List<Group> groups = groupRepository.findByIdIn(groupIds);

        return groups.stream()
                .map(group -> {
                    User creator = userRepository
                            .findById(group.getCreatedBy())
                            .orElseThrow();
                    return buildGroupResponse(group, creator);
                })
                .toList();
    }

    /* =====================================================
       ADD MEMBER
    ===================================================== */

    @PostMapping("/{groupId}/members")
public String addMember(@PathVariable Long groupId,
                        @RequestBody AddMemberRequest request) {

    User currentUser = getCurrentUser();

    if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, currentUser.getId())) {
        throw new RuntimeException("You are not a member of this group");
    }

    Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

    User userToAdd;

    if (userOptional.isPresent()) {

        userToAdd = userOptional.get();

    } else {

        userToAdd = userRepository.save(
                User.builder()
                        .name("Pending User")
                        .email(request.getEmail())
                        .password("")
                        .invited(true)
                        .build()
        );
    }

    if (groupMemberRepository.existsByGroupIdAndUserId(groupId, userToAdd.getId())) {
        return "User already in group";
    }

    groupMemberRepository.save(
            GroupMember.builder()
                    .groupId(groupId)
                    .userId(userToAdd.getId())
                    .build()
    );

    return "User added successfully";
}

    /* =====================================================
       ADD EXPENSE
    ===================================================== */

    @PostMapping("/{groupId}/expenses")
    public String addExpense(@PathVariable Long groupId,
                             @RequestBody CreateExpenseRequest request) {

        User currentUser = getCurrentUser();

        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, currentUser.getId())) {
            throw new RuntimeException("Not part of group");
        }

        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, request.getPaidByUserId())) {
            throw new RuntimeException("Payer must belong to group");
        }

        double totalSplit = request.getSplits()
                .stream()
                .mapToDouble(split -> split.getAmount())
                .sum();

        if (Math.abs(totalSplit - request.getAmount()) > 0.01) {
            throw new RuntimeException("Split amounts do not match total");
        }

        Expense expense = expenseRepository.save(
                Expense.builder()
                        .groupId(groupId)
                        .description(request.getDescription())
                        .amount(request.getAmount())
                        .paidBy(request.getPaidByUserId())
                        .settlement(false)
                        .build()
        );

        for (SplitRequest split : request.getSplits()) {
            expenseSplitRepository.save(
                    ExpenseSplit.builder()
                            .expenseId(expense.getId())
                            .userId(split.getUserId())
                            .amount(split.getAmount())
                            .build()
            );
        }

        return "Expense added";
    }

    /* =====================================================
       GET EXPENSES (THIS WAS MISSING)
    ===================================================== */

    @GetMapping("/{groupId}/expenses")
    public List<ExpenseResponse> getGroupExpenses(@PathVariable Long groupId) {

        User currentUser = getCurrentUser();

        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, currentUser.getId())) {
            throw new RuntimeException("Not part of group");
        }

        List<Expense> expenses = expenseRepository.findByGroupId(groupId);

        return expenses.stream().map(expense -> {

            User payer = userRepository
                    .findById(expense.getPaidBy())
                    .orElseThrow();

            return ExpenseResponse.builder()
                    .id(expense.getId())
                    .description(expense.getDescription())
                    .amount(expense.getAmount())
                    .paidById(expense.getPaidBy())
                    .paidByName(payer.getName())
                    .settlement(expense.isSettlement())
                    .createdAt(expense.getCreatedAt())
                    .build();

        }).toList();
    }

    /* =====================================================
       SETTLE UP
    ===================================================== */

    @PostMapping("/{groupId}/settle")
    public String settleUp(@PathVariable Long groupId,
                           @RequestParam Long fromUserId,
                           @RequestParam Long toUserId,
                           @RequestParam Double amount) {

        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, fromUserId)
                || !groupMemberRepository.existsByGroupIdAndUserId(groupId, toUserId)) {
            throw new RuntimeException("Users must belong to group");
        }

        Expense settlement = expenseRepository.save(
                Expense.builder()
                        .groupId(groupId)
                        .description("Settlement")
                        .amount(amount)
                        .paidBy(fromUserId)
                        .settlement(true)
                        .build()
        );

        expenseSplitRepository.save(
                ExpenseSplit.builder()
                        .expenseId(settlement.getId())
                        .userId(toUserId)
                        .amount(amount)
                        .build()
        );

        return "Settlement recorded";
    }

    /* =====================================================
       DELETE EXPENSE
    ===================================================== */

    @DeleteMapping("/{groupId}/expenses/{expenseId}")
    public String deleteExpense(@PathVariable Long groupId,
                                @PathVariable Long expenseId) {

        User currentUser = getCurrentUser();

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow();

        if (!expense.getPaidBy().equals(currentUser.getId())) {
            throw new RuntimeException("Only payer can delete expense");
        }

        List<ExpenseSplit> splits =
                expenseSplitRepository.findByExpenseIdIn(List.of(expenseId));

        expenseSplitRepository.deleteAll(splits);
        expenseRepository.delete(expense);

        return "Expense deleted";
    }

    /* =====================================================
       EDIT EXPENSE
    ===================================================== */

    @PutMapping("/{groupId}/expenses/{expenseId}")
    public String editExpense(@PathVariable Long groupId,
                              @PathVariable Long expenseId,
                              @RequestBody CreateExpenseRequest request) {

        User currentUser = getCurrentUser();

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow();

        if (!expense.getPaidBy().equals(currentUser.getId())) {
            throw new RuntimeException("Only payer can edit");
        }

        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setPaidBy(request.getPaidByUserId());

        expenseRepository.save(expense);

        expenseSplitRepository.deleteAll(
                expenseSplitRepository.findByExpenseIdIn(List.of(expenseId))
        );

        for (SplitRequest split : request.getSplits()) {
            expenseSplitRepository.save(
                    ExpenseSplit.builder()
                            .expenseId(expenseId)
                            .userId(split.getUserId())
                            .amount(split.getAmount())
                            .build()
            );
        }

        return "Expense updated";
    }

    /* =====================================================
       GET BALANCES
    ===================================================== */

    @GetMapping("/{groupId}/balances")
    public List<BalanceResponse> getBalances(@PathVariable Long groupId) {

        User currentUser = getCurrentUser();

        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, currentUser.getId())) {
            throw new RuntimeException("Not part of group");
        }

        List<Expense> expenses = expenseRepository.findByGroupId(groupId);

        Map<String, Double> debts = new HashMap<>();

        for (Expense expense : expenses) {

            Long payer = expense.getPaidBy();

            List<ExpenseSplit> splits =
                    expenseSplitRepository.findByExpenseIdIn(List.of(expense.getId()));

            for (ExpenseSplit split : splits) {

                Long user = split.getUserId();

                if (user.equals(payer)) continue;

                String key = user + "-" + payer;

                debts.put(key, debts.getOrDefault(key, 0.0) + split.getAmount());
            }
        }

        Map<String, Double> net = new HashMap<>();

        for (Map.Entry<String, Double> entry : debts.entrySet()) {

            String[] parts = entry.getKey().split("-");
            Long from = Long.parseLong(parts[0]);
            Long to = Long.parseLong(parts[1]);

            String reverseKey = to + "-" + from;

            double amount = entry.getValue();

            if (net.containsKey(reverseKey)) {

                double reverseAmount = net.get(reverseKey);

                if (reverseAmount > amount) {
                    net.put(reverseKey, reverseAmount - amount);
                } else if (reverseAmount < amount) {
                    net.remove(reverseKey);
                    net.put(entry.getKey(), amount - reverseAmount);
                } else {
                    net.remove(reverseKey);
                }

            } else {
                net.put(entry.getKey(), amount);
            }
        }

        List<BalanceResponse> result = new ArrayList<>();

        for (Map.Entry<String, Double> entry : net.entrySet()) {

            if (entry.getValue() <= 0.01) continue;

            String[] parts = entry.getKey().split("-");

            Long fromUser = Long.parseLong(parts[0]);
            Long toUser = Long.parseLong(parts[1]);

            result.add(
                    BalanceResponse.builder()
                            .fromUserId(fromUser)
                            .toUserId(toUser)
                            .amount(entry.getValue())
                            .build()
            );
        }

        return result;
    }

    /* =====================================================
       HELPERS
    ===================================================== */

    private User getCurrentUser() {

        Object principal = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof User user) {
            return user;
        }

        throw new RuntimeException("User not authenticated properly");
    }

    private GroupResponse buildGroupResponse(Group group, User creator) {

        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .createdById(group.getCreatedBy())
                .createdByName(creator.getName())
                .build();
    }
}

