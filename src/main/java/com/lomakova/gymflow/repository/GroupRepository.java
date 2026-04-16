package com.lomakova.gymflow.repository;

import com.lomakova.gymflow.entity.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, Long> {
    boolean existsByName(String name);
}
