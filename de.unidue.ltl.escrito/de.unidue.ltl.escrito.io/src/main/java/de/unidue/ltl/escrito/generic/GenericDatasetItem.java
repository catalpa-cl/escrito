package de.unidue.ltl.escrito.generic;

import java.util.ArrayList;
import java.util.List;

public class GenericDatasetItem {

	private String grade;
	private String text;
	private String promptId;
	private String answerId;
	private String  targetAnswerId;
	

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(promptId);
		sb.append(" ");
		sb.append(answerId);
		sb.append(" ");
		sb.append(grade);
		sb.append(" ");
		sb.append(targetAnswerId);
		sb.append(" ");
		String subStringText = text.length() > 40 ? text.substring(0, 40) : text.substring(0, text.length());
		sb.append(subStringText);
		sb.append(" ...");

		return sb.toString();        
	}
	
	public GenericDatasetItem(String promptId, String answerId, String text, String grade, String targetAnswerId) {
		this.promptId = promptId;
		this.answerId = answerId;
		this.text = text;
		this.grade = grade;
		this.targetAnswerId = targetAnswerId;
	}

	public String getGrade() {
		return grade;
	}

	public String getText() {
		return text;
	}

	public String getPromptId() {
		return promptId;
	}

	public String getAnswerId() {
		return answerId;
	}
	
	public String getTargetAnswerId() {
		return targetAnswerId;
	}
	
}
