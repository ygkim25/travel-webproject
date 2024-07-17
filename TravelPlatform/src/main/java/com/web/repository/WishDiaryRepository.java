package com.web.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.web.model.DiaryEntity;
import com.web.model.WishDiaryEntity;

import jakarta.transaction.Transactional;

@Repository
public interface WishDiaryRepository extends JpaRepository<WishDiaryEntity, Long>{
   
   Page<WishDiaryEntity> findByEmailOrderByListNumberDesc(String email, Pageable wishDiaryPageable);
   
   List<WishDiaryEntity> findByEmailOrderByListNumberDesc(String email);
   
   @Transactional
    @Modifying
    @Query("UPDATE WishDiaryEntity d SET d.listNumber = d.listNumber - 1 WHERE d.listNumber > :listNumber")
    void updateListNumbersAfterDelete(@Param("listNumber") Long listNumber);
   
}
