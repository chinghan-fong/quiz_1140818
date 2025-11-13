package com.example.quiz_1140818.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.quiz_1140818.constants.QuestionType;
import com.example.quiz_1140818.constants.ResCodeMessage;
import com.example.quiz_1140818.dao.FillinDao;
import com.example.quiz_1140818.dao.QuestionDao;
import com.example.quiz_1140818.dao.QuizDao;
import com.example.quiz_1140818.entity.Fillin;
import com.example.quiz_1140818.entity.Question;
import com.example.quiz_1140818.entity.Quiz;
import com.example.quiz_1140818.entity.User;
import com.example.quiz_1140818.response.BasicRes;
import com.example.quiz_1140818.response.FeedbackRes;
import com.example.quiz_1140818.response.StatisticRes;
import com.example.quiz_1140818.vo.Answer;
import com.example.quiz_1140818.vo.FeedbackVo;
import com.example.quiz_1140818.vo.Options;
import com.example.quiz_1140818.vo.OptionsCount;
import com.example.quiz_1140818.vo.QuestionAnswerVo;
import com.example.quiz_1140818.vo.QuestionCountVo;
import com.example.quiz_1140818.vo.StatisticVo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

@Service
public class FillinService {

	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private FillinDao fillinDao;

	@Autowired
	private QuestionDao questionDao;
	
	@Autowired
	private QuizDao quizDao;


	@Transactional(rollbackOn = Exception.class)
	public BasicRes fillin(User user, int quizId, List<Answer> answerList) throws Exception {
		// 要檢查答案，檢查的基準是存在DB中同一張的問題
		// 1.取出同張一問卷的所有問題
		List<Question> questionList = questionDao.getByQuizId(quizId);

		for (Question question : questionList) {
			// 2.檢查必填是否都有答案
			if (question.isRequired()) {
				BasicRes res = checkReqiredAnswer(question.getQuestionId(), answerList, question.getType());
				if (res != null) {
					return res;
				}
			}
			// 3. 相同問題編號下答案中的選項是否跟問卷中的選項一樣
			List<Options> reqOptionList = new ArrayList<>();
			for (Answer answer : answerList) {
				if (question.getQuestionId() == answer.getQuestionId()) {
					reqOptionList = answer.getOptionsList();
					break;
				}
			}
			try {
				BasicRes res = checkOptions(question.getOptionsStr(), reqOptionList);
				if(res!=null) {
					return res;
				}
			} catch (Exception e) {
				throw e;
			}

		}
		//4.寫答案
		try {
			for(Answer item: answerList) {
				fillinDao.fillin(quizId, item.getQuestionId(), user.getEmail(), user.getName(), user.getPhone(),//
						user.getAge(), user.getGender(), mapper.writeValueAsString(item),LocalDateTime.now());
			}
		}catch (Exception e) {
			throw e;
		}
		
		return new BasicRes(ResCodeMessage.SUCCESS.getCode()//
				, ResCodeMessage.SUCCESS.getMessage());
	}

	private BasicRes checkReqiredAnswer(int questionId, List<Answer> answerList, String type) {
		for (Answer answer : answerList) {
			// 比對相同問題編號，確認問題型態所對應的answer是否有值
			if (answer.getQuestionId() == questionId) {
				if (type.equalsIgnoreCase(QuestionType.SINGLE.getType())) {
					if (answer.getRadioAnswer() <= 0) {
						return new BasicRes(ResCodeMessage.RADIO_ANSWER_IS_REQUIRED.getCode()//
								, ResCodeMessage.RADIO_ANSWER_IS_REQUIRED.getMessage());
					}
				} else if (type.equalsIgnoreCase(QuestionType.TEXT.getType())) {
					if (StringUtils.hasText(answer.getTextAnswer())) {
						return new BasicRes(ResCodeMessage.TEXT_ANSWER_IS_REQUIRED.getCode()//
								, ResCodeMessage.TEXT_ANSWER_IS_REQUIRED.getMessage());
					}
				} else {// 問題類型是多選
					for (Options item : answer.getOptionsList()) {
						// 檢查至少有一個checkBoolean的值是true
						if (item.isCheckBoolean()) {
							return null;
						}
						return new BasicRes(ResCodeMessage.CHECKBOX_ANSWER_IS_REQUIRED.getCode()//
								, ResCodeMessage.CHECKBOX_ANSWER_IS_REQUIRED.getMessage());
					}
				}
			}
		}
		return null;
	}

