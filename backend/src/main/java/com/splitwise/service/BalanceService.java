package com.splitwise.service;

import com.splitwise.dto.BalanceResponse;
import com.splitwise.entity.Expense;
import com.splitwise.entity.ExpenseSplit;
import com.splitwise.repository.ExpenseRepository;
import com.splitwise.repository.ExpenseSplitRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;

    public List<BalanceResponse> computeBalances(Long groupId) {

        List<Expense> expenses = expenseRepository.findByGroupId(groupId);

        Map<Long, Double> net = new HashMap<>();

        for (Expense expense : expenses) {

            Long payer = expense.getPaidBy();

            List<ExpenseSplit> splits =
                    expenseSplitRepository.findByExpenseIdIn(List.of(expense.getId()));

            for (ExpenseSplit split : splits) {

                Long user = split.getUserId();
                double amount = split.getAmount();

                if (user.equals(payer)) continue;

                net.put(user, net.getOrDefault(user, 0.0) - amount);
                net.put(payer, net.getOrDefault(payer, 0.0) + amount);
            }
        }

        PriorityQueue<Map.Entry<Long, Double>> creditors =
                new PriorityQueue<>((a,b) -> Double.compare(b.getValue(), a.getValue()));

        PriorityQueue<Map.Entry<Long, Double>> debtors =
                new PriorityQueue<>((a,b) -> Double.compare(a.getValue(), b.getValue()));

        for (Map.Entry<Long, Double> entry : net.entrySet()) {

            if (entry.getValue() > 0) {
                creditors.add(entry);
            } else if (entry.getValue() < 0) {
                debtors.add(entry);
            }
        }

        List<BalanceResponse> result = new ArrayList<>();

        while (!creditors.isEmpty() && !debtors.isEmpty()) {

            Map.Entry<Long, Double> creditor = creditors.poll();
            Map.Entry<Long, Double> debtor = debtors.poll();

            double settleAmount =
                    Math.min(creditor.getValue(), -debtor.getValue());

            result.add(
                    BalanceResponse.builder()
                            .fromUserId(debtor.getKey())
                            .toUserId(creditor.getKey())
                            .amount(settleAmount)
                            .build()
            );

            creditor.setValue(creditor.getValue() - settleAmount);
            debtor.setValue(debtor.getValue() + settleAmount);

            if (creditor.getValue() > 0) creditors.add(creditor);
            if (debtor.getValue() < 0) debtors.add(debtor);
        }

        return result;
    }
}