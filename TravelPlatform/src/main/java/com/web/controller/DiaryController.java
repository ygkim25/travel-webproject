package com.web.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.web.auth.PrincipalDetails;
import com.web.dto.DiaryDTO;
import com.web.dto.WishDiaryDTO;
import com.web.model.User;
import com.web.service.DiaryService;
import com.web.service.UserService;
import com.web.service.WishDiaryService;

import jakarta.servlet.http.HttpServletRequest;

/*import lombok.RequiredArgsConstructor;*/

@Controller
/* @RequiredArgsConstructor */
public class DiaryController {

   private final DiaryService diaryService;
   private final WishDiaryService wishDiaryService;
   private final UserService userService;

   public DiaryController(DiaryService diaryService, WishDiaryService wishDiaryService, UserService userService) {
      this.diaryService = diaryService;
      this.wishDiaryService = wishDiaryService;
      this.userService = userService;

   }

   @GetMapping("/main/traveldiary")
   public String findAll(HttpServletRequest request, @AuthenticationPrincipal PrincipalDetails principalDetails,
         @PageableDefault(page = 1, size = 5) Pageable pageable,
         @RequestParam(name = "wishpage", defaultValue = "1") int wishPage,
         @RequestParam(name = "wishsize", defaultValue = "5") int wishSize, Model model) {

      System.out.println("여행일지 ``````````````");

      boolean isLoggedIn = (principalDetails != null);
      User user = (User) request.getSession().getAttribute("user");

      String email = null;
       
      if (isLoggedIn) {
         email = principalDetails.getEmail();
         model.addAttribute("nickname", principalDetails.getNickname());
      } else if (user != null) {
         email = user.getEmail();
         model.addAttribute("nickname", user.getNickname());
      } else {
         return "redirect:/main/login";
      }
       System.out.println("이메일 ::" + email);
       
       
      // 첫 번째 리스트 페이징 처리
       Page<DiaryDTO> diaryList = diaryService.findByEmail(email, pageable); //DB email 조회 해서 email로 쓴 글 출력
      int blockLimit = 5;
      int startPage = calculateStartPage(pageable.getPageNumber(), blockLimit, diaryList.getTotalPages());
      int endPage = calculateEndPage(startPage, blockLimit, diaryList.getTotalPages());

      model.addAttribute("pageDiaryList", diaryList);
      model.addAttribute("startPage", startPage);
      model.addAttribute("endPage", endPage);

      System.out.println("회원 로그인 정보 =====> " + request.getSession().getAttribute("user"));
      System.out.println("소셜 로그인 정보 =====> " + principalDetails);
      
      List<DiaryDTO> allDiaryList = diaryService.findAllByEmail(email);
      model.addAttribute("allDiaryList", allDiaryList);
      
      // 두 번째 리스트 페이징 처리
      Pageable wishDiaryPageable;
      if (wishPage < 0) {
         wishDiaryPageable = PageRequest.of(0, wishSize); // 페이지 인덱스가 음수일 경우 0으로 설정
      } else {
         wishDiaryPageable = PageRequest.of(wishPage, wishSize);
      }
      Page<WishDiaryDTO> wishDiaryList = wishDiaryService.findByEmail(email, wishDiaryPageable); //DB email 조회 해서 email로 쓴 글 출력
      System.out.println("위시리스트" + wishDiaryList);
      int wishBlockLimit = 3;
      int wishStartPage = calculateStartPage(wishDiaryPageable.getPageNumber(), wishBlockLimit,
            wishDiaryList.getTotalPages());
      int wishEndPage = calculateEndPage(wishStartPage, wishBlockLimit, wishDiaryList.getTotalPages());

      model.addAttribute("pageWishDiaryList", wishDiaryList);
      model.addAttribute("wishStartPage", wishStartPage);
      model.addAttribute("wishEndPage", wishEndPage);

      List<WishDiaryDTO> allWishDiaryList = wishDiaryService.findAllByEmail(email);
      model.addAttribute("allWishDiaryList", allWishDiaryList);
      
       System.out.println("============나의 여행일지 이동==============");


      return "diary/traveldiary";
   }

   // 시작 페이지 계산
   private int calculateStartPage(int currentPage, int blockLimit, int totalPages) {
      return (((int) (Math.ceil((double) currentPage / blockLimit))) - 1) * blockLimit + 1;
   }

