package com.web.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import com.web.dto.Role;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String email;
    private String confirmPassword;
    private String phone_number;
    private String userBirth;
    private String nickname;
    
    @Enumerated(EnumType.STRING)
    private Role role; //ADMIN, MANAGER, USER

    private String provider;
    private String providerId;
    @CreationTimestamp
    private LocalDateTime  createDate;

    @Builder
    public User(String username, String password,String email, Role role, String provider, String providerId, LocalDateTime createDate, String nickname, String userBirth) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.provider = provider;
        this.providerId = providerId;
        this.createDate = createDate;
        this.nickname = nickname;
        this.userBirth = userBirth;
    }

}
