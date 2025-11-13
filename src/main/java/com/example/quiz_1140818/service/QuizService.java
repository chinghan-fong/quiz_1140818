package com.example.quiz_1140818.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.quiz_1140818.constants.QuestionType;
import com.example.quiz_1140818.constants.ResCodeMessage;
import com.example.quiz_1140818.dao.QuestionDao;
import com.example.quiz_1140818.dao.QuizDao;
import com.example.quiz_1140818.entity.Question;
import com.example.quiz_1140818.entity.Quiz;
import com.example.quiz_1140818.response.BasicRes;
import com.example.quiz_1140818.response.QuestionListRes;
import com.example.quiz_1140818.response.QuizListRes;
import com.example.quiz_1140818.vo.Options;
import com.example.quiz_1140818.vo.QuestionVo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

@Service
public class QuizService {

	// 提供json跟java物件(object)之間的互轉
	// mapper的方法都要放在try catch裡面不然會有錯
	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private QuizDao quizDao;

	@Autowired
	private QuestionDao questionDao;

	/**
	 * * @Transactional 1.使用於修改資料時(insert/update/delete)且有以下兩種狀況<br>
	 * 1.1 一個方法裡面有用到多個Dao的時候，例如下面的方法create中有同時使用quizDao 跟 questionDao<br>
	 * 1.2 同一個Dao中有修改多筆資料，例如下面的方法create中有使用questionDao去新增多筆問題<br>
	 * rollbackOn = Exception.class<br>
	 * 2.其預設的有效作用是當成是發生RunTimeException(以及其子類別)時才會讓資料回朔所以為了
	 * 再發生其他Exception時也可以讓資料回朔，就要把作用範圍提升到所有例外的父類別Exception
	 * 3.要讓@Transactional有效的另一個條件必須要把發生的Exception throw出去
	 */
	@Transactional(rollbackOn = Exception.class)
	// throw e; 要有這個 方法要加上throws Exception
	public BasicRes create(Quiz quiz, List<QuestionVo> questionVoList) throws Exception {
		try {
			// 檢查question
			BasicRes checkRes = checkQuestion(questionVoList);
			if (checkRes != null) {// checkRes不等於NULL代表他檢查有錯要傳回去
				return checkRes;
			}
			// 檢查問卷時間:開始時間不能筆結束時間晚
			checkRes = checkDate(quiz.getStartDate(), quiz.getEndDate());
			if (checkRes != null) {// checkRes不等於NULL代表他檢查有錯要傳回去
				return checkRes;
			}
			// 新增問卷quiz
			quizDao.create(quiz.getTitle(), quiz.getDescription(), quiz.getStartDate(), //
					quiz.getEndDate(), quiz.isPublish());
			// 取得quiz_id 因為quiz_id是流水號要等資料新增進去DB才會自動產生
			int quizId = quizDao.selectMaxId();

			// vo轉成Question entity後再將資料寫進去DB
			for (QuestionVo vo : questionVoList) {
				List<Options> optionsList = vo.getOptionsList();
				if (optionsList == null) {
					optionsList = new ArrayList<>();
				}
				// 將List<Options> 中的optionsList 轉為字串
				String optionStr = mapper.writeValueAsString(vo.getOptionsList());
				// 新增題目question
				questionDao.create(quizId, vo.getQuestionId(), vo.getName(), optionStr, vo.getType(), vo.isRequired());
			}

		} catch (Exception e) {
			throw e;
		}

		return new BasicRes(ResCodeMessage.SUCCESS.getCode()//
				, ResCodeMessage.SUCCESS.getMessage());
	}