   // 끝 페이지 계산
   private int calculateEndPage(int startPage, int blockLimit, int totalPages) {
      return Math.min(startPage + blockLimit - 1, totalPages);
   }

   @GetMapping("/main/traveldiary/diarywrite")
   public String saveForm(HttpServletRequest request, @AuthenticationPrincipal PrincipalDetails principalDetails,
         Model model) {

      System.out.println("============여행일지 글쓰기 이동==============");

      boolean isLoggedIn = (principalDetails != null);
      User user = (User) request.getSession().getAttribute("user");

      if (isLoggedIn) {
         model.addAttribute("nickname", principalDetails.getNickname());
      } else if (user == null) {
         return "redirect:/main";
      } else {
         model.addAttribute("nickname", user.getNickname());
      }
      System.out.println("회원 로그인 정보 =====> " + request.getSession().getAttribute("user"));
      System.out.println("소셜 로그인 정보 =====> " + principalDetails);

      return "diary/diarywrite";
   }

   @PostMapping("/main/traveldiary/diarywrite")
   public String save(@ModelAttribute User userform, HttpServletRequest request,
         @AuthenticationPrincipal PrincipalDetails principalDetails, @ModelAttribute DiaryDTO diaryDTO,
         @ModelAttribute WishDiaryDTO wishDiaryDTO, @ModelAttribute("category") String category,
         @RequestParam("file") MultipartFile[] files, RedirectAttributes redirectAttributes, Model model) {

      User user = (User) request.getSession().getAttribute("user");

      List<String> imagePaths = new ArrayList<>();
      // 현재 디렉토리를 기준으로 절대 경로 설정
       String uploadDirectory = System.getProperty("user.dir") + "/src/main/resources/static/uploadsImages/";
      
      System.out.println("회원 로그인 정보 =====> " + request.getSession().getAttribute("user"));
      System.out.println("소셜 로그인 정보 =====> " + principalDetails);

      if (user == null && principalDetails != null) {
         user = principalDetails.getUser(); // principalDetails에 User 객체를 가져오는 메서드가 있다고 가정합니다.
      }

      if (user == null) {
         redirectAttributes.addFlashAttribute("message", "로그인이 필요합니다.");
         return "redirect:/main";
      }

      for (MultipartFile file : files) {
           if (!file.isEmpty()) {
               String originalFileName = file.getOriginalFilename();
               String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName;
               Path filePath = Paths.get(uploadDirectory + uniqueFileName);

               try {
                   Files.createDirectories(filePath.getParent());
                   file.transferTo(filePath.toFile());

                   // 디버깅 정보 출력
                   System.out.println("파일이 저장된 경로: " + filePath.toString());

                   // **수정된 부분: 타임스탬프를 URL에 추가하여 캐시 무효화**
                   imagePaths.add("/uploadsImages/" + uniqueFileName + "?t=" + System.currentTimeMillis());

               } catch (IOException e) {
                   e.printStackTrace();
                   redirectAttributes.addFlashAttribute("message", "파일 업로드 실패: " + e.getMessage());
                   return "redirect:/main/traveldiary/diarywrite";
               }
           }
       }

       String imagePathsString = String.join(",", imagePaths);
       diaryDTO.setDiaryImages(imagePathsString);

       try {
           diaryDTO.setEmail(user.getEmail());
           diaryDTO.setNickname(user.getNickname());
           System.out.println("다이어리에 저장된 이메일 ======> " + diaryDTO.getEmail());

           diaryService.save(diaryDTO);
       } catch (Exception e) {
           e.printStackTrace();
           redirectAttributes.addFlashAttribute("message", "파일 업로드 실패: " + e.getMessage());
           return "redirect:/main/traveldiary/diarywrite";
       }

      System.out.println("============글쓰기 완료==============");

      return "redirect:/main/traveldiary";
   }

