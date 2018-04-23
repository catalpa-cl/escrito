package de.unidue.ltl.escrito.io.shortanswer;

import java.util.ArrayList;
import java.util.List;

public class ItemWithTargetAnswer 
extends GenericItem
{

	private List<String> targetAnswers = new ArrayList<String>();
	
	public String getTargetAnswer() {
		return targetAnswers.get(0);
	}
	
	public List<String> getTargetAnswers() {
		return targetAnswers;
	}

	public void setTargetAnswer(String targetAnswer) {
		this.targetAnswers = new ArrayList<String>();
		this.targetAnswers.add(targetAnswer);
	}

	public ItemWithTargetAnswer(String studentId, String questionId, String learnerAnswer, int grade, String targetAnswer) {
		super(studentId, questionId, learnerAnswer, grade);
		this.targetAnswers.add(targetAnswer);
	}
	
	public ItemWithTargetAnswer(String studentId, String questionId, String learnerAnswer, int grade, String ... targetAnswers) {
		super(studentId, questionId, learnerAnswer, grade);
		for(String targetAnswer : targetAnswers)
			if(targetAnswer != null)
				this.targetAnswers.add(targetAnswer);
	}
	
	@Override
	public String toString()
	{
	     StringBuilder sb = new StringBuilder();
	     sb.append(questionId+" ");
	     sb.append(studentId);
	     sb.append(" (");
	     sb.append(grade);
	     sb.append(") ");
	     String subStringLA = text.length() > 40 ? text.substring(0, 40) : text.substring(0, text.length());
	     sb.append(subStringLA);
	     sb.append(" ...");
	     for(String targetAnswer : targetAnswers){
	    	String subStringTA = targetAnswer.length() > 40 ? targetAnswer.substring(0, 40) : targetAnswer.substring(0, targetAnswer.length());
	      	sb.append(subStringTA);
	       	sb.append(" ...");
	     }
	     return sb.toString();        
	}
}
