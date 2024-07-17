package com.web.auth;

import com.web.model.User;
import com.web.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/// 시큐리티 설정에서 loginProcessingUrl("/login");
//login 요청이 오면 자동으로 UserDetailsService 타입으로 ioc되어있는 loadUserByUsername 함수 실행
@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User findUser = userRepository.findByUsername(username);
        if(findUser!=null) {
            return new PrincipalDetails(findUser);
        }

        return null;
	}

}
