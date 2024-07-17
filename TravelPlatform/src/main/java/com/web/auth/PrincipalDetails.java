package com.web.auth;

import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.web.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

// Security Session => Authentication => UserDetails
@Data
@NoArgsConstructor
public class PrincipalDetails implements UserDetails , OAuth2User {

    private User user; // 콤포지션
    private Map<String, Object> attributes;


    
    //일반 로그인
    public PrincipalDetails(User user){
        this.user = user;
    }

    //OAuth 로그인
    public PrincipalDetails(User user, Map<String, Object> attributes){
        this.user = user;
        this.attributes = attributes;
    }


    public User getUser() {
        return user;
    }

    public String getEmail() {
    	return user.getEmail();
    }
    
    public String getNickname() {
    	return user.getNickname();
    }
    public void setNickname(String nickname) {
    	user.setNickname(nickname);
    }
    public String getProvider() {
    	return user.getProvider();
    }

    // 해당 User의 권한을 리턴하는 곳!!
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        //user.getRole()이 String이라 변환해야 됨

        Collection<GrantedAuthority> collect = new ArrayList<>();

        collect.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return String.valueOf(user.getRole());
            }
        });
        return collect;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }
    

    @Override
    //이 계정 만료 되었는가? false 만료/ true 유효
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    //잠겼는가
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    //비밀번호를 오래 썼는가
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    //활성화 여부
    public boolean isEnabled() {
        return true;
    }


    ///////////////////

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return null;
    }




}
