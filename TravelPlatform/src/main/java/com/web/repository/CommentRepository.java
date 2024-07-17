package com.web.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.web.dto.Comment;

//@NoRepositoryBean  //임시
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>{
	
	
	//findAllBy... 
	//SELECT c FROM Comment c WHERE c.board.seq = :boardSeq
	List<Comment> findAllByBoardSeq(Long boardSeq);
	
}
	
