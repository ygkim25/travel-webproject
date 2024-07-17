package com.web.controller;

import com.web.auth.PrincipalDetails;
import com.web.config.encoderService;
import com.web.dto.Board;
import com.web.dto.DiaryDTO;
import com.web.dto.PagingInfo;
import com.web.model.LikeEntity;
import com.web.model.User;
import com.web.repository.BoardRepository;
import com.web.repository.LikeRepository;
import com.web.repository.UserRepository;
import com.web.repository.UserWishListRepository;
import com.web.service.BoardService;
import com.web.service.DiaryService;
import com.web.service.MailService;
import com.web.service.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class UserController {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private BoardRepository boardRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private MailService mailService;

	@Autowired
	encoderService encoderService;

	@Autowired
	private BoardService boardService;

	@Autowired
	private DiaryService diaryService;
	@Autowired
	private UserWishListRepository userWishListRepository;
	@Autowired
	private LikeRepository likeRepository;

	@ResponseBody
	@GetMapping("/admin")
	public String admin() {
		return "admin";
	}

	@ResponseBody
	@GetMapping("/manager")
	public String manager() {
		return "manager";
	}

	// 메인 페이지
	@GetMapping("/main")
	public String main(HttpServletRequest request, HttpSession session,
			@AuthenticationPrincipal PrincipalDetails principalDetails,
			@RequestParam(defaultValue = "0", name = "curPage") int curPage,
			@RequestParam(defaultValue = "5", name = "rowSizePerPage") int rowSizePerPage,
			@RequestParam(defaultValue = "title", name = "searchType") String searchType,
			@RequestParam(defaultValue = "", name = "searchWord") String searchWord, Model model) {
		System.out.println("============메인페이지 이동==============");

		int diaryCount = diaryService.findAll().size();
		boolean myDiaryDataEmpty = (diaryCount == 0);

		boolean isLoggedIn = (principalDetails != null);

		if (isLoggedIn) {
			model.addAttribute("isLoggedIn", true);
			model.addAttribute("username", principalDetails.getEmail());
			model.addAttribute("nickname", principalDetails.getNickname());
			System.out.println("닉네임 ::" + principalDetails.getNickname());
			if (myDiaryDataEmpty) {
				model.addAttribute("myDiaryDataEmpty", myDiaryDataEmpty); // 모델에 추가
			} else {
				List<DiaryDTO> diaryDTOlist = diaryService.findTop4ByOrderByListNumberDesc();
				model.addAttribute("diaryList", diaryDTOlist);
			}
		} else {
			User user = (User) session.getAttribute("user");
			if (user != null) {
				model.addAttribute("isLoggedIn", true);
				model.addAttribute("username", user.getUsername());
				model.addAttribute("nickname", user.getNickname());

				List<DiaryDTO> diaryDTOlist = diaryService.findTop4ByOrderByListNumberDesc();
				model.addAttribute("diaryList", diaryDTOlist);

			} else {
				model.addAttribute("isLoggedIn", false);
			}
		}

		// 전체 글
		Pageable pageable = PageRequest.of(curPage, rowSizePerPage, Sort.by("seq").descending());
		Page<Board> pagedResult = boardService.getBoardList(pageable, searchType, searchWord);
		model.addAttribute("pagedResult", pagedResult);
		// 핫한 글
		Pageable topPageable = PageRequest.of(0, 5, Sort.by(Direction.DESC, "cnt"));
		Page<Board> topResult = boardService.getBoardList(topPageable, null, null);
		model.addAttribute("topResult", topResult);

		PagingInfo pagingInfo = new PagingInfo();
		int totalRowCount = pagedResult.getNumberOfElements();
		int totalPageCount = pagedResult.getTotalPages();
		int pageSize = pagingInfo.getPageSize();
		int startPage = curPage / pageSize * pageSize + 1;
		int endPage = startPage + pageSize - 1;
		endPage = endPage > totalPageCount ? (totalPageCount > 0 ? totalPageCount : 1) : endPage;

		pagingInfo.setCurPage(curPage);
		pagingInfo.setTotalRowCount(totalRowCount);
		pagingInfo.setTotalPageCount(totalPageCount);
		pagingInfo.setStartPage(startPage);
		pagingInfo.setEndPage(endPage);
		pagingInfo.setRowSizePerPage(rowSizePerPage);

		model.addAttribute("pagingInfo", pagingInfo);
		model.addAttribute("userPostPage", pagedResult);
		model.addAttribute("curPage", curPage);
		model.addAttribute("rowSizePerPage", rowSizePerPage);

		return "main";
	}

	// 로그인 페이지
	@GetMapping("/main/login")
	public String loginPage(HttpServletRequest request) {
//	    String referer = request.getHeader("Referer");
//	    if (referer != null && !referer.contains("/main/login")) {
//	        request.getSession().setAttribute("prevPage", referer);
//	    }
		System.out.println("============로그인 페이지 이동==============");
		return "user/login";
	}

	// 로그인 성공
	@PostMapping("/main/login")
	public String loginSubmit(HttpServletRequest request, HttpSession session, @ModelAttribute User userForm,
			Model model) {
		User user = userService.login(userForm.getEmail(), userForm.getPassword());

		if (user == null) {
			model.addAttribute("loginError", "이메일 주소 또는 비밀번호가 올바르지 않습니다.");
			System.out.println("============로그인 실패==============");
			return "user/login";
		}

		session.setAttribute("user", user);
		System.out.println("============로그인 성공==============");

		// 이전 페이지로 리다이렉트
//	    String prevPage = (String) request.getSession().getAttribute("prevPage");
//	    System.out.println("저장된 페이지 ::" + prevPage);
//	    if (prevPage != null) {
//	        request.getSession().removeAttribute("prevPage");
//	        return "redirect:" + prevPage;
//	    } else {
//	        return "redirect:/main";
//	    }
		return "redirect:/main";
	}

	// 비밀번호 변경
	@GetMapping("/main/find-password")
	public String resetPassword() {

		System.out.println("============비밀번호 찾기==============");

		return "user/resetPassword";
	}

	// 비밀번호 변경
	@PostMapping("/main/find-password")
	public String resetPasswordPage(HttpServletRequest request, @ModelAttribute User userForm, Model model) {

		if (userService.findUserByEmail(userForm.getEmail()) == null) {
			model.addAttribute("emailExistError", "가입된 이메일이 아닙니다.");
			System.out.println(userForm);
			return "user/resetPassword";
		}
		User user = userService.findUserByEmail(userForm.getEmail());

		user.setPassword(String.valueOf(mailService.getVerificationNumber()));
		System.out.println("변경 전 비번: " + String.valueOf(mailService.getVerificationNumber()));
		String encodedPassword = encoderService.passwordEncoder().encode(user.getPassword());
		user.setPassword(encodedPassword);
		System.out.println("변경 후 비번: " + user.getPassword());

		request.getSession().setAttribute("user", user);
		userRepository.save(user);

		return "redirect:/main/login";
	}

	@GetMapping("/main/find-email")
	public String findEmail() {
		System.out.println("============아이디 찾기==============");
		return "user/findEmail";
	}

	@PostMapping("/main/find-email")
	public String findEmailPage(HttpServletRequest request, @ModelAttribute User user, Model model) {

		String username = user.getUsername();
		User foundUser = userService.findUserByName(username);

		if (foundUser != null) {
			model.addAttribute("email", foundUser.getEmail());
			model.addAttribute("username", foundUser.getUsername());
		} else {
			model.addAttribute("errorMessage", "가입된 이름이 아닙니다.");
		}

		return "user/findEmail";
	}

	// 회원가입 페이지
	@GetMapping("/main/register")
	public String registerPage(Model model) {
		model.addAttribute("user", new User());

		System.out.println("============회원가입 페이지 이동==============");

		return "user/register";
	}

	// 회원가입 성공
	@PostMapping("/main/register")
	public String registerSubmit(@AuthenticationPrincipal PrincipalDetails principalDetails, HttpSession session,
			@RequestParam(required = false, name = "emailVerified") Boolean emailVerified,
			@ModelAttribute User userForm, @RequestParam("verificationCode") String verificationCode, Model model) {

		String passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$";
		// 이메일 중복 오류

		if (userService.findUserByEmail(userForm.getEmail()) != null) {
			model.addAttribute("emailEqualError", "이미 가입된 이메일입니다.");
			return "user/register";
		} else if (userForm.getNickname().length() < 2) {
			// 닉네임 글자 수 오류
			model.addAttribute("nicknameLengthError", "닉네임은 최소 2글자 이상이어야 합니다.");
			return "user/register";
		} else if (userService.existsByNickname(userForm.getNickname())) {
			// 닉네임 중복 오류
			model.addAttribute("nicknameEqualError", "이미 사용중인 닉네임입니다.");
			return "user/register";
		} else if (!userForm.getPassword().equals(userForm.getConfirmPassword())) {
			// 비밀번호 확인
			model.addAttribute("passwordEqualError", "비밀번호가 일치하지 않습니다.");
			return "user/register";
		} else if (userForm.getPassword().length() < 8) {
			// 비밀번호 8자리 이하 오류
			model.addAttribute("passwordLengthError", "비밀번호는 8자리 이상이어야 합니다.");
			return "user/register";
		} else if (!userForm.getPassword().matches(passwordPattern)) {
			// 비밀번호 패턴 오류
			model.addAttribute("passwordPatternError", "비밀번호는 영어, 숫자, 특수문자가 포함된 8자 이상이어야 합니다.");
			return "user/register";
		} else if (userForm.getUserBirth().length() != 6) {
			model.addAttribute("birthLengthError", "생년월일 6자리를 입력해주세요.");
			return "user/register";
		}
//		 else if (!verificationCode.equals(String.valueOf(mailService.getVerificationNumber()))) {
//				// 이메일 인증코드 확인
//				model.addAttribute("emailVerificationError", "이메일 인증 후 회원가입 가능합니다.");
//				return "user/register";
//			}
		Boolean isEmailVerified = (Boolean) session.getAttribute("emailVerified");
		if (isEmailVerified == null || !isEmailVerified) {
			model.addAttribute("emailVerificationError", "이메일 인증 후 회원가입 가능합니다.");
			return "user/register";
		}

		userService.registerUser(userForm);
		session.removeAttribute("emailVerified");
		System.out.println("============회원가입 성공==============");
		System.out.println("이메일 인증 번호 : " + String.valueOf(mailService.getVerificationNumber()));
		return "redirect:/user/login";

	}

	@PostMapping("/main/register/verifySuccess")
	@ResponseBody
	public void verifySuccess(HttpSession session) {
		session.setAttribute("emailVerified", true);
	}

	// 회원탈퇴 확인 페이지
	@GetMapping("/main/profile/unregister")
	public String unregister(Model model) {
		// 탈퇴 확인 페이지에 전달할 빈 User 객체 추가
		model.addAttribute("userForm", new User());
		return "user/unregister";
	}

	// 회원탈퇴 처리
	@PostMapping("/main/profile/unregister")
	public String unregisterSubmit(@ModelAttribute User userForm
			, HttpSession session, Model model,Board board
			) {
		// userForm에서 전달받은 이메일을 이용하여 실제 User 객체 조회
		User user = userService.findUserByEmail(userForm.getEmail());

		if (user != null && encoderService.passwordEncoder().matches(userForm.getPassword(), user.getPassword())) {
			userService.deleteByBoardSeq(board.getSeq());
			userService.deleteUser(user);
			session.invalidate();
			System.out.println("회원 탈퇴 완료: " + user);
		} else {
			System.out.println("회원 탈퇴 실패 - 사용자 찾을 수 없음: " + userForm);
			model.addAttribute("passwordEqualError", "비밀번호가 일치하지 않습니다.");
			return "user/unregister";
		}
		return "redirect:/main";
	}

	// 로그아웃
	@GetMapping("/main/logout")
	public String logout(HttpServletRequest request) {
		request.getSession().invalidate();

		return "redirect:/main";
	}

	// 사용자 정보 페이지
	@GetMapping("/main/profile/user-info")
	public String info(HttpServletRequest request, @ModelAttribute User user,
			@AuthenticationPrincipal PrincipalDetails principalDetails, @ModelAttribute Board board, Model model) {
		System.out.println("============마이 페이지 이동==============");

		user = (User) request.getSession().getAttribute("user");
		boolean isLoggedIn = (principalDetails != null);

		if (isLoggedIn) {
			user = principalDetails.getUser();
			model.addAttribute("isLoggedIn", true);
			model.addAttribute("email", user.getEmail());
			model.addAttribute("nickname", user.getNickname());
			model.addAttribute("username", user.getUsername());
			model.addAttribute("provider", user.getProvider());
			System.out.println("로그인 계정 ::: " + user.getEmail());
			System.out.println("닉네임 ::: " + user.getNickname());
		} else if (user != null) {

			model.addAttribute("isLoggedIn", false);
			model.addAttribute("email", user.getEmail());
			model.addAttribute("nickname", user.getNickname());
			model.addAttribute("username", user.getUsername());
			model.addAttribute("userbirth", user.getUserBirth());
			System.out.println("로그인 계정 ::: " + user.getEmail());
			System.out.println("닉네임 ::: " + user.getNickname());

		} else {
			return "redirect:/main/login";
		}

		return "user/profile";
	}

	// 사용자 페이지 - 내 게시글
	@GetMapping("/main/profile/user-posts")
	public String userPosts(HttpServletRequest request, Model model,
			@AuthenticationPrincipal PrincipalDetails principalDetails,
			@RequestParam(defaultValue = "0", name = "curPage") int curPage,
			@RequestParam(defaultValue = "5", name = "rowSizePerPage") int rowSizePerPage,
			@RequestParam(defaultValue = "title", name = "searchType") String searchType) {
		User user = (User) request.getSession().getAttribute("user");
		boolean isLoggedIn = (principalDetails != null);

		if (curPage < 0) {
			curPage = 0; // Set to first page if negative
		}

		if (isLoggedIn) {
			user = principalDetails.getUser();
			model.addAttribute("isLoggedIn", true);
			model.addAttribute("email", user.getEmail());
			model.addAttribute("nickname", user.getNickname());
			model.addAttribute("username", user.getUsername());
			model.addAttribute("provider", user.getProvider());
			System.out.println("로그인 계정 ::: " + user.getEmail());
			System.out.println("닉네임 ::: " + user.getNickname());
		}

		if (user != null) {
			model.addAttribute("isLoggedIn", false);
			model.addAttribute("email", user.getEmail());
			model.addAttribute("nickname", user.getNickname());
			model.addAttribute("username", user.getUsername());
			model.addAttribute("userbirth", user.getUserBirth());
			System.out.println("로그인 계정 ::: " + user.getEmail());
			System.out.println("닉네임 ::: " + user.getNickname());
//	        List<Board> userPostPage = boardService.getUserPost(user.getEmail());

			Pageable pageable = PageRequest.of(curPage, rowSizePerPage, Sort.by("seq").descending());
			Page<Board> userPostPage = boardService.getUserPost(user.getEmail(), pageable);
			model.addAttribute("userPostPage", userPostPage);

			PagingInfo pagingInfo = new PagingInfo();
			int totalRowCount = userPostPage.getNumberOfElements();
			int totalPageCount = userPostPage.getTotalPages();
			int pageSize = pagingInfo.getPageSize();
			int startPage = curPage / pageSize * pageSize + 1;
			int endPage = startPage + pageSize - 1;
			endPage = endPage > totalPageCount ? (totalPageCount > 0 ? totalPageCount : 1) : endPage;

			pagingInfo.setCurPage(curPage);
			pagingInfo.setTotalRowCount(totalRowCount);
			pagingInfo.setTotalPageCount(totalPageCount);
			pagingInfo.setStartPage(startPage);
			pagingInfo.setEndPage(endPage);
			pagingInfo.setRowSizePerPage(rowSizePerPage);

			model.addAttribute("pagingInfo", pagingInfo);
			model.addAttribute("userPostPage", userPostPage);
			model.addAttribute("curPage", curPage);
			model.addAttribute("rowSizePerPage", rowSizePerPage);
			return "user/profile-userpost"; // 이 경로는 Thymeleaf 템플릿 이름과 일치해야 함
		} else {
			return "redirect:/main/login";
		}

	}

	// 사용자 페이지 - 찜 목록
	@GetMapping("/main/profile/user-wishlist")
	public String userWishList(HttpServletRequest request, @AuthenticationPrincipal PrincipalDetails principalDetails,
			Model model, @RequestParam(defaultValue = "0", name = "curPage") int curPage,
			@RequestParam(defaultValue = "5", name = "rowSizePerPage") int rowSizePerPage,
			@RequestParam(defaultValue = "title", name = "searchType") String searchType) {

		User user = (User) request.getSession().getAttribute("user");
		boolean isLoggedIn = (principalDetails != null);

		if (curPage < 0) {
			curPage = 0; // Set to first page if negative
		}

		if (isLoggedIn) {
			user = principalDetails.getUser();
		}

		if (user != null) {
			Pageable pageable = PageRequest.of(curPage, rowSizePerPage, Sort.by("seq").descending());
			Page<Board> userWishListPage = boardService.getUserWishlist(user, pageable);

			model.addAttribute("userWishListPage", userWishListPage);

			PagingInfo pagingInfo = new PagingInfo();
			int totalRowCount = userWishListPage.getNumberOfElements();
			int totalPageCount = userWishListPage.getTotalPages();
			int pageSize = pagingInfo.getPageSize();
			int startPage = curPage / pageSize * pageSize + 1;
			int endPage = startPage + pageSize - 1;
			endPage = endPage > totalPageCount ? (totalPageCount > 0 ? totalPageCount : 1) : endPage;

			pagingInfo.setCurPage(curPage);
			pagingInfo.setTotalRowCount(totalRowCount);
			pagingInfo.setTotalPageCount(totalPageCount);
			pagingInfo.setStartPage(startPage);
			pagingInfo.setEndPage(endPage);
			pagingInfo.setRowSizePerPage(rowSizePerPage);

			model.addAttribute("pagingInfo", pagingInfo);
			model.addAttribute("curPage", curPage);
			model.addAttribute("rowSizePerPage", rowSizePerPage);

			return "user/profile-userwishlist";
		} else {
			return "redirect:/main/login";
		}
	}

	@PostMapping("/main/profile/nickname")
	@ResponseBody
	public Map<String, String> setNickname(HttpServletRequest request,
			@AuthenticationPrincipal PrincipalDetails principalDetails, @RequestParam("nickname") String nickname) {
		Map<String, String> response = new HashMap<>();
		if (principalDetails != null) {
			User user = principalDetails.getUser();
			if (userService.existsByNickname(nickname)) {
				response.put("nicknameEqualError", "이미 사용중인 닉네임입니다.");
			} else {
				user.setNickname(nickname);
				userRepository.save(user);
				principalDetails.setNickname(nickname);
			}
		} else {
			User user = (User) request.getSession().getAttribute("user");
			if (userService.existsByNickname(nickname)) {
				response.put("nicknameEqualError", "이미 사용중인 닉네임입니다.");
			} else {
				user.setNickname(nickname);
				userRepository.save(user);
				request.getSession().setAttribute("user", user);
			}
		}
		return response;
	}

	// 회원정보 변경
	@PostMapping("/main/profile/user-info")
	public String changePassword(HttpServletRequest request, @ModelAttribute("user") User user, 
	                              @RequestParam("password") String password,
	                              @RequestParam("newPassword") String newPassword,
	                              @AuthenticationPrincipal PrincipalDetails principalDetails,
	                              Model model) {
	    // 로그인 상태 확인
		boolean isLoggedIn = (principalDetails != null);

        // 세션에서 User 객체 가져오기

        user = (User) request.getSession().getAttribute("user");


        if (isLoggedIn) {
            user = principalDetails.getUser();
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("email", user.getEmail());
            model.addAttribute("nickname", user.getNickname());
            model.addAttribute("username", user.getUsername());
            model.addAttribute("provider", user.getProvider());
            System.out.println("로그인 계정 ::: " + user.getEmail());
            System.out.println("닉네임 ::: " + user.getNickname());
        } else if (user != null) {
            model.addAttribute("isLoggedIn", false);
            model.addAttribute("email", user.getEmail());
            model.addAttribute("nickname", user.getNickname());
            model.addAttribute("username", user.getUsername());
            model.addAttribute("userbirth", user.getUserBirth());
            System.out.println("로그인 계정 ::: " + user.getEmail());
            System.out.println("닉네임 ::: " + user.getNickname());
        } else {
            return "redirect:/main/login";
        }
	    
	    // 비밀번호 검증
	    if (newPassword.length() < 8) {
	        model.addAttribute("passwordLengthError", "비밀번호는 8자리 이상이어야 합니다.");
	        return "user/profile";
	    }

	    String passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$";
	    if (!newPassword.matches(passwordPattern)) {
	        model.addAttribute("passwordPatternError", "비밀번호는 영어, 숫자, 특수문자가 포함된 8자 이상이어야 합니다.");
	        return "user/profile";
	    }

	    boolean isPasswordChanged = userService.changePassword(user.getEmail(), password, newPassword);
	    if (isPasswordChanged) {
	        System.out.println("============비밀번호 변경 성공==============");
	        model.addAttribute("successMessage", "비밀번호가 성공적으로 변경되었습니다.");
	        return "redirect:/main/profile/user-info"; // 비밀번호 변경 후 프로필 페이지로 리다이렉트
	    } else {
	        System.out.println("============비밀번호 변경 실패==============");
	        model.addAttribute("equalError", "현재 비밀번호가 일치하지 않습니다.");
	        return "user/profile"; // 변경 실패 시 프로필 페이지로 리다이렉트
	    }
	}
	@GetMapping({"/main/search"})
	  private String searchAll(Board board, Model model, HttpServletRequest request
			  , @ModelAttribute User user, @AuthenticationPrincipal PrincipalDetails principalDetails
			  , @RequestParam(defaultValue = "0", name = "curPage") int curPage, @RequestParam(defaultValue = "8", name = "rowSizePerPage") int rowSizePerPage
			  , @RequestParam(defaultValue = "title", name = "searchType") String searchType, @RequestParam(defaultValue = "", name = "searchWord") String searchWord
			  , @CookieValue(name = "nonMemberUUID", required = false) String uuid, HttpServletResponse response) {
	    System.out.println("========== 메인 검색 ==========");
	    user = (User)request.getSession().getAttribute("user");
	    boolean isLoggedIn = (principalDetails != null);
	    if (isLoggedIn) {
	      user = principalDetails.getUser();
	      model.addAttribute("isLoggedIn", Boolean.valueOf(true));
	      model.addAttribute("writer", user.getEmail());
	      model.addAttribute("nickname", user.getNickname());
	      System.out.println("로그인 계정 ::: " + user.getEmail());
	      System.out.println("닉네임 ::: " + user.getNickname());
	    } else if (user != null) {
	      model.addAttribute("isLoggedIn", Boolean.valueOf(true));
	      model.addAttribute("writer", user.getEmail());
	      model.addAttribute("nickname", user.getNickname());
	      System.out.println("로그인 계정 ::: " + user.getEmail());
	      System.out.println("닉네임 ::: " + user.getNickname());
	    } else {
	      model.addAttribute("isLoggedIn", Boolean.valueOf(false));
	      System.out.println("비로그인 조회");
	    } 
	    List<Board> recommendedBoards = this.boardService.getRecommendedBoards(user);
	    model.addAttribute("recommendedBoards", recommendedBoards);
	    System.out.println("===============================================================================");
	    System.out.println("추천 게시글:::" + recommendedBoards);
	    System.out.println("===============================================================================");
	    
	    if (curPage < 0)
	      curPage = 0; 
	    if (user == null || user.getEmail() == null)
	      if (uuid == null) {
	        uuid = UUID.randomUUID().toString();
	        Cookie uuidCookie = new Cookie("nonMemberUUID", uuid);
	        uuidCookie.setMaxAge(86400);
	        uuidCookie.setPath("/");
	        response.addCookie(uuidCookie);
	        System.out.println("uuid ::: " + uuidCookie);
	      }  
	    PagingInfo pagingInfo = new PagingInfo();
	    PageRequest pageRequest1 = PageRequest.of(curPage, rowSizePerPage, Sort.by(new String[] { "seq" }).descending());
	    Page<Board> pagedResult = this.boardService.getBoardListSearch((Pageable)pageRequest1, searchWord);
	    PageRequest pageRequest2 = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, new String[] { "cnt" }));
	    Page<Board> topResult = this.boardService.getBoardList((Pageable)pageRequest2, null, null);
	    PageRequest pageRequest3 = PageRequest.of(0, 5, Sort.by(new String[] { "likes" }).descending());
	    Page<Board> recResult = this.boardService.getBoardList((Pageable)pageRequest3, null, null);
	    
	    int totalRowCount = pagedResult.getNumberOfElements();
	    int totalPageCount = pagedResult.getTotalPages();
	    int pageSize = pagingInfo.getPageSize();
	    int startPage = curPage / pageSize * pageSize + 1;
	    int endPage = startPage + pageSize - 1;
	    endPage = (endPage > totalPageCount) ? ((totalPageCount > 0) ? totalPageCount : 1) : endPage;
	    pagingInfo.setCurPage(curPage);
	    pagingInfo.setTotalRowCount(totalRowCount);
	    pagingInfo.setTotalPageCount(totalPageCount);
	    pagingInfo.setStartPage(startPage);
	    pagingInfo.setEndPage(endPage);
	    pagingInfo.setSearchType(searchType);
	    pagingInfo.setSearchWord(searchWord);
	    pagingInfo.setRowSizePerPage(rowSizePerPage);
	    model.addAttribute("cp", Integer.valueOf(curPage));
	    model.addAttribute("sp", Integer.valueOf(startPage));
	    model.addAttribute("ep", Integer.valueOf(endPage));
	    model.addAttribute("ps", Integer.valueOf(pageSize));
	    model.addAttribute("rp", Integer.valueOf(rowSizePerPage));
	    model.addAttribute("tp", Integer.valueOf(totalPageCount));
	    model.addAttribute("st", searchType);
	    model.addAttribute("sw", searchWord);
	    model.addAttribute("pagingInfo", pagingInfo);
	    model.addAttribute("pagedResult", pagedResult);
	    model.addAttribute("pageable", pageRequest1);
	    model.addAttribute("topResult", topResult);
	    model.addAttribute("topPageable", pageRequest2);
	    model.addAttribute("recResult", recResult);
	    model.addAttribute("recPageable", pageRequest3);
	    model.addAttribute("uuid", uuid);
	    return "search/search";
	  }

}
