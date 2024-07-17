package com.web.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.web.dto.Board;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="user_wishlist")
public class UserWishList {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	private User user;
	private String email;
	
	@ManyToOne
	private Board board;
	
	@CreationTimestamp
    private LocalDateTime  createDate;
}