   @GetMapping("/main/traveldiary/wishDiaryWrite")
   public String wishSaveForm(HttpServletRequest request, @AuthenticationPrincipal PrincipalDetails principalDetails,
         Model model) {

      System.out.println("============위시리스트 글쓰기 이동==============");

      boolean isLoggedIn = (principalDetails != null);
      User user = (User) request.getSession().getAttribute("user");

      if (isLoggedIn) {
         model.addAttribute("nickname", principalDetails.getNickname());
      } else if (user == null) {
         return "redirect:/main";
      } else {
         model.addAttribute("nickname", user.getNickname());
      }
      System.out.println("회원 로그인 정보 =====> " + request.getSession().getAttribute("user"));
      System.out.println("소셜 로그인 정보 =====> " + principalDetails);

      return "diary/wishDiaryWrite";
   }

   @PostMapping("/main/traveldiary/wishDiaryWrite")
   public String wishSave(@ModelAttribute User userform, HttpServletRequest request,
         @AuthenticationPrincipal PrincipalDetails principalDetails, @ModelAttribute WishDiaryDTO wishDiaryDTO, RedirectAttributes redirectAttributes, Model model) {

      User user = (User) request.getSession().getAttribute("user");

      System.out.println("회원 로그인 정보 =====> " + request.getSession().getAttribute("user"));
      System.out.println("소셜 로그인 정보 =====> " + principalDetails);

      if (user == null && principalDetails != null) {
         user = principalDetails.getUser(); // principalDetails에 User 객체를 가져오는 메서드가 있다고 가정합니다.
      }

      if (user == null) {
         redirectAttributes.addFlashAttribute("message", "로그인이 필요합니다.");
         return "redirect:/main";
      }
      
      try {
         wishDiaryDTO.setEmail(user.getEmail());
         wishDiaryDTO.setNickname(user.getNickname());
         System.out.println("다이어리에 저장된 이메일 ======> " + wishDiaryDTO.getEmail());
         
         wishDiaryService.save(wishDiaryDTO);
      }catch(Exception e) {
         
      }
      
      System.out.println("============위시리스트 글쓰기 완료==============");

      return "redirect:/main/traveldiary";
   }
   
   
   @GetMapping("/traveldiary/diaryUpdate/{listNumber}")
   public String updateForm(@PathVariable("listNumber") Long listNumber, HttpServletRequest request, @AuthenticationPrincipal PrincipalDetails principalDetails, Model model) {
      DiaryDTO diaryDTO = diaryService.findById(listNumber);

      boolean isLoggedIn = (principalDetails != null);
      User user = (User) request.getSession().getAttribute("user");

      if (isLoggedIn) {
         model.addAttribute("nickname", principalDetails.getNickname());
         System.out.println(principalDetails.getNickname());
      } else if (user == null) {
         return "redirect:/main";
      } else {
         model.addAttribute("nickname", user.getNickname());
         System.out.println(user.getNickname());
      }
      
      if (diaryDTO == null) {
         return "error/404"; // 에러 페이지로 이동
      }
      model.addAttribute("diaryUpdate", diaryDTO);

      return "diary/diaryUpdate";
   }

   //여행 일지 업데이트
   @PostMapping("/traveldiary/diaryUpdate")
   public String update(@ModelAttribute DiaryDTO diaryDTO, Model model,
         @RequestParam("file") MultipartFile[] files, HttpServletRequest request,
         @AuthenticationPrincipal PrincipalDetails principalDetails, RedirectAttributes redirectAttributes) {

      DiaryDTO diary = diaryService.update(diaryDTO);
      model.addAttribute("diary", diary);

      List<String> imagePaths = new ArrayList<>();
       String uploadDirectory = System.getProperty("user.dir") + "/src/main/resources/static/uploadsImages/";
      
      boolean isLoggedIn = (principalDetails != null);
      User user = (User) request.getSession().getAttribute("user");

      
      if (isLoggedIn) {
         model.addAttribute("nickname", principalDetails.getNickname());
         System.out.println(principalDetails.getNickname());
      } else if (user == null) {
         return "redirect:/main";
      } else {
         model.addAttribute("nickname", user.getNickname());
         System.out.println(user.getNickname());
      }

      System.out.println("회원 로그인 정보 =====> " + request.getSession().getAttribute("user"));
      System.out.println("소셜 로그인 정보 =====> " + principalDetails);

      if (user == null && principalDetails != null) {
         user = principalDetails.getUser(); // principalDetails에 User 객체를 가져오는 메서드가 있다고 가정합니다.
      }
      
      for (MultipartFile file : files) {
         if (!file.isEmpty()) {
            String originalFileName = file.getOriginalFilename();
            String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName;
            Path filePath = Paths.get(uploadDirectory + uniqueFileName);
            
            try {
               Files.createDirectories(filePath.getParent());
               file.transferTo(filePath.toFile());
               
               // 디버깅 정보 출력
               System.out.println("파일이 저장된 경로: " + filePath.toString());
               imagePaths.add("/uploadsImages/" + uniqueFileName);
               
               
            } catch (IOException e) {
               e.printStackTrace();
               redirectAttributes.addFlashAttribute("message", "파일 업로드 실패: " + e.getMessage());
               return "redirect:/main/traveldiary/diarywrite";
            }
         }
      }
      
      String imagePathsString = String.join(",", imagePaths);
      diaryDTO.setDiaryImages(imagePathsString);
      
      try {
         diaryDTO.setEmail(user.getEmail());
         diaryDTO.setNickname(user.getNickname());
         System.out.println("다이어리에 저장된 이메일 ======> " + diaryDTO.getEmail());
         
         diaryService.save(diaryDTO);
      } catch(Exception e) {
         e.printStackTrace();
         redirectAttributes.addFlashAttribute("message", "파일 업로드 실패: " + e.getMessage());
         return "redirect:/main/traveldiary/diarywrite";
      }
      
      return "redirect:/main/traveldiary";
      // return "redirect:/board/" + boardDTO.getId();
   }

