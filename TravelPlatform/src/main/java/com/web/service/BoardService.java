package com.web.service;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.web.dto.Board;
//import com.web.dto.Member;
import com.web.model.User;

@Service
public interface BoardService {
	
	void insertBoard(Board board);
	void updateBoard(Board board);
	void deleteBoard(Board board);
	
	Board getBoard(Board board);
	//Board getBoard(Board board, Member memeber);
	long getTotalRowCount(Board board);
	Page<Board> getBoardList(Pageable pageable, String searchType, String searchWord);
	
	List<Board> getRecommendedBoards(User user);
	List<Board> getMyBoard(User user);

	
	Page<Board> getUserPost(String email, Pageable pageable);
    Page<Board> getUserWishlist(User user, Pageable pageable);
    
	List<Board> getUserPost(String email);
    List<Board> getUserWishlist(User user);
	
    List<Board> findByWriter(String writer);
    Page<Board> getBoardListSearch(Pageable pageable, String searchWord);
	

}
