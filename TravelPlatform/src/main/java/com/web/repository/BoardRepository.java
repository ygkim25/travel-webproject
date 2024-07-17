package com.web.repository;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.web.dto.Board;


@Repository
public interface BoardRepository extends JpaRepository<Board, Long>{
	
	//getBoard, insertBoard, updateBoard, deleteBoard, = CrudRepository 메서드 사용	
	
	@Modifying
	@Transactional 
	@Query(value="update Board b set b.cnt = b.cnt+1 where b.seq=:seq ", nativeQuery = false)
	int updateReadCount(@Param("seq") Long seq);
	
//	Board findbyemail(String writer);
	
	// select * from board where title like '%xxx%';
	Page<Board> findByTitleContaining(String title, Pageable pageable);
	Page<Board> findByWriterContaining(String writer, Pageable pageable);
	Page<Board> findByContentContaining(String content, Pageable pageable);

	// 마이페이지 게시글, 찜 목록 조회
//	List<Board> findByWriter(String writer);
	Page<Board> findByWriter(String writer, Pageable pageable);
	

	
}
