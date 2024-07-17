package com.web.service;


import java.util.List;

import org.springframework.stereotype.Service;

import com.web.dto.Comment;

import jakarta.transaction.Transactional;


@Service
public interface CommentService {
	
	
	//댓글 작성
	@Transactional
	public Comment writeComment(Long boardSeq, Comment comment);
	
	//게시글 부모댓글 불러오기
	@Transactional
	public List<Comment> getComments(Long boardSeq);
	//게시글 자식댓글 불러오기
	@Transactional
	public List<Comment> getChildComments(Long parentId);
	
	//댓글 삭제
	@Transactional
	public String deleteComment(Long commentId);

	
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	
	//부모댓글 찾기
	public Comment findComment(Long id);
	

}