	// 檢查question 的type和選項 下面這個只會回null跟非null 是使用排除法 所以null是我檢查正確的
	private BasicRes checkQuestion(List<QuestionVo> questionVoList) {
		for (QuestionVo item : questionVoList) {
			if (!QuestionType.checkAllType(item.getType())) {
				// 檢查的結果如果是FALSE表示QUESTION 中的TYPE不是設定的3種類型
				return new BasicRes(ResCodeMessage.QUESTION_TYPE_ERROR.getCode()//
						, ResCodeMessage.QUESTION_TYPE_ERROR.getMessage());
			}
			// 檢查選項:
			// 1.type是簡答題時不會有選項
			// 2.選擇題的時候有選項
			// 以下是排除當type是簡答題的時候卻有選項
			if (!QuestionType.checkChoiceType(item.getType())) {
				// 簡答題時選項List卻不是空的
				if (!item.getOptionsList().isEmpty()) {
					return new BasicRes(ResCodeMessage.QUESTION_TYPE_OPTIONS_MISMATCH.getCode()//
							, ResCodeMessage.QUESTION_TYPE_OPTIONS_MISMATCH.getMessage());
				}
			}
			// 以下是排除當type是單或多選題的時候卻沒有選項
			if (QuestionType.checkChoiceType(item.getType())) {
				if (item.getOptionsList().isEmpty()) {
					return new BasicRes(ResCodeMessage.QUESTION_TYPE_OPTIONS_MISMATCH.getCode()//
							, ResCodeMessage.QUESTION_TYPE_OPTIONS_MISMATCH.getMessage());
				}
			}
		}
		return null;
	}

	/**
	 * 1.檢查問卷日期:開始日期不能比結束日期晚，開始日期在結束日期之後就錯<br>
	 * 2.條件1 成立下，開始日期不能比當前日期早<br>
	 * 3.startDate.isAfter(endDate):<br>
	 * 3.1 startDate 早於 endDate --> false <br>
	 * 3.2 startDate 等於 endDate --> false <br>
	 * 3.3 startDate 晚於 endDate --> true
	 */
	private BasicRes checkDate(LocalDate startDate, LocalDate endDate) {
		// startDate.isAfter(endDate):表示開始日期不能等於小於結束日期
		// 阿下面的意思就是當開始時間在結束時間之後回傳true 但他是錯的所以回傳錯誤訊息
		if (startDate.isAfter(endDate)) {
			return new BasicRes(ResCodeMessage.QUIZ_DATE_ERROR.getCode()//
					, ResCodeMessage.QUIZ_DATE_ERROR.getMessage());
		}
		// 開始日期比當前時間早回true 但他是錯的所以報錯
		if (startDate.isBefore(LocalDate.now())) {
			return new BasicRes(ResCodeMessage.QUIZ_DATE_ERROR.getCode()//
					, ResCodeMessage.QUIZ_DATE_ERROR.getMessage());
		}

		return null;
	}

	@Transactional
	public BasicRes update(Quiz quiz, List<QuestionVo> questionVoList) throws Exception {
		try {
			// 檢查quiz是否存在於 DB
			int quizId = quiz.getId();
			// 搜尋欄位id出現次數 因為id是pk所以結果只會是0或1
			if (quizDao.selectCountId(quizId) == 0) {// 0代表不存在
				return new BasicRes(ResCodeMessage.NOT_FOUND.getCode()//
						, ResCodeMessage.NOT_FOUND.getMessage());
			}
			// 檢查問卷時間:開始時間不能筆結束時間晚

			BasicRes checkRes = checkDate(quiz.getStartDate(), quiz.getEndDate());
			if (checkRes != null) {// checkRes不等於NULL代表他檢查有錯要傳回去
				return checkRes;
			}
			// 跟新quiz
			quizDao.update(quizId, quiz.getTitle(), quiz.getDescription(), quiz.getStartDate(), quiz.getEndDate(),
					quiz.isPublish());
			// 跟新question
			// 1.刪除相同quizId的所有問卷
			questionDao.deleteByQuizId(quizId);
			// 2.新增問題
			// vo轉成Question entity後再將資料寫進去DB
			for (QuestionVo vo : questionVoList) {
				List<Options> optionsList = vo.getOptionsList();
				if (optionsList == null) {
					optionsList = new ArrayList<>();
				}
				// 將List<Options> 中的optionsList 轉為字串
				String optionStr = mapper.writeValueAsString(vo.getOptionsList());
				// 新增題目question
				// 確保這些問題都次同一個quizId-->使用quiz中取得的id
				questionDao.create(quizId, vo.getQuestionId(), vo.getName(), optionStr, //
						vo.getType(), vo.isRequired());
			}

			return new BasicRes(ResCodeMessage.SUCCESS.getCode()//
					, ResCodeMessage.SUCCESS.getMessage());
		} catch (Exception e) {
			throw e;
		}

	}

