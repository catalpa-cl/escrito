package de.unidue.ltl.escrito.io.shortanswer;

public class MohlerMihalceaItem 
{

	private int score;
	private String text;
	private int promptId;
	private String studentId;
	private int assignmentId;


	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(promptId);
		sb.append(" (");
		sb.append(score);
		sb.append(") ");
		String subStringText = text.length() > 40 ? text.substring(0, 40) : text.substring(0, text.length());
		sb.append(subStringText);
		sb.append(" ...");

		return sb.toString();        
	}

	public MohlerMihalceaItem(String studentId, int questionId, String text,
			int score, int assignmentId) {
		this.studentId = studentId;
		this.promptId = questionId;
		this.text = text;
		this.score = score;
		this.assignmentId = assignmentId;
	}

	public int getScore() {
		return score;
	}

	public String getText() {
		return text;
	}

	public int getPromptId() {
		return promptId;
	}

	public String getStudentId() {
		return studentId;
	}

	public int getAssignmentId() {
		return this.assignmentId;
	}

}
