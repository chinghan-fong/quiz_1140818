package com.example.quiz_1140818.constants;

public enum ResCodeMessage {

	SUCCESS(200,"成功!!"),
	ADD_INFO_FALIED(400,"新增資訊失敗"),//
	NOT_FOUND(404,"找不到資料!!"),//
	PARAM_ACCOUNT_ERROR(400,"帳號參數錯誤!!"),//
	PARAM_PASSWORD_ERROR(400,"密碼參數錯誤!!"),//
	PASSWORD_MISMATCH(400,"密碼錯誤!!"),//
	ACCOUNT_EXIST(400,"帳戶已存在"),
	QUESTION_TYPE_ERROR(400,ConstantsMessage.QUESTION_TYPE_ERROR),//
	QUESTION_TYPE_OPTIONS_MISMATCH(400,"問題類型和選項不匹配"),//
	QUIZ_DATE_ERROR(400,"問卷日期錯誤!!"),//
	QUIZ_ID_ERROR(400,"問卷 ID 錯誤"),//
	RADIO_ANSWER_IS_REQUIRED(400,"必須選擇一個單選答案!!"),//
	TEXT_ANSWER_IS_REQUIRED(400,"文字答案為必填!!"),//
	CHECKBOX_ANSWER_IS_REQUIRED(400,"多選答案為必填!!"),//
	QUESTION_OPTION_MISMATCH(400,"題目選項不匹配!!");//

	
	
	
	
	private int code;

	private String message;

	private ResCodeMessage(int code, String message) {
		this.code = code;
		this.message = message;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
