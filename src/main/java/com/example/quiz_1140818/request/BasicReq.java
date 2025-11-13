package com.example.quiz_1140818.request;

import com.example.quiz_1140818.constants.ConstantsMessage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class BasicReq {
	/**
	 * 1.@NotBlank(message="Account format error!!"<br>
	 * 2.@NotBlank:表示限制字串 account不能是null 空字串和全空白字串<br>
	 * 3.@Pattern 就是可以使用正規表達式<br>
	 * 4.message 後面的字串表示當違反@NotBlank的時候會回傳的訊息<br>
	 * 4.1message 後面的字串值只能寫死就是固定不變，若要參數化擇要在參數前面加上final<br>
	 * 4.2其餘可以看ConstantsMessage的註解
	 */
	@NotBlank(message=ConstantsMessage.PARAM_ACCOUNT_ERROR)
	@Pattern(regexp="\\w{3,8}",message=ConstantsMessage.PARAM_ACCOUNT_ERROR)
	private String account;
	
	@NotBlank(message=ConstantsMessage.PARAM_PASSWORD_ERROR)
	@Pattern(regexp="\\w{3,16}",message=ConstantsMessage.PARAM_PASSWORD_ERROR)
	private String password;

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
