package com.idos.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import model.UserProfileSecurity;

public class RandomSecurityQuestion {
	
	private static final String[] objects = { "What is the name of your first school?",
		"What is the name of your first school friend?",
		"What is the name of your favourite teacher?",
		"What is your favourite food?",
		"Which place were you born?",
		"Which is your favourite holiday destination?",
		"What is your mother's maiden name?",
		"What is your first acheivement as a student?",
		"Who is your favourite sports person?",
		"When was the last time you threw a big party?"};
	
	public static String returnRandomQuestion(){
		String randomQuestion="";
		int length = objects.length;
		int rand = (int) (Math.random() * length);
		randomQuestion=objects[rand];
		return randomQuestion;
	}
	
	public static String returnRandomQuestion(String[] questionArr){
		String randomQuestion="";
		int length = questionArr.length;
		int rand = (int) (Math.random() * length);
		randomQuestion = questionArr[rand];
		return randomQuestion;
	}
	
	public static String returnRandomQuestion(List<UserProfileSecurity> securities){
		String[] array = null;
		if (!securities.isEmpty() && securities.size() > 0) {
			array = new String[securities.size()];
			for (int i = 0; i < securities.size(); i++) {
				array[i] = securities.get(i).getSecurityQuestion();
			}
		}
		return returnRandomQuestion(array);
	}
	
	public static String returnRandomQuestion(Collection<String> questions){
		return returnRandomQuestion(questions.toArray(new String[questions.size()]));
	}
	
	public static String [] getAllQuestions() {
		return objects;
	}
	
	public static String[] getNotInList(final String[] questionList) {
		List<String> lists = Arrays.asList(questionList);
		List<String> pendingQuestions = new ArrayList<String>();
		for (String s : objects) {
			if (!lists.contains(s)) {
				pendingQuestions.add(s);
			}
		}
		String list [] = new String[pendingQuestions.size()];
		list = pendingQuestions.toArray(list);
		return list;
	}
	
	public static String[] getNotInList(final Collection<String> questionList) {
		return getNotInList(questionList.toArray(new String[questionList.size()]));
	}	
	
}