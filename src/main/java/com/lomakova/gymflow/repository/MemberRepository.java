package com.lomakova.gymflow.repository;

import com.lomakova.gymflow.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

    List<MemberEntity> findAllByGroup_Id(Long groupId);
}
