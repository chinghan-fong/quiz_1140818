package com.example.quiz_1140818.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.quiz_1140818.request.BasicReq;
import com.example.quiz_1140818.response.AdminRes;
import com.example.quiz_1140818.response.BasicRes;
import com.example.quiz_1140818.service.AccountService;

import jakarta.validation.Valid;
@CrossOrigin
@RestController
public class AccountController {

	@Autowired
	private AccountService accountService;
	
	@PostMapping("quiz/add_info")
	//有加Valid才會自動檢查參數 他如果有錯會拋出例外我們使用 GlobalExceptionHandler這個檔案萊處理變成我們想要看到的資訊
	public BasicRes addinfo(@Valid @RequestBody BasicReq req) {
		return accountService.addinfo(req.getAccount(), req.getPassword());
	}
	
	@PostMapping("quiz/login")
	public AdminRes login(@Valid @RequestBody BasicReq req) {
		return accountService.login(req.getAccount(), req.getPassword());
	}
	
	
}


