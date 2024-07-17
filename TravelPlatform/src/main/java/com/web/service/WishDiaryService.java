package com.web.service;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.web.dto.DiaryDTO;
import com.web.dto.WishDiaryDTO;
import com.web.model.DiaryEntity;
import com.web.model.WishDiaryEntity;
import com.web.repository.WishDiaryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WishDiaryService {

    private final WishDiaryRepository wishDiaryRepository;

    public void save(WishDiaryDTO wishDiaryDTO) {
        WishDiaryEntity wishDiaryEntity = WishDiaryEntity.toWishDiaryEntity(wishDiaryDTO);
        wishDiaryRepository.save(wishDiaryEntity);
    }

    public List<WishDiaryDTO> findAll() {
        List<WishDiaryEntity> wishDiaryEntityList = wishDiaryRepository.findAll();
        List<WishDiaryDTO> wishDiaryDTOList = new ArrayList<>();
        for (WishDiaryEntity wishDiaryEntity : wishDiaryEntityList) {
            wishDiaryDTOList.add(WishDiaryDTO.toWishDiaryDTO(wishDiaryEntity));
        }
        return wishDiaryDTOList;
    }

    public WishDiaryDTO findById(Long listNumber) {
        Optional<WishDiaryEntity> optionalWishDiaryEntity = wishDiaryRepository.findById(listNumber);
        if (optionalWishDiaryEntity.isPresent()) {
            WishDiaryEntity wishDiaryEntity = optionalWishDiaryEntity.get();
            return WishDiaryDTO.toWishDiaryDTO(wishDiaryEntity);
        } else {
            throw new RuntimeException("Diary not found with listNumber: " + listNumber);
        }
    }

    public WishDiaryDTO wishUpdate(WishDiaryDTO wishDiaryDTO) {
        WishDiaryEntity wishDiaryEntity = WishDiaryEntity.toUpdateWishEntity(wishDiaryDTO);
        wishDiaryRepository.save(wishDiaryEntity);
        return findById(wishDiaryDTO.getListNumber());
    }

    public void delete(Long listNumber) {
        wishDiaryRepository.deleteById(listNumber);
        wishDiaryRepository.updateListNumbersAfterDelete(listNumber);
    }

    public Page<WishDiaryDTO> wishPaging(Pageable wishDiaryPageable) {
        int wishpage = wishDiaryPageable.getPageNumber() - 1;
        int wishpageLimit = 5;
        Page<WishDiaryEntity> wishDiaryEntities = wishDiaryRepository.findAll(PageRequest.of(wishpage, wishpageLimit, Sort.by(Sort.Direction.DESC, "listNumber")));

        return wishDiaryEntities.map(wishDiary -> new WishDiaryDTO(
                wishDiary.getListNumber(),
                wishDiary.getDiaryTitle(),
                wishDiary.getDiaryContent(),
                wishDiary.getDiaryLocation(),
                wishDiary.getDiaryDate(),
                wishDiary.getEmail(),
                wishDiary.getNickname()
        ));
    }

    // 이메일을 기반으로 사용자가 작성한 여행 일지를 조회하는 메서드
    public Page<WishDiaryDTO> findByEmail(String email, Pageable wishDiaryPageable) {
        int wishpage = wishDiaryPageable.getPageNumber() - 1;
        int wishPageLimit = 5;
        Page<WishDiaryEntity> wishDiaryEntities = wishDiaryRepository.findByEmailOrderByListNumberDesc(email, PageRequest.of(wishpage, wishPageLimit));

        return wishDiaryEntities.map(wishDiary -> new WishDiaryDTO(
              wishDiary.getListNumber(),
              wishDiary.getDiaryTitle(),
              wishDiary.getDiaryContent(),
              wishDiary.getDiaryLocation(),
                wishDiary.getDiaryDate(),
                wishDiary.getEmail(),
                wishDiary.getNickname()
        ));
    }
public List<WishDiaryDTO> findAllByEmail(String email) {
        
       List<WishDiaryEntity> wishDiaryEntities = wishDiaryRepository.findByEmailOrderByListNumberDesc(email);

        return wishDiaryEntities.stream()
              .map(wishDiary -> new WishDiaryDTO(
                wishDiary.getListNumber(),
                wishDiary.getDiaryTitle(),
                wishDiary.getDiaryContent(),
                wishDiary.getDiaryLocation(),
                  wishDiary.getDiaryDate(),
                  wishDiary.getEmail(),
                  wishDiary.getNickname()
              ))
              .collect(Collectors.toList());
    }
}
