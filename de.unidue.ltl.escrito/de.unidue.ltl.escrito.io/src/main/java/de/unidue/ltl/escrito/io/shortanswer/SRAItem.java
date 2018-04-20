package de.unidue.ltl.escrito.io.shortanswer;

import java.util.ArrayList;

public class SRAItem extends ItemWithTargetAnswer{
	public String toString(){
		StringBuilder sb = new StringBuilder();
		//sb.append("[")
		//sb.append(questionId);
		//sb.append("]")
		//sb.append("[")
		//sb.append(module);
		//sb.append("]")
		String subStringId= answerId.length() > 40 ? answerId.substring((answerId.length()-13),(answerId.length()-1)) : text.substring(0, text.length());
        sb.append(subStringId);
		sb.append(" (");
        sb.append(nWayGrade);
        sb.append(") ");
        String subStringText = text.length() > 40 ? text.substring(0, 40) : text.substring(0, text.length());
        sb.append(subStringText);
        sb.append(" ...");
        String subStringTA = getTargetAnswer().length() > 40 ? getTargetAnswer().substring(0, 40) : getTargetAnswer().substring(0, getTargetAnswer().length());
        sb.append(subStringTA);
        sb.append(" ...");
        return sb.toString();
	}
	
	private String answerId;
	private String nWayGrade;
	private String module;
	private ArrayList<String>  targetAnswerIds;

	

	public ArrayList<String> getTargetAnswerIds() {
		return targetAnswerIds;
	}
	public void setTargetAnswerIds(ArrayList<String> targetAnswerIds) {
		this.targetAnswerIds = targetAnswerIds;
	}
	public SRAItem(String questionId, String module, String answerId, String text,  String nWayGrade, String targetAnswer) {
		super("",questionId, text,-1,targetAnswer);
		this.module=module;
		this.answerId=answerId;
		this.nWayGrade=nWayGrade;		
	}
	public String getModule(){
		return module;
	}
	
	public void setModule(String module){
		this.module = module;
	}
	public String getAnswerId(){
		return answerId;
	}
	
	public void setAnswerId(String answerId){
		this.answerId = answerId;
	}

	public String getnWayGrade(){
		return nWayGrade;
	}

	public void setnWayGrade(String nWayGrade) {
		this.nWayGrade = nWayGrade;
	}

}
