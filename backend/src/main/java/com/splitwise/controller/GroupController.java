package com.splitwise.controller;

import com.splitwise.dto.*;
import com.splitwise.entity.*;
import com.splitwise.repository.*;
import com.splitwise.service.BalanceService;

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
    private final BalanceService balanceService;

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
    @GetMapping("/{groupId}/analytics")
public GroupAnalyticsResponse getGroupAnalytics(@PathVariable Long groupId) {

    User currentUser = getCurrentUser();

    if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, currentUser.getId())) {
        throw new RuntimeException("Not part of group");
    }

    List<Expense> expenses = expenseRepository.findByGroupId(groupId);

    double totalGroupExpense = expenses.stream()
            .mapToDouble(Expense::getAmount)
            .sum();

    double yourExpense = expenses.stream()
            .filter(e -> e.getPaidBy().equals(currentUser.getId()))
            .mapToDouble(Expense::getAmount)
            .sum();

    Map<Long, Double> spending = new HashMap<>();

    for (Expense e : expenses) {
        spending.put(
            e.getPaidBy(),
            spending.getOrDefault(e.getPaidBy(), 0.0) + e.getAmount()
        );
    }

    Long topUser = null;
    double max = 0;

    for (Map.Entry<Long, Double> entry : spending.entrySet()) {
        if (entry.getValue() > max) {
            max = entry.getValue();
            topUser = entry.getKey();
        }
    }

    String highestSpender = userRepository
            .findById(topUser)
            .map(User::getName)
            .orElse("Unknown");

    double largestExpense = expenses.stream()
            .mapToDouble(Expense::getAmount)
            .max()
            .orElse(0);

    return GroupAnalyticsResponse.builder()
            .totalGroupExpense(totalGroupExpense)
            .yourExpense(yourExpense)
            .highestSpender(highestSpender)
            .highestExpenseAmount(largestExpense)
            .build();
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

    return balanceService.computeBalances(groupId);
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

