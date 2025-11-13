package com.example.quiz_1140818.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.quiz_1140818.constants.ResCodeMessage;
import com.example.quiz_1140818.dao.AccountDao;
import com.example.quiz_1140818.entity.Account;
import com.example.quiz_1140818.response.AdminRes;
import com.example.quiz_1140818.response.BasicRes;

@Service
public class AccountService {

	private BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();
	
	@Autowired
	private AccountDao accountDao;
	
	

	public BasicRes addinfo(String account, String password) {
		try {
			//若文件有要檢查帳號是否存在
			int count=accountDao.selectCountByAccount(account);
			//因為是透過PK去查詢是否有存在值 count只會是0或1
			if(count==1) {
				return new BasicRes(ResCodeMessage.ACCOUNT_EXIST.getCode()//
						, ResCodeMessage.ACCOUNT_EXIST.getMessage());
			}
			//存進去DB中的密碼要加密
			String encodePwd =encoder.encode(password);
			accountDao.addinfo(account, encodePwd);
			return new BasicRes(ResCodeMessage.SUCCESS.getCode()//
					, ResCodeMessage.SUCCESS.getMessage());
		} catch (Exception e) {
			//發生exception時，可以有下面兩個寫法
			//1.固定的回傳訊息，但無法詳細知道真正錯誤
//			return new BasicRes(ResCodeMessage.ADD_INFO_FALIED.getCode()//
//					, ResCodeMessage.ADD_INFO_FALIED.getMessage());
		
			//2.將catch 到的例外exception拋出(throw) 再由自訂義到的類別GlobalExceptionHandler
			//	寫入(回復)真正的錯誤訊息
			throw e;
		}
	}
	
	public AdminRes login(String account,String password) {
		//透過帳號取得資料
		Account data=accountDao.selectByAccount(account);
		if(data==null) {//data=null表示沒有資料
			return new AdminRes(ResCodeMessage.NOT_FOUND.getCode()//
					, ResCodeMessage.NOT_FOUND.getMessage(),false);
		}
		//比對密碼 使用排除法 這裡是密碼錯誤的意思
		if(!encoder.matches(password, data.getPassword())){
			return new AdminRes(ResCodeMessage.PASSWORD_MISMATCH.getCode()//
					, ResCodeMessage.PASSWORD_MISMATCH.getMessage(),false);
		}
		return new AdminRes(ResCodeMessage.SUCCESS.getCode()//
				, ResCodeMessage.SUCCESS.getMessage(), data.isAdmin());
	}
	
	
}
