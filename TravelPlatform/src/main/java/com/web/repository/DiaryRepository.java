package com.web.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.web.dto.DiaryDTO;
import com.web.model.DiaryEntity;

import jakarta.transaction.Transactional;

@Repository
public interface DiaryRepository extends JpaRepository<DiaryEntity, Long> {

   // 사용자의 이메일을 기반으로 여행 일지를 내림차순으로 조회하는 메서드
   Page<DiaryEntity> findByEmailOrderByListNumberDesc(String email, Pageable pageable);

   List<DiaryEntity> findByEmailOrderByListNumberDesc(String email);

   List<DiaryEntity> findTop4ByOrderByListNumberDesc(Pageable pageable);

   @Transactional
   @Modifying
   @Query("UPDATE DiaryEntity d SET d.listNumber = d.listNumber - 1 WHERE d.listNumber > :listNumber")
   void updateListNumbersAfterDelete(@Param("listNumber") Long listNumber);

}
