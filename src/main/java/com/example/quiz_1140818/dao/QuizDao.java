package com.example.quiz_1140818.dao;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.quiz_1140818.entity.Quiz;

@Repository
public interface QuizDao extends JpaRepository<Quiz, Integer>{
	
	@Modifying
	@Transactional
	@Query(value="insert into quiz (title, description, start_date, end_date, is_publish"//
			+ " ) values(?1,?2,?3,?4,?5)",nativeQuery = true)
	public void  create(String title,String description ,LocalDate startDate,LocalDate endDate,
			boolean publish);

	//搜尋欄位id當前最大值，因為id是流水號，最大值也就表示最新新增的下一筆資料
	@Query(value="select max(id) from quiz",nativeQuery = true)
	public int selectMaxId();
	
	//搜尋欄位id出現次數 因為id是pk所以結果只會是0或1
	@Query(value="select count(id) from quiz where id=?1",nativeQuery = true)
	public int selectCountId(int id );
	
	
	@Modifying
	@Transactional
	@Query(value="update quiz set title=?2,description=?3,"
			+ "  start_date=?4,  end_date=?5,  is_publish=?6 where id=?1",nativeQuery = true)
	public int update(int id ,String title,String description ,LocalDate startDate,LocalDate endDate,
			boolean publish);
	
	@Query(value="select * from quiz",nativeQuery = true)
	public List<Quiz> getAll();
	
	@Query(value="select * from quiz where is_publish is true",nativeQuery = true)
	public List<Quiz> getPublishedAll();
	
	@Query(value="select * from quiz where title like %?1% and start_date >=?2 and "
			+ " end_date<=?3",nativeQuery = true)
	public List<Quiz> getSearch(String title,LocalDate startDate,LocalDate endDat);
	
	@Query(value="select * from quiz where title like %?1% and start_date >=?2 and "
			+ " end_date<=?3 and is_publish is true",nativeQuery = true)
	public List<Quiz> getPublishedSearch(String title,LocalDate startDate,LocalDate endDat);
	
	
	@Modifying
	@Transactional
	@Query(value="delete from quiz where id in (?1)",nativeQuery = true)
	public void deleteByIdIn(List<Integer> quizIdList);
	

	//可刪除1.未發布或 2.已發布且已結束
	@Modifying
	@Transactional
	@Query(value="delete from quiz where (is_publish is false or is_publish is true and ?2>end_date) "
			+ " and id in (?1)",nativeQuery = true)
	public void deleteByIdInWihCondition(List<Integer> quizIdList,LocalDate now);
	
	@Query(value="select * from quiz where id=?1",nativeQuery = true)
	public Quiz getById(int id);
	
	
	
}
