package com.example.quiz_1140818.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.quiz_1140818.entity.Fillin;
import com.example.quiz_1140818.entity.FillinId;

@Repository
public interface FillinDao extends JpaRepository<Fillin, FillinId>{
	
	@Modifying
	@Transactional
	@Query(value="insert into fillin (quiz_id,question_id,email,name,phone,age,"//
			+ " gender, answer_str, fillin_date) values(?1,?2,?3,?4,?5,?6,?7,?8,?9)",nativeQuery = true)
	public void fillin(int quizId,int questionId,String email,String name,String phone,//
			int age,String gender,String answerStr,LocalDateTime fillinDate);
	
	@Query(value="select *from fillin where quiz_id=?1",nativeQuery = true)
	public List<Fillin> getByQuizId(int quizId);
//													//nativeQuery=true是執行資料庫 false執行物件
//	@Query(value="select questionId ,answerStr from fillin where quiz_id=?1",nativeQuery = false)
//	public List<Fillin> getByQuizId1(int quizId);
}
