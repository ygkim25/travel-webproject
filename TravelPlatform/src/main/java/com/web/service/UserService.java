package com.web.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import com.web.config.encoderService;
import com.web.dto.Role;
import com.web.model.LikeEntity;
import com.web.model.User;
import com.web.repository.LikeRepository;
import com.web.repository.UserRepository;
import com.web.repository.UserWishListRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private LikeRepository likeRepository;
	@Autowired
	private UserWishListRepository userWishListRepository;
	
	@Autowired
    encoderService encoderService;
	
	@Autowired
	MailService mailService;
	

	
	// 회원가입 로직 (비밀번호 암호화)
    public void registerUser(User user) {
    	
    	user.setRole(Role.ROLE_USER);
    	
        String encodedPassword = encoderService.passwordEncoder().encode(user.getPassword());
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }
    
    public void findPassword(User user) {
    	
    	String encodePassword = encoderService.passwordEncoder()
    			.encode(String.valueOf(mailService.getVerificationNumber()));
    	user.setPassword(encodePassword);
    	userRepository.save(user);
    }
    
    // 로그인 
	public User login(String email, String password) {
        User user = userRepository.findByemail(email);
        if (user != null && encoderService.passwordEncoder().matches(password,user.getPassword())) {
            System.out.println("로그인 이메일: " + user.getEmail());
            return user;
        }else {
        	System.out.println("로그인 실패 - 이메일: " + email);
        	return null;
        }
    }
	// 회원 탈퇴
	@Transactional
	public void deleteUser(User user) {
		if(user != null) {
			likeRepository.deleteByUser(user);
			userWishListRepository.deleteByUser(user);
			userRepository.delete(user);	
			

		}
	}
	// 게시글 삭제
	@Transactional
    public void deleteByBoardSeq(Long boardSeq) {
		likeRepository.deleteByBoardSeq(boardSeq);
		userWishListRepository.deleteByBoardSeq(boardSeq);
    }

	public boolean changePassword(String email, String password, String newPassword) {

	        User user = userRepository.findByemail(email);

	        if (user != null && encoderService.passwordEncoder().matches(password, user.getPassword())) {
	            user.setPassword(encoderService.passwordEncoder().encode(newPassword));
	            userRepository.save(user);
	            return true;
	        }else {
	        	return false;
	        }

	    
	}

	// DB email 조회
	public User findUserByEmail(String email) {
	    return userRepository.findByemail(email);
	}
	public User findUserByName(String username) {
		return userRepository.findByUsername(username);
	}
	// DB nickname 조회
	public boolean existsByNickname(String nickname) {
		return userRepository.existsByNickname (nickname);
	}
	
}
