package com.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

//첨부파일 업로드용 + 썸네일
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
//썸네일 방법 1
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;

import javax.imageio.ImageIO;
//썸네일 방법 2
//import net.coobird.thumbnailator.Thumbnails;
import javax.servlet.http.HttpSession;

//logging 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
//import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.auth.PrincipalDetails;
import com.web.dto.Board;
import com.web.dto.Comment;
//import com.web.dto.Comment;
//import com.web.dto.Member;
import com.web.dto.PagingInfo;
import com.web.model.LikeEntity;
import com.web.model.User;
import com.web.model.UserWishList;
import com.web.repository.BoardRepository;
import com.web.repository.CommentRepository;
import com.web.repository.LikeRepository;
import com.web.repository.UserRepository;
import com.web.repository.UserWishListRepository;
import com.web.service.BoardService;
import com.web.service.CommentService;
import com.web.service.FileService;
import com.web.service.JsonService;
import com.web.service.UserService;
import com.web.service.UserWishListService;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class BoardController {

	@Autowired
	private BoardService boardService;

	@Autowired
	private FileService fileService;
	@Autowired
	private BoardRepository boardRepository;
	@Autowired
	private JsonService jsonService;
	@Autowired
	private CommentService commentService;
	@Autowired
	private CommentRepository commentRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private LikeRepository likeRepository;
	@Autowired
	private UserWishListRepository userWishListRepository;
	@Autowired
	private UserWishListService userWishListService;

	public PagingInfo pagingInfo = new PagingInfo();

	@Value("${path.upload}")
	public String uploadFolder;

	// logging
	private final Logger log = LoggerFactory.getLogger(getClass());

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	private static final String UUID_COOKIE_NAME = "nonMemberUUID";
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	// 커뮤니티 이동
	@GetMapping("/main/community") // getBoardList
	public String getBoardList(Board board, Model model, HttpServletRequest request, @ModelAttribute User user,
			@AuthenticationPrincipal PrincipalDetails principalDetails,
			@RequestParam(defaultValue = "0", name = "curPage") int curPage,
			@RequestParam(defaultValue = "8", name = "rowSizePerPage") int rowSizePerPage,
			@RequestParam(defaultValue = "title", name = "searchType") String searchType,
			@RequestParam(defaultValue = "", name = "searchWord") String searchWord,
			@CookieValue(name = UUID_COOKIE_NAME, required = false) String uuid, HttpServletResponse response) {

		System.out.println("========== 커뮤니티 ==========");

		LikeEntity like = new LikeEntity();

		user = (User) request.getSession().getAttribute("user");
		boolean isLoggedIn = (principalDetails != null);

		if (isLoggedIn) {
			user = principalDetails.getUser();
			model.addAttribute("isLoggedIn", true);
			model.addAttribute("writer", user.getEmail());
			model.addAttribute("nickname", user.getNickname());
			System.out.println("로그인 계정 ::: " + user.getEmail());
			System.out.println("닉네임 ::: " + user.getNickname());

		} else {
			if (user != null) {
				model.addAttribute("isLoggedIn", true);
				model.addAttribute("writer", user.getEmail());
				model.addAttribute("nickname", user.getNickname());
				System.out.println("로그인 계정 ::: " + user.getEmail());
				System.out.println("닉네임 ::: " + user.getNickname());
			} else {
				model.addAttribute("isLoggedIn", false);
				System.out.println("비로그인 조회");
			}
		}

		// 추천 게시글 가져오기
		List<Board> recommendedBoards = boardService.getRecommendedBoards(user);
		model.addAttribute("recommendedBoards", recommendedBoards);

		System.out.println("===============================================================================");
		System.out.println("추천 게시글:::" + recommendedBoards);
		System.out.println("===============================================================================");

		if (curPage < 0) {
			curPage = 0; // Set to first page if negative
		}

		// 비회원만 uuid Cookie 생성
		if (user == null || user.getEmail() == null) {

			if (uuid == null) {
				uuid = UUID.randomUUID().toString();

				// uuid를 쿠키로 저장(optional)
				Cookie uuidCookie = new Cookie(UUID_COOKIE_NAME, uuid);
				uuidCookie.setMaxAge(24 * 60 * 60); // 24 hours
				uuidCookie.setPath("/");
				response.addCookie(uuidCookie);

				System.out.println("uuid ::: " + uuidCookie);
			}

		}

		PagingInfo pagingInfo = new PagingInfo();

		// 전체 게시판 목록
		Pageable pageable = PageRequest.of(curPage, rowSizePerPage, Sort.by("seq").descending());
		Page<Board> pagedResult = boardService.getBoardList(pageable, searchType, searchWord);

		// 조회수순 목록
		Pageable topPageable = PageRequest.of(0, 5, Sort.by(Direction.DESC, "cnt"));
		Page<Board> topResult = boardService.getBoardList(topPageable, null, null);

		// 추천순 목록
		Pageable recPageable = PageRequest.of(0, 5, Sort.by("likes").descending());
		Page<Board> recResult = boardService.getBoardList(recPageable, null, null);

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
		pagingInfo.setSearchType(searchType);
		pagingInfo.setSearchWord(searchWord);
		pagingInfo.setRowSizePerPage(rowSizePerPage);

		model.addAttribute("cp", curPage);
		model.addAttribute("sp", startPage);
		model.addAttribute("ep", endPage);
		model.addAttribute("ps", pageSize);
		model.addAttribute("rp", rowSizePerPage);
		model.addAttribute("tp", totalPageCount);
		model.addAttribute("st", searchType);
		model.addAttribute("sw", searchWord);

		model.addAttribute("pagingInfo", pagingInfo);
		model.addAttribute("pagedResult", pagedResult);
		model.addAttribute("pageable", pageable);
		model.addAttribute("topResult", topResult);
		model.addAttribute("topPageable", topPageable);
		model.addAttribute("recResult", recResult);
		model.addAttribute("recPageable", recPageable);

		model.addAttribute("uuid", uuid);

		return "community/community";

	}

	// 게시글 수정
	@GetMapping("/main/community/post-update")
	public String getBoard(HttpServletRequest request, @ModelAttribute User user, @RequestParam("seq") Long seq,
			@AuthenticationPrincipal PrincipalDetails principalDetails, Model model) {

		System.out.println("========== 게시글 수정 페이지 ==========");

		user = (User) request.getSession().getAttribute("user");
		boolean isLoggedIn = (principalDetails != null);

		if (principalDetails != null) {
			user = principalDetails.getUser();
			model.addAttribute("isLoggedIn", true);
			model.addAttribute("writer", user.getEmail());
			model.addAttribute("nickname", user.getNickname());
			System.out.println("로그인 계정 ::: " + user.getEmail());
			System.out.println("닉네임 ::: " + user.getNickname());
		} else if (user != null) {
			model.addAttribute("isLoggedIn", true);
			model.addAttribute("writer", user.getEmail());
			model.addAttribute("nickname", user.getNickname());
			System.out.println("로그인 계정 ::: " + user.getEmail());
			System.out.println("닉네임 ::: " + user.getNickname());
		} else {
			return "redirect:/main/login";
		}

		System.out.println("유저 정보 :::" + user);

		// boardRepository getBoard 대신(조회수 카운트 방지)
		Optional<Board> optionalBoard = boardRepository.findById(seq);

		Board board = optionalBoard.get();
		System.out.println("board : " + board);

		model.addAttribute("board", board);

		return "/community/community-post-update";
	}

	// 게시글 수정
	@PostMapping("/main/community/post-update")
	public String updateBoard(HttpServletRequest request, @ModelAttribute User user,
			@AuthenticationPrincipal PrincipalDetails principalDetails,
			@RequestParam("uploadFiles") List<MultipartFile> uploadFiles, Board board, Model model) {

		System.out.println("====== 게시글 수정 완료 ======");

		user = (User) request.getSession().getAttribute("user");
		boolean isLoggedIn = (principalDetails != null);

		if (principalDetails != null) {
			user = principalDetails.getUser();
			model.addAttribute("isLoggedIn", true);
			model.addAttribute("writer", user.getEmail());
			model.addAttribute("nickname", user.getNickname());
			System.out.println("로그인 계정 ::: " + user.getEmail());
			System.out.println("닉네임 ::: " + user.getNickname());
		} else if (user != null) {
			model.addAttribute("isLoggedIn", true);
			model.addAttribute("writer", user.getEmail());
			model.addAttribute("nickname", user.getNickname());
			System.out.println("로그인 계정 ::: " + user.getEmail());
			System.out.println("닉네임 ::: " + user.getNickname());
		} else {
			return "redirect:/main/login";
		}

		// 파일 새로 처리용
		String userID = user.getEmail();
		System.out.println("memberId for update : " + userID);
		if (!uploadFiles.isEmpty()) {
			try {
				fileService.processUploadFiles(uploadFiles, userID, uploadFolder, board, 80, 80);

			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			// 확인
			System.out.println("이미지는 업데이트하지 않습니다!");
		}

		// 모두 합쳐서 update
		boardService.updateBoard(board);
		System.out.println(" board to update : " + board);
		Long seq = board.getSeq();
		// model.addAttribute("seq", seq);
		model.addAttribute("board", board);
		model.addAttribute("user", user);

		System.out.println("update successful............................");

		return "redirect:/main/community/post-detail?seq=" + seq;

	}

	// 게시글 삭제
	@GetMapping("/deleteBoard")
	public String deleteBoard(@ModelAttribute User user, HttpServletRequest request,
			@AuthenticationPrincipal PrincipalDetails principalDetails, Board board, Model model,
			@RequestParam("seq") Long seq) {

		user = (User) request.getSession().getAttribute("user");

		if (principalDetails != null) {
			user = principalDetails.getUser();
			System.out.println("로그인 유저:::" + user);
		} else if (user == null) {
			System.out.println("로그인 유저:::" + user);
			return "redirect:/main/login";
		}

		if (board != null) {
			userService.deleteByBoardSeq(seq);
			boardService.deleteBoard(board);

		}
		System.out.println(user);

		return "redirect:/main/community";

	}

	// 게시글 쓰기
	@GetMapping("/main/community/write")
	public String insertBoardForm(HttpServletRequest request, @ModelAttribute User user,
			@AuthenticationPrincipal PrincipalDetails principalDetails, Model model) {

		System.out.println("============글 등록 페이지 이동==============");

		user = (User) request.getSession().getAttribute("user");

		if (principalDetails != null) {
			user = principalDetails.getUser();
			System.out.println("사용자 정보 ::: " + user);
			model.addAttribute("writer", principalDetails.getEmail());
			model.addAttribute("nickname", principalDetails.getNickname());
			System.out.println("로그인 된 유저 ::" + principalDetails.getEmail());
			System.out.println("유저 닉네임 ::" + principalDetails.getNickname());
		} else if (user != null) {
			System.out.println("사용자 정보 ::: " + user);
			model.addAttribute("writer", user.getEmail());
			model.addAttribute("nickname", user.getNickname());
			System.out.println("로그인 된 유저 ::" + user.getEmail());
			System.out.println("유저 닉네임 ::" + user.getNickname());
		} else {
			return "redirect:/main/login";
		}

		return "community/community-write";
	}

	// 게시글 쓰기
	@PostMapping("/main/community/write")
	public String insertBoard(HttpServletRequest request, @ModelAttribute User user,
			@AuthenticationPrincipal PrincipalDetails principalDetails, @ModelAttribute("board") Board board,
			@RequestParam("uploadFiles") List<MultipartFile> uploadFiles, @RequestParam(value = "action") String action,
			Model model) throws IOException {

		user = (User) request.getSession().getAttribute("user");

		String email = null;
		String nickname = null;

		if (principalDetails != null) {
			user = principalDetails.getUser();
			System.out.println("사용자 정보 ::: " + user);
			email = principalDetails.getEmail();
			nickname = principalDetails.getNickname();
			model.addAttribute("writer", principalDetails.getEmail());
			model.addAttribute("nickname", principalDetails.getNickname());
			System.out.println("로그인 된 유저 ::" + principalDetails.getEmail());
			System.out.println("유저 닉네임 ::" + principalDetails.getNickname());
		} else if (user != null) {
			email = user.getEmail();
			nickname = user.getNickname();
			System.out.println("사용자 정보 ::: " + user);
			model.addAttribute("writer", user.getEmail());
			model.addAttribute("nickname", user.getNickname());
			System.out.println("로그인 된 유저 ::" + user.getEmail());
			System.out.println("유저 닉네임 ::" + user.getNickname());
		} else {
			return "redirect:/main/login";
		}

		// 확인
		System.out.println("userID :: " + email); // ex) admin@gmail.com
		System.out.println("userNickname :: " + nickname);
		// 다수 파일 업로드
		if (!uploadFiles.isEmpty()) {
			try {
				fileService.processUploadFiles(uploadFiles, email, uploadFolder, board, 80, 80);

				// 확인
				// Retrieve the processed file names from the board object
				List<String> processedFileNames = board.getProcessedFileNames();
				System.out.println("processedFileNames : " + processedFileNames);
				System.out.println("successfully processed Files to board : " + board);

				// Model에 List 넣기(추가 작업이 있을 때 사용)
				model.addAttribute("processedFileNames", processedFileNames);
				System.out.println("model : " + model);
				log.info("model : " + model);

			} catch (IOException e) {

				e.printStackTrace();
			}
		}
		boardService.insertBoard(board);
		model.addAttribute("board", board);

		Long b_seq = board.getSeq();
		System.out.println("b_seq: " + b_seq); // Debug statement

		if ("intro".equals(action)) {
			// 일지 안쓰는 경우 바로 게시판 경로
			// return "redirect:/main/community/post-detail?seq=" + b_seq;
			model.addAttribute("msg", "글 작성이 완료되었습니다!");
			model.addAttribute("url", "/main/community");

			return "community/messageAlert";

		} else {
			// 일지 쓰러 가는 경로
			return "redirect:/main/community/writediary/" + b_seq;
		}

	}

	// 게시글 글쓰기 >> 여행일지 작성
	@GetMapping("/main/community/writediary/{seq}")
	public ModelAndView jsondata(@PathVariable("seq") Long seq,
			@AuthenticationPrincipal PrincipalDetails principalDetails, HttpServletRequest request,
			@ModelAttribute User user, Model model) {

		user = (User) request.getSession().getAttribute("user");
		boolean isLoggedIn = (principalDetails != null);

		if (isLoggedIn) {
			model.addAttribute("isLoggedIn", true);
			model.addAttribute("writer", principalDetails.getEmail());
			model.addAttribute("nickname", principalDetails.getNickname());
			System.out.println("로그인 계정 ::: " + principalDetails.getEmail());
			System.out.println("닉네임 ::: " + principalDetails.getNickname());
		} else {

			if (user != null) {
				model.addAttribute("isLoggedIn", true);
				model.addAttribute("writer", user.getEmail());
				model.addAttribute("nickname", user.getNickname());

				System.out.println("로그인 계정 ::: " + user.getEmail());
				System.out.println("닉네임 :::" + user.getNickname());
			} else {
				return new ModelAndView("redirect:/main/login");
			}
		}
		// check
		System.out.println("============일지쓰기 이동==============");

		Optional<Board> optionalBoard = boardRepository.findById(seq);
		System.out.println(" optionalBoard : " + optionalBoard);
		Board board = optionalBoard.get();

		if (board == null || !board.getSeq().equals(seq)) {
			return new ModelAndView("redirect:/main/community");
		}

		System.out.println("PathVariable : " + seq);

		ModelAndView mav = new ModelAndView("/community/community-write-diary");
		System.out.println("board to go into mav : " + board);
		mav.addObject("board", board);

		return mav;
	}

	// 게시글 글쓰기 >> 여행일지 작성
	@ResponseBody
	@PostMapping("/main/community/writediary")
	public ResponseEntity<String> saveJson(@RequestBody List<Map<String, Object>> textareaValues,
			@AuthenticationPrincipal PrincipalDetails principalDetails,
			@RequestHeader("prevBoardSeq") String prevBoardSeq, @ModelAttribute User user, HttpServletRequest request,
			@ModelAttribute("board") Board board, Model model) {

		user = (User) request.getSession().getAttribute("user");

		boolean isLoggedIn = (principalDetails != null);

		if (isLoggedIn) {
			model.addAttribute("isLoggedIn", true);
			model.addAttribute("writer", principalDetails.getEmail());
			model.addAttribute("nickname", principalDetails.getNickname());
			System.out.println("로그인 계정 ::: " + principalDetails.getEmail());
			System.out.println("닉네임 ::: " + principalDetails.getNickname());
		} else {

			if (user != null) {
				model.addAttribute("isLoggedIn", true);
				model.addAttribute("writer", user.getEmail());
				model.addAttribute("nickname", user.getNickname());

				System.out.println("로그인 계정 ::: " + user.getEmail());
				System.out.println("닉네임 :::" + user.getNickname());
			} else {
				model.addAttribute("isLoggedIn", false);
			}
		}


		// change String to int
		Long seq = Long.parseLong(prevBoardSeq);

		Optional<Board> optionalBoard = boardRepository.findById(seq);
		System.out.println(" optionalBoard : " + optionalBoard);
		board = optionalBoard.get();

		System.out.println("from jsondata board : " + board);

		if (board == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"message\": \"Board not found in session\"}");
		}

		try {

			ObjectMapper objectMapper = new ObjectMapper();
			String jsonString;

			jsonString = objectMapper.writeValueAsString(textareaValues);
			System.out.println("jsonString : " + jsonString);
			System.out.println("jsonString.getClass() : " + jsonString.getClass());

			board.setJsonContent(jsonString);
			boardService.insertBoard(board);

			model.addAttribute("board", board);
			System.out.println("model : " + model);
			System.out.println("board : " + board);

			String redirectUrl = "/main/community"; // Get the current URL
			String responseBody = "{\"message\": \"Data saved successfully\", \"boardSeq\": " + board.getSeq()
					+ ", \"redirectUrl\": \"" + redirectUrl + "\"}";

			System.out.println("insert success...............................................");
			System.out.println("redirectUrl........................" + redirectUrl + " 전달");
			return ResponseEntity.ok().body(responseBody);

		} catch (JsonProcessingException e) {

			e.printStackTrace();
			log.info("Exception occurred while processing json data: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("{\"message\": \"Error occurred while saving data\"}");
		}
	}

	/* 일지 등록하지 않는 경우 */
	@GetMapping("/exitwrite/{seq}")
	public String exitwrite(@PathVariable("seq") String stringSeq, Board board, Model model) {

		try {
			Long boardSeq = Long.parseLong(stringSeq);
			System.out.println("board seq to delete : " + boardSeq);

			Optional<Board> optionalBoard = boardRepository.findById(boardSeq);
			if (optionalBoard.isPresent()) {
				board = optionalBoard.get();
				// 작성중이던 게시글 삭제
				boardService.deleteBoard(board);
			}

			System.out.println("successfully deleted board and redirecting to boardlist..................");
			// return "redirect:getBoardList";
			// return "redirect:/getBoardList";

			model.addAttribute("msg", "글 작성 취소! 커뮤니티로 돌아갑니다.");
			model.addAttribute("url", "/main/community");

			return "community/messageAlert";

		} catch (NumberFormatException | NullPointerException ex) {
			System.out.println("Error processing exitjson : " + ex.getMessage());

			model.addAttribute("msg", ex.getMessage());
			model.addAttribute("url", "/main/community");

			return "community/messageAlert";
		}
	}

	/* 게시글 상세창 */
	@GetMapping("/main/community/post-detail")
	public String parsejson(@RequestParam(value = "seq") Long seq, @ModelAttribute User user,
			@AuthenticationPrincipal PrincipalDetails principalDetails, HttpServletRequest request,
			@CookieValue(name = UUID_COOKIE_NAME, required = false) String uuid, HttpServletResponse response,
			Board board, Model model) {

		System.out.println("============게시글 상세 보기==============");

		user = (User) request.getSession().getAttribute("user");
		boolean isLoggedIn = (principalDetails != null);

		if (principalDetails != null) {
			user = principalDetails.getUser();
			model.addAttribute("isLoggedIn", true);
			model.addAttribute("writer", user.getEmail());
			model.addAttribute("nickname", user.getNickname());
			System.out.println("로그인 계정 ::: " + user.getEmail());
			System.out.println("닉네임 ::: " + user.getNickname());
		} else {
			if (user != null) {
				model.addAttribute("isLoggedIn", true);
				model.addAttribute("writer", user.getEmail());
				model.addAttribute("nickname", user.getNickname());
				System.out.println("로그인 계정 ::: " + user.getEmail());
				System.out.println("닉네임 ::: " + user.getNickname());
			} else {
				model.addAttribute("isLoggedIn", false);
				System.out.println("비로그인 조회");
			}
		}

		System.out.println("게시글 번호 : " + seq);
		// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		// 비회원만 uuid Cookie 체크 및 만료시 생성
		if (user == null) {

			System.out.println("====== 비로그인 회원 게시글 조회 ======");
			if (uuid == null) {
				uuid = UUID.randomUUID().toString();

				// uuid를 쿠키로 저장(optional)
				Cookie uuidCookie = new Cookie(UUID_COOKIE_NAME, uuid);
				uuidCookie.setMaxAge(24 * 60 * 60);
				uuidCookie.setPath("/");
				response.addCookie(uuidCookie);
				// 확인
				System.out.println("your uuid cookie : " + uuid);
			}
		} else {
			System.out.println("====== 회원 게시글 조회 ======");
			System.out.println("로그인 상태 : " + user);
		}
		// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

		// 조회수 카운트
		board = boardService.getBoard(board);
		System.out.println("게시글 작성 정보 : " + board);

		// **************************************************************************
		if (board == null) {
			model.addAttribute("msg", "페이지 없음! 게시판으로 돌아갑니다.!");
			model.addAttribute("url", "/main/community");

			return "commumity/messageAlert";
		}
		// **************************************************************************
		// **************************************************************************
		// 댓글목록 불러오기:
		List<Comment> commentList = commentService.getComments(seq);		

		String JsonContent = board.getJsonContent();
		if (JsonContent != null) {
			// Board board = optionalBoard.get();
			System.out.println(" JsonContent : " + JsonContent);
			System.out.println(" JsonContent.getClass() : " + JsonContent.getClass());

			List<String> parsedStrings = jsonService.parseJson(JsonContent);
			// Add the parsed JSON to the model
			model.addAttribute("jsonContentList", parsedStrings);
		}

		model.addAttribute("board", board);
		model.addAttribute("member", user);
		model.addAttribute("commentList", commentList);
		// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		model.addAttribute("uuid", uuid);
		// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

		return "community/community-post";
	}

	/* 게시글 등록 할 때 여행일지 수정 */
	@GetMapping("/main/community/traveldiary")
	public String getJsonData(HttpServletRequest request, @ModelAttribute User user, @RequestParam("seq") Long seq,
			@AuthenticationPrincipal PrincipalDetails principalDetails, Model model) {

		user = (User) request.getSession().getAttribute("user");
		boolean isLoggedIn = (principalDetails != null);

		if (principalDetails != null) {
			user = principalDetails.getUser();
			model.addAttribute("isLoggedIn", true);
			model.addAttribute("writer", user.getEmail());
			model.addAttribute("nickname", user.getNickname());
			System.out.println("로그인 계정 ::: " + user.getEmail());
			System.out.println("닉네임 ::: " + user.getNickname());
		} else {
			if (user != null) {
				model.addAttribute("isLoggedIn", true);
				model.addAttribute("writer", user.getEmail());
				model.addAttribute("nickname", user.getNickname());
				System.out.println("로그인 계정 ::: " + user.getEmail());
				System.out.println("닉네임 ::: " + user.getNickname());
			} else {
				model.addAttribute("isLoggedIn", false);
				System.out.println("비로그인 조회");
			}
		}

		System.out.println("============여행일지 수정 이동==============");

		if (user == null || user.getEmail() == null) {
			return "redirect:/main/login";
		}

		Optional<Board> optionalBoard = boardRepository.findById(seq);
		Board board = optionalBoard.get();
		System.out.println("board : " + board);

		if (board == null) {
			// Handle case where board is not found
			model.addAttribute("msg", "페이지 없음! 게시판으로 돌아갑니다.!");
			model.addAttribute("url", "/main/community");

			return "community/messageAlert";
		}
		String JsonContent = board.getJsonContent();
		if (JsonContent != null) {
			// Board board = optionalBoard.get();
			System.out.println(" JsonContent : " + JsonContent);
			System.out.println(" JsonContent.getClass() : " + JsonContent.getClass());

			List<String> parsedStrings = jsonService.parseJson(JsonContent);
			// Add the parsed JSON to the model
			model.addAttribute("jsonContentList", parsedStrings);
		}

		model.addAttribute("board", board);
		model.addAttribute("member", user);
		System.out.println("model : " + model);

		return "community/diary-update";
	}

//    
//    
//     /*댓글 쓰기*/ 
	@PostMapping("addboardComment/{boardSeq}")
	public String addboardComment(@PathVariable("boardSeq") Long seq, @RequestParam("comWriter") String comWriter,
			@RequestParam("comment") String commentContent
			, @RequestParam("comWriterNick") String comWriterNick
			,@ModelAttribute User user, HttpServletRequest request, Board board, Model model) {

		System.out.println("============댓글 쓰기==============");
		System.out.println("seq : " + seq);
		System.out.println("board : " + board); // seq 이외에 빈 객체
		System.out.println("comWriter : " + comWriter);
		System.out.println("comment :" + commentContent);
		System.out.println("============댓글 쓰기==============");
		
		// 댓글 정보 가져오기
		Comment newComment = new Comment();
		newComment.setContent(commentContent);
		newComment.setWriter(comWriter);
		newComment.setNickname(comWriterNick);

		// 댓글 쓰기
		Comment comment = commentService.writeComment(seq, newComment);

		Optional<Board> optionalBoard = boardRepository.findById(seq);
		board = optionalBoard.get();
		System.out.println("board : " + board);

		if (board == null) {
			// Handle case where board is not found
			model.addAttribute("msg", "페이지 없음! 게시판으로 돌아갑니다.!");
			model.addAttribute("url", "/main/community");

			return "community/messageAlert";
		}

		// 댓글 수 업데이트
		board.setCommentCount(board.getCommentCount() + 1);
		boardService.insertBoard(board);

		// 찍어보기
		model.addAttribute("commentContent", commentContent);
		model.addAttribute("comWriter", comWriter);
		model.addAttribute("board", board);
		model.addAttribute("member", user);
		model.addAttribute("comment", comment);
		model.addAttribute("seq", seq);
		System.out.println("============쓴 댓글 : " + comment);

		return "redirect:/main/community/post-detail?seq=" + seq;

	}

//    
	/* 댓글 삭제 */
	@GetMapping("deleteComment/{boardSeq}/{commentId}")
	public String deleteComment(@ModelAttribute User user, HttpServletRequest request,
			@AuthenticationPrincipal PrincipalDetails principalDetails, @PathVariable("boardSeq") Long seq,
			@PathVariable("commentId") Long id, Board board, Model model) {

		user = (User) request.getSession().getAttribute("user");
		boolean isLoggedIn = (principalDetails != null);

		if (principalDetails != null) {
			user = principalDetails.getUser();
			model.addAttribute("isLoggedIn", true);
			model.addAttribute("writer", user.getEmail());
			model.addAttribute("nickname", user.getNickname());
			System.out.println("로그인 계정 ::: " + user.getEmail());
			System.out.println("닉네임 ::: " + user.getNickname());
		} else {
			if (user != null) {
				model.addAttribute("isLoggedIn", true);
				model.addAttribute("writer", user.getEmail());
				model.addAttribute("nickname", user.getNickname());
				System.out.println("로그인 계정 ::: " + user.getEmail());
				System.out.println("닉네임 ::: " + user.getNickname());
			} else {
				model.addAttribute("isLoggedIn", false);
				System.out.println("비로그인 조회");
			}
		}

		System.out.println("seq : " + seq);
		System.out.println("id : " + id);
		System.out.println("============댓글 삭제==============");

		// 삭제
		commentService.deleteComment(id);

		// 댓글수 업데이트할 board 구하기
		Optional<Board> optionalBoard = boardRepository.findById(seq);

		if (optionalBoard.isPresent()) {
			board = optionalBoard.get();

			// 수정된 댓글수 구하기
			// 1. 부모댓글

			List<Comment> commentList = commentService.getComments(seq);
			if (commentList.size() > 0) {
				int totalChildCount = 0;
				int commentCount = commentList.size(); // size() = int
				// 2. 자식댓글
				// Iterate through each parent comment to count their child comments
				for (Comment parentComment : commentList) {
					List<Comment> childList = commentService.getChildComments(parentComment.getId());

					if (childList != null) {
						int childCount = childList.size();
						totalChildCount += childCount;
					}
				}

				int totalCount = commentCount + totalChildCount;

				// 댓글 수 업데이트
				board.setCommentCount(Long.valueOf(totalCount)); // int => Long
			} else {
				board.setCommentCount(0L);
			}

			boardService.insertBoard(board);
		}

		model.addAttribute("board", board);
		model.addAttribute("user", user);

		return "redirect:/main/community/post-detail?seq=" + seq;
	}

// 
//    
////+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++   	
//    							//board id = seq
////+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//    
	/* 대댓글 */
	@ResponseBody
	@PostMapping("/main/community/post-detail/reply")
	public ResponseEntity<String> replyToComment(@RequestBody Map<String, Object> requestBody,
			@AuthenticationPrincipal PrincipalDetails principalDetails, @ModelAttribute("board") Board board,
			@ModelAttribute User user, HttpServletRequest request, Model model) {

		user = (User) request.getSession().getAttribute("user");
		boolean isLoggedIn = (principalDetails != null);

		if (principalDetails != null) {
			user = principalDetails.getUser();
			model.addAttribute("isLoggedIn", true);
			model.addAttribute("writer", user.getEmail());
			model.addAttribute("nickname", user.getNickname());
			System.out.println("로그인 계정 ::: " + user.getEmail());
			System.out.println("닉네임 ::: " + user.getNickname());
		} else {
			if (user != null) {
				model.addAttribute("isLoggedIn", true);
				model.addAttribute("writer", user.getEmail());
				model.addAttribute("nickname", user.getNickname());
				System.out.println("로그인 계정 ::: " + user.getEmail());
				System.out.println("닉네임 ::: " + user.getNickname());
			} else {
				model.addAttribute("isLoggedIn", false);
				System.out.println("비로그인 조회");
			}
		}

		System.out.println("============대댓글 쓰기==============");

		Long boardSeq = Long.valueOf(requestBody.get("boardSeq").toString());
		Long parentId = Long.valueOf(requestBody.get("parentId").toString());
		String replyContent = requestBody.get("content").toString();
		String repWriter = requestBody.get("repWriter").toString();
		String repNick = requestBody.get("repNick").toString();

		try {

			// board 글 찾기
			Optional<Board> optionalBoard = boardRepository.findById(boardSeq);
			board = optionalBoard.get();
			System.out.println("board : " + board);

			// 부모 댓글 찾기
			Comment parentComment = commentService.findComment(parentId); // Fetch parent comment
			Comment newReply = new Comment();

			newReply.setWriter(repWriter);
			newReply.setContent(replyContent);
			newReply.setParent(parentComment);
			newReply.setNickname(repNick);

			// 댓글 쓰기
			commentService.writeComment(boardSeq, newReply);

			// 댓글 수 업데이트
			board.setCommentCount(board.getCommentCount() + 1);
			boardService.insertBoard(board);

			String redirectUrl = "/main/community/post-detail"; // Get the current URL
			String responseBody = "{ \"message\": \"Reply posted Successfully!!\"," + "\"replyWriter\": \"" + repWriter
					+ "\"," + "\"replyComment\": \"" + replyContent + "\"," + "\"parentComment\": \"" + parentComment
					+ "\"," + "\"boardSeq\": \"" + boardSeq + "\"," + " \"redirectUrl\": \"" + redirectUrl + "\""
					+ "\"," + "\"replyNickname\": \"" + repNick + "\" }";

			return ResponseEntity.ok(responseBody);

		} catch (Exception e) {
			// Log the exception or perform any other error handling actions here
			e.printStackTrace();
			log.info("Exception occurred while processing json data: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("{\"message\": \"Error occurred while saving data\"}");
		}
	};

	/* 좋아요 기능 */

	// 좋아요 버튼
	@PostMapping("/main/community/post-detail/{boardSeqString}/like")
	public ResponseEntity<Long> addLikeCount(@PathVariable("boardSeqString") String boardSeqString,
			@AuthenticationPrincipal PrincipalDetails principalDetails, HttpServletRequest request, Model model) {

		Long boardSeq;
		try {
			boardSeq = Long.parseLong(boardSeqString);
		} catch (NumberFormatException e) {
			return ResponseEntity.badRequest().build();
		}

		User user = null;
		if (principalDetails != null) {
			user = principalDetails.getUser();
		} else {
			user = (User) request.getSession().getAttribute("user");
		}

		if (user != null) {
			model.addAttribute("isLoggedIn", true);
			model.addAttribute("writer", user.getEmail());
			model.addAttribute("nickname", user.getNickname());
		} else {
			model.addAttribute("isLoggedIn", false);
		}

		try {
			Optional<Board> optionalBoard = boardRepository.findById(boardSeq);
			if (optionalBoard.isPresent()) {

				Board board = optionalBoard.get();
				if (likeRepository.existsByUserAndBoard(user, board)) {
					return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 이미 좋아요를 누른 경우
				}

				LikeEntity like = new LikeEntity();
				like.setUser(user);
				like.setBoard(board);
				like.setEmail(user.getEmail());
				likeRepository.save(like);

				System.out.println("좋아요 누른 유저 : " + like.getUser());
				System.out.println("좋아요 누른 게시글 : " + like.getBoard());

				board.setLikes(board.getLikes() + 1);
				boardService.insertBoard(board);
				return ResponseEntity.ok(board.getLikes());
			} else {
				return ResponseEntity.notFound().build();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/main/community/post-detail/{boardSeqString}/likeCount")
	public ResponseEntity<Long> getLikeCount(@PathVariable("boardSeqString") String boardSeqString,
			@AuthenticationPrincipal PrincipalDetails principalDetails, HttpServletRequest request, Model model) {

		System.out.println("entering likeCount.......................................");
		Long boardSeq;
		try {
			boardSeq = Long.parseLong(boardSeqString);
		} catch (NumberFormatException e) {
			return ResponseEntity.badRequest().build();
		}

		User user = null;
		if (principalDetails != null) {
			user = principalDetails.getUser();
		} else {
			user = (User) request.getSession().getAttribute("user");
		}

		if (user != null) {
			model.addAttribute("isLoggedIn", true);
			model.addAttribute("writer", user.getEmail());
			model.addAttribute("nickname", user.getNickname());
		} else {
			model.addAttribute("isLoggedIn", false);
		}

		Optional<Board> optionalBoard = boardRepository.findById(boardSeq);
		if (optionalBoard.isPresent()) {
			return ResponseEntity.ok(optionalBoard.get().getLikes());
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@PostMapping("/main/community/post-detail/{boardSeqString}/wishlist")
	public ResponseEntity<Long> getWishList(@PathVariable("boardSeqString") String boardSeqString,
			@AuthenticationPrincipal PrincipalDetails principalDetails, HttpServletRequest request, Model model) {

		System.out.println("=======================찜 하기 ==============================");

		Long boardSeq;
		try {
			boardSeq = Long.parseLong(boardSeqString);
		} catch (NumberFormatException e) {
			return ResponseEntity.badRequest().build();
		}

		User user = null;
		if (principalDetails != null) {
			user = principalDetails.getUser();
		} else {
			user = (User) request.getSession().getAttribute("user");
		}

		if (user != null) {
			model.addAttribute("isLoggedIn", true);
			model.addAttribute("writer", user.getEmail());
			model.addAttribute("nickname", user.getNickname());
		} else {
			model.addAttribute("isLoggedIn", false);
		}

		System.out.println("=======================여기까지 됨 =============================");
		System.out.println("로그인 유저 :::" + user.getEmail());
		try {
			Optional<Board> optionalBoard = boardRepository.findById(boardSeq);
			if (optionalBoard.isPresent()) {

				System.out.println("=======================여기까지 됨2 =============================");

				Board board = optionalBoard.get();

				if (userWishListRepository.existsByUserAndBoard(user, board)) {
					return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 이미 찜한 경우
				}

				UserWishList wishlist = new UserWishList();
				wishlist.setUser(user);
				wishlist.setBoard(board);
				wishlist.setEmail(user.getEmail());
				userWishListRepository.save(wishlist);

				System.out.println("찜한 유저 : " + wishlist.getUser());
				System.out.println("찜한 게시글 : " + wishlist.getBoard());

				return ResponseEntity.ok(wishlist.getId());
			} else {
				return ResponseEntity.notFound().build();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	// 파일 다운로드
	@GetMapping("/download")
	public ResponseEntity<Resource> handleFileDownload(HttpServletRequest req, @RequestParam int seq,
			@RequestParam String fn) throws Exception {
		req.setCharacterEncoding("utf-8");
		String fileName = req.getParameter("fn");

		System.out.println("Entering file download........................................................");
		// 파일 경로 구하기
		String seqParam = req.getParameter("seq");
		Long boardSeq = Long.parseLong(seqParam);
		Optional<Board> optionalBoard = boardRepository.findById(boardSeq);
		Board board = optionalBoard.get();
		String boardWriter = board.getWriter();
		System.out.println("boardWriter : " + boardWriter);

		Path filePath = Paths.get(uploadFolder + '/' + boardWriter + '/' + fileName).toAbsolutePath();
		System.out.println("filePath : " + filePath);

		Resource resource = null;
		try {
			resource = new UrlResource(filePath.toUri());
			if (resource.exists()) {
				return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
						.header(HttpHeaders.CONTENT_DISPOSITION,
								"attachment; filename=\"" + URLEncoder.encode(resource.getFilename(), "utf-8") + "\"")
						.body(resource);
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			}
		} catch (MalformedURLException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

//    
//
//사진 갤러리
	@GetMapping("/main/community/gallery")
	public String gallery(Model model, @ModelAttribute("user") User user, HttpServletRequest request,
			@AuthenticationPrincipal PrincipalDetails principalDetails) throws IOException {

		user = (User) request.getSession().getAttribute("user");
		boolean isLoggedIn = (principalDetails != null);

		if (isLoggedIn) {
			user = principalDetails.getUser();
			model.addAttribute("isLoggedIn", true);
			model.addAttribute("writer", user.getEmail());
			model.addAttribute("nickname", user.getNickname());
			System.out.println("로그인 계정 ::: " + user.getEmail());
			System.out.println("닉네임 ::: " + user.getNickname());

		} else {
			if (user != null) {
				model.addAttribute("isLoggedIn", true);
				model.addAttribute("writer", user.getEmail());
				model.addAttribute("nickname", user.getNickname());
				System.out.println("로그인 계정 ::: " + user.getEmail());
				System.out.println("닉네임 ::: " + user.getNickname());
			} else {
				// model.addAttribute("isLoggedIn", false);
				System.out.println("비로그인 조회");
				model.addAttribute("msg", "로그인 후 이용 가능합니다.");
				model.addAttribute("url", "/main/login");

				return "community/messageAlert";
			}
		}

		String writer = user.getEmail();
		System.out.println("writer : " + writer);

		// get files from uploadFolder
		String directoryPath = uploadFolder + '/' + writer + '/';
		List<String> fileNames = fileService.listFiles(directoryPath);
		model.addAttribute("fileNames", fileNames);
		model.addAttribute("user", user);

		System.out.println("entering gallery.............................................");

		return "community/gallery";
	}

}
