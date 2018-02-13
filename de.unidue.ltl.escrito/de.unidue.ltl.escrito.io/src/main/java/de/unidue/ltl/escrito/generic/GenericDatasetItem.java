package de.unidue.ltl.escrito.generic;

public class GenericDatasetItem {

	private int grade;
	private String text;
	private String promptId;
	private String answerId;
	

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
		String subStringText = text.length() > 40 ? text.substring(0, 40) : text.substring(0, text.length());
		sb.append(subStringText);
		sb.append(" ...");

		return sb.toString();        
	}

	public GenericDatasetItem(String promptId, String answerId, String text, int grade) {
		this.promptId = promptId;
		this.answerId = answerId;
		this.text = text;
		this.grade = grade;
	}

	public int getGrade() {
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
	
}
