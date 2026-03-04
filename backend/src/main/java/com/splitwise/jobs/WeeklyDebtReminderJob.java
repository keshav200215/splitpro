package com.splitwise.jobs;

import com.splitwise.dto.BalanceResponse;
import com.splitwise.entity.Group;
import com.splitwise.entity.User;
import com.splitwise.repository.GroupRepository;
import com.splitwise.repository.UserRepository;
import com.splitwise.service.EmailService;
import com.splitwise.service.BalanceService;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WeeklyDebtReminderJob {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final BalanceService balanceService;
    private final EmailService emailService;

@Scheduled(cron = "0 0 9 ? * SUN")
public void sendWeeklyDebtReminders() {

    System.out.println("Running weekly debt reminder job...");

    List<Group> groups = groupRepository.findAll();

    for (Group group : groups) {

        System.out.println("Checking group: " + group.getName());

        List<BalanceResponse> balances =
                balanceService.computeBalances(group.getId());

        System.out.println("Balances found: " + balances.size());

        for (BalanceResponse balance : balances) {

            System.out.println("Processing balance: " + balance);

            User debtor = userRepository
                    .findById(balance.getFromUserId())
                    .orElse(null);

            User creditor = userRepository
                    .findById(balance.getToUserId())
                    .orElse(null);

            if (debtor == null || creditor == null) {
                System.out.println("User missing, skipping...");
                continue;
            }

            System.out.println("Sending email to: " + debtor.getEmail());

            String message =
                    "Hi " + debtor.getName() + ",\n\n" +
                    "You owe ₹" + balance.getAmount() +
                    " to " + creditor.getName() +
                    " in group " + group.getName() + ".\n\n" +
                    "Please settle it soon.";

            emailService.sendDebtReminder(debtor.getEmail(), message);
        }
    }
}
}