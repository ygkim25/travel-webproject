package com.web.controller;

import java.util.HashMap;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.web.service.MailService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MailController {
	
	private final MailService mailService;
    private int number;
    
	// 인증 이메일 전송
	@ResponseBody
	@PostMapping("/main/register/mailSend")
	public HashMap<String, Object> mailSend(@RequestParam("mail") String mail) {
		HashMap<String, Object> map = new HashMap<>();

		try {
			number = mailService.sendMail(mail);
			String num = String.valueOf(number);

			map.put("success", Boolean.TRUE);
			map.put("number", num);
		} catch (Exception e) {
			map.put("success", Boolean.FALSE);
			map.put("error", e.getMessage());
		}

		return map;
	}
	
	@ResponseBody
	@PostMapping("/main/find-password/mailSend")
	public HashMap<String, Object> passwordMailSend(@RequestParam("mail") String mail) {
		HashMap<String, Object> map = new HashMap<>();

		try {
			number = mailService.sendPasswordMail(mail);
			String num = String.valueOf(number);

			map.put("success", Boolean.TRUE);
			map.put("number", num);
			System.out.println(number);
		} catch (Exception e) {
			map.put("success", Boolean.FALSE);
			map.put("error", e.getMessage());
		}

		return map;
	}

	// 인증번호 일치여부 확인
	@ResponseBody
	@GetMapping("/main/register/mailCheck")
	public ResponseEntity<?> mailCheck(@RequestParam("userNumber") String userNumber) {

		boolean isMatch = userNumber.equals(String.valueOf(number));

		return ResponseEntity.ok(isMatch);
    }
}
