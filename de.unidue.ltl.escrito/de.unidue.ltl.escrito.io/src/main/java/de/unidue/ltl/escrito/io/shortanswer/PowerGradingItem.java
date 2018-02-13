package de.unidue.ltl.escrito.io.shortanswer;

public class PowerGradingItem
{

	private int grader1;
	private int grader2;
	private int grader3;
	private String text;
	private int promptId;
	private String studentId;

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(promptId);
		sb.append(" (");
		sb.append(grader1 + "/" + grader2 + "/" + grader3);
		sb.append(") ");
		String subStringText = text.length() > 40 ? text.substring(0, 40) : text.substring(0, text.length());
		sb.append(subStringText);
		sb.append(" ...");

		return sb.toString();        
	}

	public PowerGradingItem(String studentId, int promptId, String text,
			int grader1, int grader2, int grader3) {
		this.studentId = studentId;
		this.promptId = promptId;
		this.text = text;
		this.grader1 = grader1;
		this.grader2 = grader2;
		this.grader3 = grader3;
	}

	public int getGrader1() {
		return grader1;
	}

	public int getGrader2() {
		return grader2;
	}

	public int getGrader3() {
		return grader3;
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


}