   @GetMapping("/traveldiary/wishDiaryUpdate/{listNumber}")
   public String updateWishForm(@PathVariable("listNumber") Long listNumber, HttpServletRequest request, @AuthenticationPrincipal PrincipalDetails principalDetails, Model model) {
      WishDiaryDTO wishDiaryDTO = wishDiaryService.findById(listNumber);
      if (wishDiaryDTO == null) {
         return "error/404"; // 에러 페이지로 이동
      }
      
      boolean isLoggedIn = (principalDetails != null);
      User user = (User) request.getSession().getAttribute("user");

      if (isLoggedIn) {
         model.addAttribute("nickname", principalDetails.getNickname());
         System.out.println(principalDetails.getNickname());
      } else if (user == null) {
         return "redirect:/main";
      } else {
         model.addAttribute("nickname", user.getNickname());
         System.out.println(user.getNickname());
      }
      
      model.addAttribute("wishDiaryUpdate", wishDiaryDTO);
      return "diary/wishDiaryUpdate";
   }

   // 위시리스트 업데이트
   @PostMapping("/traveldiary/wishDiaryUpdate")
   public String wishUpdate(@ModelAttribute WishDiaryDTO wishDiaryDTO, Model model,HttpServletRequest request,
         @AuthenticationPrincipal PrincipalDetails principalDetails) {
      WishDiaryDTO wishDiary = wishDiaryService.wishUpdate(wishDiaryDTO);

      boolean isLoggedIn = (principalDetails != null);
      User user = (User) request.getSession().getAttribute("user");

      if (isLoggedIn) {
         model.addAttribute("nickname", principalDetails.getNickname());
         System.out.println(principalDetails.getNickname());
      } else if (user == null) {
         return "redirect:/main";
      } else {
         model.addAttribute("nickname", user.getNickname());
         System.out.println(user.getNickname());
      }
      
      if (user == null && principalDetails != null) {
         user = principalDetails.getUser(); // principalDetails에 User 객체를 가져오는 메서드가 있다고 가정합니다.
      }
      
      wishDiaryDTO.setEmail(user.getEmail());
      wishDiaryDTO.setNickname(user.getNickname());
      model.addAttribute("wishDiary", wishDiary);
      wishDiaryService.save(wishDiaryDTO);
      return "redirect:/main/traveldiary";
      // return "redirect:/board/" + boardDTO.getId();
   }

   @GetMapping("/traveldiary/diaryDelete/{listNumber}")
   public String delete(@PathVariable("listNumber") Long listNumber) {
      System.out.println(listNumber);
      diaryService.delete(listNumber);
      return "redirect:/main/traveldiary";
   }

   @GetMapping("/traveldiary/wishDiaryDelete/{listNumber}")
   public String wishDelete(@PathVariable("listNumber") Long listNumber) {
      System.out.println(listNumber);
      wishDiaryService.delete(listNumber);
      return "redirect:/main/traveldiary";
   }

}