	public QuizListRes getQuizList(boolean getPublished) {
		if(getPublished) {//如果getPublished==true 取得已發布
			return new QuizListRes(ResCodeMessage.SUCCESS.getCode()//
					, ResCodeMessage.SUCCESS.getMessage(), quizDao.getPublishedAll());
			
		}
		return new QuizListRes(ResCodeMessage.SUCCESS.getCode()//
				, ResCodeMessage.SUCCESS.getMessage(), quizDao.getAll());
		
	}

	public QuizListRes getQuizList(String title, LocalDate startDate, LocalDate endDate,boolean getPublished) {
		// 假設 startDate 和 endDate --> 檢查 endDate > startDate
		if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
			return new QuizListRes(ResCodeMessage.QUIZ_DATE_ERROR.getCode(), //
					ResCodeMessage.QUIZ_DATE_ERROR.getMessage(), quizDao.getAll());
		}

		// 到title 沒帶值(預設null)或空字串 或全空白字串一律轉換成空字串
		// 到sql中搭配like %空字串的title% 就是把所有的title資料都撈出來
		if (!StringUtils.hasText(title)) {
			title = "";
		}
		// 轉換沒有帶值得開始日期 就把它設成很早的日期
		if (startDate == null) {
			startDate = LocalDate.of(1970, 1, 1);
		}
		// 轉換沒有帶值得結束日期 就把它設成很晚的日期
		if (endDate == null) {
			endDate = LocalDate.of(3000, 1, 1);
		}

		if(getPublished) {//getPublished等同於getPublished==true
			return new QuizListRes(ResCodeMessage.SUCCESS.getCode()//
					, ResCodeMessage.SUCCESS.getMessage(), quizDao.getPublishedSearch(title, startDate, endDate));
		}
		return new QuizListRes(ResCodeMessage.SUCCESS.getCode()//
				, ResCodeMessage.SUCCESS.getMessage(), quizDao.getSearch(title, startDate, endDate));
	
	}

	public QuestionListRes getQuestionList(int quizId) throws Exception {
		if (quizId <= 0) {
			return new QuestionListRes(ResCodeMessage.QUIZ_ID_ERROR.getCode()//
					, ResCodeMessage.QUIZ_ID_ERROR.getMessage());
		}
		// 檢查quizId是否存在DB
		// 搜尋欄位id出現次數 因為id是pk所以結果只會是0或1
		if (quizDao.selectCountId(quizId) == 0) {// 0代表不存在
			return new QuestionListRes(ResCodeMessage.NOT_FOUND.getCode()//
					, ResCodeMessage.NOT_FOUND.getMessage());
		}

		List<Question> questionList = questionDao.getByQuizId(quizId);
		// 建立List<QuestionVo>用來放下面for迴圈中所建立的每一個vo
		List<QuestionVo> voList = new ArrayList<>();
		// 將每一個 optionsStr轉成List<options>
		for (Question item : questionList) {
			// 用quizId從question表撈取資料
			try {
				List<Options> optionsList = mapper.readValue(item.getOptionsStr(), //
						new TypeReference<>() {
						});
				// 把每個question的值塞到Question的vo裡面 QuestionVo
				QuestionVo vo = new QuestionVo(item.getQuizId(), item.getQuestionId(), //
						item.getName(), optionsList, item.getType(), item.isRequired());
				// 將vo(QuestionVo) 新增到voList中
				voList.add(vo);
			} catch (Exception e) {
				throw e;
			}
		}
		return new QuestionListRes(ResCodeMessage.SUCCESS.getCode()//
				, ResCodeMessage.SUCCESS.getMessage(), voList);
	}

	//有動到多次表
	@Transactional(rollbackOn = Exception.class)
	public BasicRes deleteByQuizId(List<Integer> quizIdList) throws Exception  {
		try {
			//刪quiz
			quizDao.deleteByIdIn(quizIdList);
			//刪question
			questionDao.deleteByQuizIdIn(quizIdList);
			
		} catch (Exception e) {
			throw e;
		}	
		return new BasicRes(ResCodeMessage.SUCCESS.getCode()//
				, ResCodeMessage.SUCCESS.getMessage());
	}
	
		
}
