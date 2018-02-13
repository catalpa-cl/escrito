package de.unidue.ltl.escrito.io.shortanswer;

public class Asap2Item
{
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(answerId);
		sb.append("-");
		sb.append(promptId);
		sb.append(" ");
		sb.append(goldClass);
		sb.append("(");
		sb.append(valClass);
		sb.append(") ");
		String subStringText = text.length() > 40 ? text.substring(0, 40) : text.substring(0, text.length());
		sb.append(subStringText);
		sb.append(" ...");

		return sb.toString();        
	}

	private int answerId;
	private int promptId;
	private String goldClass;
	private String valClass;
	private String text;

	public Asap2Item(int answerId, int promptId, String goldClass, String valClass, String text)
	{
		super();
		this.answerId = answerId;
		this.promptId = promptId;
		this.goldClass = goldClass;
		this.valClass = valClass;
		this.text = text;
	}

	public int getTextId()
	{
		return answerId;
	}


	public int getPromptId()
	{
		return promptId;
	}

	
	public String getGoldClass()
	{
		return goldClass;
	}

	
	public String getValClass()
	{
		return valClass;
	}

	

	public String getText()
	{
		return text;
	}

	
}
