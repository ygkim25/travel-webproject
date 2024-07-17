package com.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.web.dto.Board;
import com.web.model.User;
import com.web.model.UserWishList;
import com.web.repository.BoardRepository;
import com.web.repository.UserRepository;
import com.web.repository.UserWishListRepository;

import jakarta.transaction.Transactional;

@Service
public class UserWishListService {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BoardRepository boardRepository;
	
	@Autowired
	private UserWishListRepository userWishListRepository;


}