	private BasicRes checkOptions(String optionsStr, List<Options> reqOptionsList) throws Exception {
		// 轉換optionsStr 成物件List<Options>
		try {
			List<Options> optionsList = mapper.readValue(optionsStr, new TypeReference<>() {
			});
			for (Options item : optionsList) {
				int code = item.getCode();
				String optionName = item.getOptionName();
				for (Options reqItem : reqOptionsList) {
					// 相同編號底下若選項不一樣則回傳錯誤
					if (code == reqItem.getCode()) {
						if (!optionName.equalsIgnoreCase(reqItem.getOptionName())) {
							return new BasicRes(ResCodeMessage.QUESTION_OPTION_MISMATCH.getCode()//
									, ResCodeMessage.QUESTION_OPTION_MISMATCH.getMessage());
						}
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}

		return null;
	}
//	public FeedbackRes feedback(int quizId) throw 
	
	
//	public FeedbackRes feedback(int quizId) throws Exception {
//		if (quizId <= 0) {
//			return new FeedbackRes(ResCodeMessage.QUIZ_ID_ERROR.getCode(), //
//					ResCodeMessage.QUIZ_ID_ERROR.getMessage());
//		}
//		List<Question> questionList = questionDao.getByQuizId(quizId);
//		// 將 Question 轉成 QuestionAnswerVo
//		Map<Integer, QuestionAnswerVo> map = new HashMap<>();
//		for (Question question : questionList) {
//			QuestionAnswerVo vo = new QuestionAnswerVo(question.getQuizId(), //
//					question.getQuestionId(), question.getName(), //
//					question.getType(), question.isRequired());
//			// 問題編號，vo
//			map.put(question.getQuestionId(), vo);
//		}
//		Quiz quiz = quizDao.getById(quizId);
//		
//		List<Fillin> fillinList = fillinDao.getByQuizId(quizId);
//		//====================================
//		// 一個 email 表示一位使用者的 FeedbackVo
//		Map<String, FeedbackVo> emailFeedbackVoMap = new HashMap<>();
//		for (Fillin item : fillinList) {
//			FeedbackVo feedbackVo = new FeedbackVo();
//			List<QuestionAnswerVo> questionAnswerVoList = new ArrayList<>();
//			String email = item.getEmail();
//			if(!emailFeedbackVoMap.containsKey(email)) { // 表示尚未記錄到該 user 的答案
//				User user = new User(item.getName(), item.getPhone(), item.getEmail(), //
//						item.getAge(), item.getGender());
//				// 將 User、Quiz、QuestionVoList、FillinDate 設定到 feedbackVo
//				feedbackVo.setUser(user);
//				feedbackVo.setQuiz(quiz);
//				feedbackVo.setFillinDate(item.getFillinDate());
//				feedbackVo.setQuestionVoList(questionAnswerVoList);
//				emailFeedbackVoMap.put(email,feedbackVo);
//			} else {
//				feedbackVo = emailFeedbackVoMap.get(email);
//				questionAnswerVoList = feedbackVo.getQuestionVoList();
//			}
//			
//			try {
//				Answer ans = mapper.readValue(item.getAnswerStr(), Answer.class);
//				// 透過 questionId 當作 key 從 map 中取得對應的 QuestionAnswerVo
//				QuestionAnswerVo vo = map.get(item.getQuestionId());
//				vo.setOptionsList(ans.getOptionsList());
//				vo.setTextAnswer(ans.getTextAnswer());
//				vo.setRadioAnswer(ans.getRadioAnswer());
//				questionAnswerVoList.add(vo);
//			} catch (Exception e) {
//				throw e;
//			}
//			
//		}
//		// 將 emailFeedbackVoMap 的 FeedbackVo 增加到 feedbackVoList
//		List<FeedbackVo> feedbackVoList = new ArrayList<>();
//		for(Entry<String, FeedbackVo> mapItem : emailFeedbackVoMap.entrySet()) {
//			feedbackVoList.add(mapItem.getValue());
//		}
//		return new FeedbackRes(ResCodeMessage.SUCCESS.getCode(), //
//				ResCodeMessage.SUCCESS.getMessage(), feedbackVoList);
//	}

	public FeedbackRes feedback(int quizId) throws Exception {
	    if (quizId <= 0) {
	        return new FeedbackRes(ResCodeMessage.QUIZ_ID_ERROR.getCode(),
	                ResCodeMessage.QUIZ_ID_ERROR.getMessage());
	    }

	    List<Question> questionList = questionDao.getByQuizId(quizId);
	    Quiz quiz = quizDao.getById(quizId);

	    List<Fillin> fillinList = fillinDao.getByQuizId(quizId);

	    Map<String, FeedbackVo> emailFeedbackVoMap = new HashMap<>();

	    for (Fillin item : fillinList) {
	        String email = item.getEmail();
	        FeedbackVo feedbackVo;
	        List<QuestionAnswerVo> questionAnswerVoList;

	        if (!emailFeedbackVoMap.containsKey(email)) {
	            User user = new User(item.getName(), item.getPhone(), item.getEmail(),
	                    item.getAge(), item.getGender());

	            feedbackVo = new FeedbackVo();
	            feedbackVo.setUser(user);
	            feedbackVo.setQuiz(quiz);
	            feedbackVo.setFillinDate(item.getFillinDate());

	            questionAnswerVoList = new ArrayList<>();
	            feedbackVo.setQuestionVoList(questionAnswerVoList);

	            emailFeedbackVoMap.put(email, feedbackVo);
	        } else {
	            feedbackVo = emailFeedbackVoMap.get(email);
	            questionAnswerVoList = feedbackVo.getQuestionVoList();
	        }

	        try {
	            Answer ans = mapper.readValue(item.getAnswerStr(), Answer.class);

	            Question question = questionList.stream()
	                    .filter(q -> q.getQuestionId() == item.getQuestionId())
	                    .findFirst()
	                    .orElse(null);

	            if (question != null) {
	                QuestionAnswerVo vo = new QuestionAnswerVo(
	                        question.getQuizId(),
	                        question.getQuestionId(),
	                        question.getName(),
	                        question.getType(),
	                        question.isRequired()
	                );

	                vo.setOptionsList(ans.getOptionsList());
	                vo.setTextAnswer(ans.getTextAnswer());
	                vo.setRadioAnswer(ans.getRadioAnswer());

	                questionAnswerVoList.add(vo);
	            }

	        } catch (Exception e) {
	            // 捕捉解析錯誤，但繼續處理其他資料
	            e.printStackTrace();
	        }
	    }

	    List<FeedbackVo> feedbackVoList = new ArrayList<>(emailFeedbackVoMap.values());

	    return new FeedbackRes(ResCodeMessage.SUCCESS.getCode(),
	            ResCodeMessage.SUCCESS.getMessage(),
	            feedbackVoList);
	}
	
	
	
	
	public StatisticRes statistic(int quizId) throws Exception {
		//判斷存不存在
		if (quizId <= 0) {
			return new StatisticRes(ResCodeMessage.QUIZ_ID_ERROR.getCode(), //
					ResCodeMessage.QUIZ_ID_ERROR.getMessage());
		}
		// 將問題相關的資訊設定給 QuestionAnswerVo
		//  questionId, QuestionCountVo
		Map<Integer, QuestionCountVo> voMap = setQuestionAnswerVo(quizId);
		// 使用 QuizId 撈取所有的填答
		List<Fillin> fillinList = fillinDao.getByQuizId(quizId);
		// 	問題編號,     	選項編號      		選項     次數
		Map<Integer, Map<Integer, Map<String, Integer>>> map = new HashMap<>();
		for (Fillin fillin : fillinList) {
			try {
				// 1. 把 answer_str 轉成 Answer
				Answer ans = mapper.readValue(fillin.getAnswerStr(), Answer.class);
				// 2. 統計次數
				// 2.1 簡答題
				if (StringUtils.hasText(ans.getTextAnswer())) {
					// textAnswer 有內容的話，表示該題是簡答題 --> 跳過
					continue;
				}
				//  選項編號,      選項,  次數			
				Map<Integer, Map<String, Integer>> codeOpCountMap = new HashMap<>();
				if (map.containsKey(ans.getQuestionId())) {
					// 若問題編號已存在，則把對應的 選項編號、選項、次數的 Map 取出
					codeOpCountMap = map.get(ans.getQuestionId());
				}
				
				// 2.2 多選題: 先做的原因是因為要先取得選項編號與選項，而其答案是綁定在 List<Options> 中
				//             可以順便蒐集次數
				for (Options op : ans.getOptionsList()) {
					// 先判斷 opCountMap 中是否已有蒐集過的選項編號
					if(codeOpCountMap.containsKey(op.getCode())) {
						// 有蒐集過的選項編號
						// 判斷 checkBoolean 的值是否為 true 
						if(op.isCheckBoolean()) {
							// --> 取出對應的 value (選項和次數的 map)
							//    選項, 次數
							Map<String, Integer> opCountMap = codeOpCountMap.get(op.getCode());
							// --> 取出選項對應的次數後再 + 1
							int count = opCountMap.get(op.getOptionName()) + 1;
							// --> 將更新後的次數放回(put) opCountMap
							opCountMap.put(op.getOptionName(), count);
							// codeOpCountMap 不需要更新，因為其對應 value 的記憶體上的值(opCountMap)已更新

						}
					} else {
						// 沒有蒐集過的選項編號 --> 建立新的, 次數是 0
						Map<String, Integer> opCountMap = new HashMap<>();
						int count = 0;
						// checkBoolean 的值是否為 true
						if(op.isCheckBoolean()) {
							// 有的話 --> 次數變成 1
							count = 1;
						}
						opCountMap.put(op.getOptionName(), count);
						// 將結果更新回 codeOpCountMap
						//一開始沒東西所以要加回去
						
						//key 應該是選項編號 (op.getCode())，而不是問題編號。
						//那每個問題的多個選項都會共用同一個 key（問題編號），導致：
						//每新增一個選項時會覆蓋前面的選項資料
						//最後統計出來只有最後一個選項的次數，其他選項會被覆蓋掉
						codeOpCountMap.put(op.getCode(), opCountMap);//改這樣
						//codeOpCountMap.put(ans.getQuestionId(), opCountMap);
					}
				}
				// 至此選項編號和選項已蒐集完畢
				// 2.3 單選題
				//radioanswer選項編號
				
				//把資料存進去 沒有這一個資料會是空的
				 map.put(ans.getQuestionId(), codeOpCountMap);//還有這裡
			} catch (Exception e) {
				throw e;
			}
		}
		// 將每一題中每個編號的選項和次數設定回 QuestionCountVo
		List<QuestionCountVo> voList = setAndGetQuestionCountVoList(map, voMap);
		Quiz quiz = quizDao.getById(quizId);
		StatisticVo statisticVo = new StatisticVo(quiz, voList);
		return new StatisticRes(ResCodeMessage.SUCCESS.getCode(), //
				ResCodeMessage.SUCCESS.getMessage(), statisticVo);
	}
	
	//將question的所有選擇題的基本資訊(不包含選項和次數)設定到questionCountVo
	private Map<Integer, QuestionCountVo> setQuestionAnswerVo(int quizId) {
		// 將問題相關的資訊設定給 QuestionAnswerVo
		List<Question> questionList = questionDao.getByQuizId(quizId);
		// 將 Question 轉成 QuestionAnswerVo
		Map<Integer, QuestionCountVo> map = new HashMap<>();
		for (Question question : questionList) {
			// 跳過簡答題
			if (QuestionType.checkTextType(question.getType())) {
				continue;
			}
			QuestionCountVo vo = new QuestionCountVo(//
					question.getQuestionId(), question.getName(), //
					question.getType(), question.isRequired());
			// 問題編號，vo
			map.put(question.getQuestionId(), vo);
			
		}
		return map;
	}
	
	private List<QuestionCountVo> setAndGetQuestionCountVoList(Map<Integer, Map<Integer, Map<String, Integer>>> map, //
			Map<Integer, QuestionCountVo> voMap) {
		List<QuestionCountVo> voList = new ArrayList<>();
		for(int questionId : map.keySet()) {
			List<OptionsCount> opCountList = new ArrayList<>();
			// 取出對應的 Map<選項編號, Map<選項, 次數>>
			Map<Integer, Map<String, Integer>> codeOpCountMap = map.get(questionId);	
			// 以下2種寫法擇一
			// 寫法1
			for(int code : codeOpCountMap.keySet()) {
				Map<String, Integer> opNameCountMap = codeOpCountMap.get(code);
				for(String opName : opNameCountMap.keySet()) {
					int count = opNameCountMap.get(opName);
					OptionsCount opCount = new OptionsCount(code, opName, count);
					opCountList.add(opCount);
				}
				
			}
			// 寫法2: 以下是 Lambda 寫法: 執行效率有比上面的程式碼好
//			codeOpCountMap.forEach((code, v) -> {
//				v.forEach((opName, count) -> {
//					OptionsCount opCount = new OptionsCount(code, opName, count);
//					opCountList.add(opCount);
//				});
//			});
			// voMap 是之前先整理過的 Map<問題編號, QuestionCountVo>，所以所有選擇題都會有
			QuestionCountVo vo = voMap.get(questionId);
			if (vo == null) {
			    // 可能是簡答題或資料不完整 → 跳過
			    continue;
			}
			vo.setOptionsCountList(opCountList);
			voList.add(vo);
		}
		return voList;
	}


}
