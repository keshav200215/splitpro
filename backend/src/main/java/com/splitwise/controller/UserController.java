package com.splitwise.controller;

import com.splitwise.dto.UserResponse;
import com.splitwise.entity.GroupMember;
import com.splitwise.entity.User;
import com.splitwise.repository.GroupMemberRepository;
import com.splitwise.repository.UserRepository;
import com.splitwise.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;

    /* ==========================================
       GET CURRENT USER
    ========================================== */

    @GetMapping("/me")
    public UserResponse getCurrentUser() {

        User currentUser = getAuthenticatedUser();

        return UserResponse.builder()
                .id(currentUser.getId())
                .name(currentUser.getName())
                .email(currentUser.getEmail())
                .build();
    }

    /* ==========================================
       GET ASSOCIATED USERS
    ========================================== */

    @GetMapping("/associated")
    public List<UserResponse> getAssociatedUsers() {

        User currentUser = getAuthenticatedUser();

        List<GroupMember> memberships =
                groupMemberRepository.findByUserId(currentUser.getId());

        List<Long> groupIds = memberships.stream()
                .map(GroupMember::getGroupId)
                .toList();

        List<GroupMember> allMembers =
                groupMemberRepository.findAll()
                        .stream()
                        .filter(m -> groupIds.contains(m.getGroupId()))
                        .toList();

        return allMembers.stream()
                .map(m -> userRepository.findById(m.getUserId()).orElse(null))
                .filter(Objects::nonNull)
                .filter(u -> !u.getId().equals(currentUser.getId()))
                .collect(Collectors.toMap(
                        User::getId,
                        u -> u,
                        (u1, u2) -> u1
                ))
                .values()
                .stream()
                .map(u -> UserResponse.builder()
                        .id(u.getId())
                        .name(u.getName())
                        .email(u.getEmail())
                        .build())
                .toList();
    }
    @GetMapping("/search")
public List<UserResponse> searchUsers(@RequestParam String query) {

    List<User> users = userRepository
            .findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);

    return users.stream()
            .map(u -> UserResponse.builder()
                    .id(u.getId())
                    .name(u.getName())
                    .email(u.getEmail())
                    .build())
            .toList();
}

    /* ==========================================
       HELPER
    ========================================== */

    private User getAuthenticatedUser() {

        Object principal = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    
        if (principal instanceof User user) {
            return user;
        }
    
        throw new RuntimeException("User not authenticated properly");
    }
}