package com.splitwise.repository;

import com.splitwise.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    List<GroupMember> findByUserId(Long userId);

    List<GroupMember> findByGroupId(Long groupId);

    @Query("SELECT COUNT(g) > 0 FROM GroupMember g WHERE g.groupId = :groupId AND g.userId = :userId")
    boolean existsByGroupIdAndUserId(Long groupId, Long userId);
}