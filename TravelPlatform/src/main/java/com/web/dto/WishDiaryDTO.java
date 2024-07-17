package com.web.dto;

import java.time.LocalDate;

import com.web.model.WishDiaryEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class WishDiaryDTO {

	private Long listNumber;
	private LocalDate diaryDate;
	private String diaryTitle;
	private String diaryContent;
	private String diaryLocation;
	
	private String email;
	private String nickname;
	
	
	public WishDiaryDTO(Long listNumber, String diaryTitle, String diaryContent, String diaryLocation, LocalDate diaryDate, String email, String nickname) {
        this.listNumber = listNumber;
        this.diaryTitle = diaryTitle;
        this.diaryContent = diaryContent;
        this.diaryLocation = diaryLocation;
        this.diaryDate = diaryDate;
        this.email = email;
        this.nickname = nickname;
    }
	
	
	public static WishDiaryDTO toWishDiaryDTO(WishDiaryEntity wishDiaryEntity) {
		WishDiaryDTO wishDiaryDTO = new WishDiaryDTO();
		wishDiaryDTO.setListNumber(wishDiaryEntity.getListNumber());
		wishDiaryDTO.setDiaryDate(wishDiaryEntity.getDiaryDate());
		wishDiaryDTO.setDiaryTitle(wishDiaryEntity.getDiaryTitle());
		wishDiaryDTO.setDiaryContent(wishDiaryEntity.getDiaryContent());
		wishDiaryDTO.setDiaryLocation(wishDiaryEntity.getDiaryLocation());
		wishDiaryDTO.setEmail(wishDiaryEntity.getEmail());
		wishDiaryDTO.setNickname(wishDiaryEntity.getNickname());
		return wishDiaryDTO;
	}
	
	
}
