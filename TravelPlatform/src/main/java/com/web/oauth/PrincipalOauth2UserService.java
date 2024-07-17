package com.web.oauth;

import com.web.auth.PrincipalDetails;
import com.web.config.encoderService;
import com.web.dto.Role;
import com.web.model.User;
import com.web.provider.FacebookUserInfo;
import com.web.provider.GoogleUserInfo;
import com.web.provider.KakaoUserInfo;
import com.web.provider.NaverUserInfo;
import com.web.provider.OAuth2UserInfo;
import com.web.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    encoderService encoderService;
    //구글로 받은 userRequest 데이터에 대해 후처리하는 함수
    
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        
    	//"registraionId" 로 어떤 OAuth 로 로그인 했는지 확인 가능(google,naver등)
    	System.out.println("getClientRegistration : " + userRequest.getClientRegistration());
        System.out.println("getAccessToken   : " + userRequest.getAccessToken());
        System.out.println("getAttributes : " + super.loadUser(userRequest).getAttributes());

        
        
//        OAuth 로그인 회원가입
        OAuth2User oAuth2User = super.loadUser(userRequest);
        OAuth2UserInfo oAuth2Userinfo = null;

        if(userRequest.getClientRegistration().getRegistrationId().equals("google")) {
            oAuth2Userinfo = new GoogleUserInfo(oAuth2User.getAttributes());
        }
        else if(userRequest.getClientRegistration().getRegistrationId().equals("facebook")) {
            oAuth2Userinfo = new FacebookUserInfo(oAuth2User.getAttributes());
        }
        else if(userRequest.getClientRegistration().getRegistrationId().equals("naver")) {
            oAuth2Userinfo = new NaverUserInfo((Map)oAuth2User.getAttributes().get("response"));
        }
        else if(userRequest.getClientRegistration().getRegistrationId().equals("kakao")) {
            oAuth2Userinfo = new KakaoUserInfo((Map)oAuth2User.getAttributes().get("kakao_account"),
                                String.valueOf(oAuth2User.getAttributes().get("id")));
        }
        else{
            System.out.println("지원하지 않은 로그인 서비스 입니다.");
        }

        String provider = oAuth2Userinfo.getProvider(); // google, naver, kakao
        String providerId = oAuth2Userinfo.getProviderId();
        String username = provider + "_" + providerId; // google_123213213
        String password = encoderService.passwordEncoder().encode("test");
        String email = oAuth2Userinfo.getEmail();
        Role role = Role.ROLE_USER;

        User userEntity = userRepository.findByUsername(username);

        if(userEntity == null){
        	LocalDateTime createTime = LocalDateTime.now();
            userEntity = User.builder()
                    .username(username)
                    .password(password)
                    .email(email)
                    .role(role)
                    .provider(provider)
                    .providerId(providerId)
                    .createDate(createTime)
                    .nickname("")
                    .build();
            userRepository.save(userEntity);
        }else{
        	System.out.println(email);
        }

        return new PrincipalDetails(userEntity,oAuth2User.getAttributes());
    }
    
   
}
