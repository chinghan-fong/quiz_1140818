package com.example.quiz_1140818.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.quiz_1140818.entity.Account;

@Repository
public interface AccountDao extends JpaRepository<Account, String>{
	
	

	@Modifying
	@Transactional
	@Query(value = "INSERT INTO account (account,password) VALUES(?1,?2)", nativeQuery = true)
	public void addinfo(String account,String password) ;

	//檢查帳號是否存在
	@Query(value = "select count(account) from account where account=?1; ", nativeQuery = true)
	public int selectCountByAccount(String account) ;
	
	
	@Query(value = "select* from account where account=?1; ", nativeQuery = true)
	public Account selectByAccount(String account) ;
		
	
	
}
