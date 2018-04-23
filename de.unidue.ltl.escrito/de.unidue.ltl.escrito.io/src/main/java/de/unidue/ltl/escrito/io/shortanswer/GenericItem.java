package de.unidue.ltl.escrito.io.shortanswer;

public class GenericItem
{
	protected String studentId;
    protected String text;
    protected String questionId;
    protected int grade;
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(studentId);
        sb.append(" (");
        sb.append(grade);
        sb.append(") ");
        String subStringText = text.length() > 40 ? text.substring(0, 40) : text.substring(0, text.length());
        sb.append(subStringText);
        sb.append(" ...");
        
        return sb.toString();        
    }
    
	public GenericItem(String studentId, String questionId, String text,
			int grade) {
		this.studentId = studentId;
		this.questionId = questionId;
		this.text = text;
		this.grade = grade;
	}

	public String getStudentId() {
		return studentId;
	}

	public void setStudentId(String studentId) {
		this.studentId = studentId;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getGrade() {
		return grade;
	}

	public void setGrade(int grade) {
		this.grade = grade;
	}

	public String getQuestionId() {
		return questionId;
	}

	public void setQuestionId(String questionId) {
		this.questionId = questionId;
	}
}
