package com.web.dto;

import java.time.LocalDate;

import com.web.model.DiaryEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class DiaryDTO {

	private Long listNumber;
	private LocalDate diaryDate;
	private String diaryTitle;
	private String diaryContent;
	private String diaryLocation;
	private String diaryImages;
	
	private String email;
	private String nickname;
	
	public DiaryDTO(Long listNumber, String diaryTitle, String diaryContent, String diaryLocation, LocalDate diaryDate, String diaryImages, String email, String nickname) {
        this.listNumber = listNumber;
        this.diaryTitle = diaryTitle;
        this.diaryContent = diaryContent;
        this.diaryLocation = diaryLocation;
        this.diaryDate = diaryDate;
        this.diaryImages = diaryImages;
        this.email = email;
        this.nickname = nickname;
    }
	
	
	public static DiaryDTO toDiaryDTO(DiaryEntity diaryEntity) {
		DiaryDTO diaryDTO = new DiaryDTO();
		diaryDTO.setListNumber(diaryEntity.getListNumber());
		diaryDTO.setDiaryDate(diaryEntity.getDiaryDate());
		diaryDTO.setDiaryTitle(diaryEntity.getDiaryTitle());
		diaryDTO.setDiaryContent(diaryEntity.getDiaryContent());
		diaryDTO.setDiaryLocation(diaryEntity.getDiaryLocation());
		diaryDTO.setDiaryImages(diaryEntity.getDiaryImages());
		diaryDTO.setEmail(diaryEntity.getEmail());
		diaryDTO.setNickname(diaryEntity.getNickname());
		return diaryDTO;
	}
	
	
}
