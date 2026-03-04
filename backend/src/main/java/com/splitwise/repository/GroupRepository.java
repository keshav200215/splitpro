package com.splitwise.repository;

import com.splitwise.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByIdIn(List<Long> ids);
}