package com.web.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.web.dto.Board;
import com.web.dto.Comment;
import com.web.repository.BoardRepository;
import com.web.repository.CommentRepository;
import com.web.service.CommentService;

import jakarta.transaction.Transactional;


@Service
@Transactional
public class CommentServiceImpl implements CommentService {
	
	@Autowired
	BoardRepository boardRepository;
	@Autowired
	CommentRepository commentRepository;
	@Override
	public Comment writeComment(Long boardSeq, Comment comment) {
			
		Board board = boardRepository.findById(boardSeq).orElseThrow(() -> {
			return new IllegalArgumentException("게시글을 찾을 수 없습니다!");
		});
	
		if (comment.getParent() != null && comment.getParent().getId() != null) {
			// Find the parent comment by ID
			Comment parentComment = findComment(comment.getParent().getId());
			
			if (parentComment == null) {
				throw new IllegalArgumentException("부모 댓글을 찾을 수 없습니다!");
			}

			parentComment.getId();

			comment.setParent(parentComment);	       
			
			parentComment.getChild().add(comment);

			//child 추가된 parent 업데이트
			commentRepository.save(parentComment);
			commentRepository.flush(); 
			System.out.println("flushed parentComment !!! -----> " + parentComment);
			
		} else {
			//부모댓글 없다면 새 댓글
			comment.setBoard(board);
			// Save the new comment
			System.out.println("No parent, new comment................. : " + comment);
			commentRepository.save(comment);
		}    
		
		System.out.println("successfully returning comment=============>" + comment);
		return comment;
	}
	
	//부모댓글 구하기
	@Override
	public List<Comment> getComments(Long boardSeq) {
		List<Comment> comments = commentRepository.findAllByBoardSeq(boardSeq);
		System.out.println("enter getComment.......................................");
		
		System.out.println("Returning comments: " + comments.size() + " for seq: " + boardSeq);
		System.out.println("comments : " + comments);
		return comments;
	}
	
	//자식댓글 구하기
	@Override
	public List<Comment> getChildComments(Long parentId) {
		Comment parentComment = findComment(parentId);
		
		if (parentComment != null) {
			List<Comment> childComments = parentComment.getChild();
			return childComments;
		} else {
			return null;
		}
		
	}
	
	//댓글 삭제
	@Override
	public String deleteComment(Long commentId) {
		Comment comment = commentRepository.findById(commentId).orElseThrow(() -> {
			return new IllegalArgumentException("댓글 id를 찾을 수 없습니다!");
		});
		
		if(comment != null) {
			commentRepository.deleteById(commentId);			
		}
		return "댓글 삭제 완료!";
	}
	
	
	//부모댓글 찾기
	@Override
	public Comment findComment(Long id) {
		
		return commentRepository.findById(id).orElse(null);
	}
	
	


}
