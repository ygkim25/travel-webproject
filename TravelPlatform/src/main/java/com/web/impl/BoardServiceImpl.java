package com.web.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.web.dto.Board;
import com.web.repository.BoardRepository;
import com.web.repository.LikeRepository;
import com.web.repository.UserRepository;
import com.web.repository.UserWishListRepository;
import com.web.service.BoardService;
import com.web.model.LikeEntity;
import com.web.model.User;
import com.web.model.UserWishList;

@Service
public class BoardServiceImpl implements BoardService {

	@Autowired
	private BoardRepository boardRepository;
	@Autowired
	private LikeRepository likeRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private UserWishListRepository userWishListRepository;
	
	@Override
	public void insertBoard(Board board) {
		boardRepository.save(board);
		boardRepository.flush();

	}

	@Override
	public void updateBoard(Board board) {
		Board findBoard = boardRepository.findById(board.getSeq()).get();

		System.out.println("Before update, commentCount: " + findBoard.getCommentCount());
		System.out.println("findBoard : " + findBoard);
		findBoard.setTitle(board.getTitle());
		findBoard.setContent(board.getContent());

		// 파일 첨부했을 경우 업데이트
		List<String> processedFileNames = board.getProcessedFileNames();
		if (processedFileNames.size() > 0) {
			findBoard.setProcessedFileNames(processedFileNames);
		}

		System.out.println("After updating title and content, commentCount: " + findBoard.getCommentCount());

		boardRepository.save(findBoard);
		boardRepository.flush();

	}

	@Override
	public void deleteBoard(Board board) {
		boardRepository.deleteById(board.getSeq());
	}

// 
	@Override
	public Board getBoard(Board board) {
		Optional<Board> findBoard = boardRepository.findById(board.getSeq());
		System.out.println("board : " + board); // seq 이외 null
		System.out.println("findBoard : " + findBoard);

		if (findBoard.isPresent()) {

			board = findBoard.get();

			boardRepository.updateReadCount(board.getSeq());
//			System.out.println("조회수 count success !!!!");

			return board;
		} else {
			return null;
		}
	}

	@Override
	public long getTotalRowCount(Board board) {
		return boardRepository.count();
	}

	@Override
	public Page<Board> getBoardList(Pageable pageable, String searchType, String searchWord) {

		if (searchType != null && !searchType.isEmpty() && searchWord != null && !searchWord.isEmpty()) {

			if (searchType.equalsIgnoreCase("Title")) {
				return boardRepository.findByTitleContaining(searchWord, pageable);
			} else if (searchType.equalsIgnoreCase("Writer")) {
				return boardRepository.findByWriterContaining(searchWord, pageable);
			} else {
				return boardRepository.findByContentContaining(searchWord, pageable);
			}
		} else {

			System.out.println("No search criteria applied.");
			return boardRepository.findAll(pageable);
		}

	}
	
	@Override
	public Page<Board> getBoardListSearch(Pageable pageable, String searchWord) {

		return boardRepository.findByTitleContaining(searchWord, pageable);
	}

	@Override
	public List<Board> getRecommendedBoards(User user) {
		List<LikeEntity> userLikes = likeRepository.findByUser(user);
		List<Board> likedBoards = userLikes.stream().map(LikeEntity::getBoard).collect(Collectors.toList());
		System.out.println("사용자가 좋아요 누른 게시글 ::::" + likedBoards);

		List<User> otherUsers = likedBoards.stream().flatMap(board -> likeRepository.findByBoard(board).stream())
				.map(LikeEntity::getUser).distinct().filter(otherUser -> !otherUser.equals(user))
				.collect(Collectors.toList());
		System.out.println("좋아요 누른 다른 유저 ::::" + otherUsers);

		Set<Board> recommendedBoards = new HashSet<>();
		for (User otherUser : otherUsers) {
			List<LikeEntity> otherUserLikes = likeRepository.findByUser(otherUser);
			List<Board> boardsLikedByOtherUser = otherUserLikes.stream().map(LikeEntity::getBoard)
					.collect(Collectors.toList());
			recommendedBoards.addAll(boardsLikedByOtherUser);
			System.out.println("다른 유저가 좋아요 누른 게시글 (" + otherUser.getEmail() + ") ::::" + boardsLikedByOtherUser);
		}
		System.out.println("다른 유저가 좋아요 누른 게시글::::" + recommendedBoards);

		List<Board> recommendedBoardList = recommendedBoards.stream().distinct().collect(Collectors.toList());
		Collections.shuffle(recommendedBoardList);
		System.out.println("랜덤 추천 게시글 :::" + recommendedBoardList);

		return recommendedBoardList.stream().limit(5).collect(Collectors.toList());
	}

	@Override
	public List<Board> getMyBoard(User user) {

		return null;
	}

//	@Override
//	public Page<Board> getUserPost(String email, Pageable pageable) {
//		return boardRepository.findByWriter(email, pageable);
//	}
//
//
//	@Override
//    public Page<Board> getUserWishlist(User user, Pageable pageable) {
//        List<UserWishList> userWishList = userWishListRepository.findByUser(user);
//        List<Board> likedBoards = userWishList.stream().map(UserWishList::getBoard).collect(Collectors.toList());
//        // return likedBoards;
////         Convert List<Board> to Page<Board>
//        int start = (int) pageable.getOffset();
//        int end = Math.min(start + pageable.getPageSize(), likedBoards.size());
//        List<Board> subList = likedBoards.subList(start, end);
//
//        return new PageImpl<>(subList, pageable, likedBoards.size());
//    }

	@Override
	public Page<Board> getUserPost(String email, Pageable pageable) {
		return boardRepository.findByWriter(email, pageable);
	}

	@Override
	public Page<Board> getUserWishlist(User user, Pageable pageable) {
		List<UserWishList> userWishList = userWishListRepository.findByUser(user);

	    // `List<Board>`로 변환
	    List<Board> boards = userWishList.stream().map(UserWishList::getBoard).collect(Collectors.toList());

	    // `PageImpl`을 사용하여 `Page<Board>` 객체 생성
	    int start = (int) pageable.getOffset();
	    int end = Math.min((start + pageable.getPageSize()), boards.size());
	    Page<Board> page = new PageImpl<>(boards.subList(start, end), pageable, boards.size());

	    return page;
	}

	@Override
    public List<Board> getUserWishlist(User user) {
        List<UserWishList> userWishList = userWishListRepository.findByUser(user);
        List<Board> likedBoards = userWishList.stream().map(UserWishList::getBoard).collect(Collectors.toList());
         return likedBoards;
//         Convert List<Board> to Page<Board>
	}
	@Override
	public List<Board> findByWriter(String writer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Board> getUserPost(String email) {
		// TODO Auto-generated method stub
		return null;
	}

}
