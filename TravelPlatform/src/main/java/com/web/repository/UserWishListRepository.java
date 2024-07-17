package com.web.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.web.dto.Board;
import com.web.model.User;
import com.web.model.UserWishList;

import jakarta.transaction.Transactional;

@Repository
public interface UserWishListRepository extends JpaRepository<UserWishList, Long>{
	
	boolean existsByUserAndBoard(User user, Board board);
	List<UserWishList> findByUser(User user);
	
	void deleteByUser(User user);
	
	@Modifying
    @Query("DELETE FROM UserWishList l WHERE l.board.seq = :boardSeq")
    void deleteByBoardSeq(@Param("boardSeq") Long boardSeq);
	
//	@Modifying
//    @Transactional
//    @Query("DELETE FROM UserWishList uwl WHERE uwl.user.id = :userId")
//    void deleteByUserId(@Param("userId") Long userId);
}
