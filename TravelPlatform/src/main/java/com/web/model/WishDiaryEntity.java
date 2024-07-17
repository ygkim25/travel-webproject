package com.web.model;

import java.time.LocalDate;

import com.web.dto.WishDiaryDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "mywishlist")
public class WishDiaryEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) 
	private Long listNumber;
	
	@Column(unique = true)
	private String diaryTitle;
	
	
	@Column(columnDefinition = "TEXT")
	private String diaryContent;
	
	@Column
	private String diaryLocation;
	
	@Column
	private String email;
	@Column
	private String nickname;
	
	@Column
	private LocalDate diaryDate;
	
	public static WishDiaryEntity toWishDiaryEntity(WishDiaryDTO wishDiaryDTO) {
		WishDiaryEntity wishDiaryEntity = new WishDiaryEntity();
		wishDiaryEntity.setListNumber(wishDiaryDTO.getListNumber());
		wishDiaryEntity.setDiaryDate(wishDiaryDTO.getDiaryDate());
		wishDiaryEntity.setDiaryTitle(wishDiaryDTO.getDiaryTitle());
		wishDiaryEntity.setDiaryContent(wishDiaryDTO.getDiaryContent());
		wishDiaryEntity.setDiaryLocation(wishDiaryDTO.getDiaryLocation());
		wishDiaryEntity.setEmail(wishDiaryDTO.getEmail());
		wishDiaryEntity.setNickname(wishDiaryDTO.getNickname());
		return wishDiaryEntity;
	}

	public static WishDiaryEntity toUpdateWishEntity(WishDiaryDTO wishDiaryDTO) {
		WishDiaryEntity wishDiaryEntity = new WishDiaryEntity();
		wishDiaryEntity.setListNumber(wishDiaryDTO.getListNumber());
		wishDiaryEntity.setDiaryDate(wishDiaryDTO.getDiaryDate());
		wishDiaryEntity.setDiaryTitle(wishDiaryDTO.getDiaryTitle());
		wishDiaryEntity.setDiaryContent(wishDiaryDTO.getDiaryContent());
		wishDiaryEntity.setDiaryLocation(wishDiaryDTO.getDiaryLocation());
		wishDiaryEntity.setEmail(wishDiaryDTO.getEmail());
		wishDiaryEntity.setNickname(wishDiaryDTO.getNickname());
        return wishDiaryEntity;
		
	}
}
