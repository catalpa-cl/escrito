package de.unidue.ltl.escrito.io.shortanswer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MohlerMihalceaPreprocessor {

	
	public static void main(String[] args) throws IOException{
		int assignmentId = 1;
		String question = null;
		String answer = null;
		int questionId = 0;
		String[] files = {"assign1.txt", "assign2.txt", "assign3.txt"};
		for (String file: files){
			System.out.println(file);
			BufferedReader br = new BufferedReader(new FileReader(System.getenv("DKPRO_HOME")+"/datasets/mohler_and_mihalcea/basicDataset/"+file));
			String line = br.readLine();
			while (line != null){
				line = line.replaceAll("\r\n$", "");
				// remove encoded linebreaks
				line = line.replaceAll("<br>", "");
				Pattern pattern1 = Pattern.compile("^\t+Question: (.+)$");
				Pattern pattern2 = Pattern.compile("^\t+Answer: (.+)$");
				Pattern pattern3 = Pattern.compile("^(.+)\t\\[(.+)\\]\t(.+)$");
				Matcher m1 = pattern1.matcher(line);
				Matcher m2 = pattern2.matcher(line);
				Matcher m3 = pattern3.matcher(line);
				if (line.startsWith("#")){
					System.out.println(line);				
				} else if (m1.find()){
				    question = m1.group(1);
				    question = question.trim();
				    questionId++;
				} else if (m2.find()){
				    answer = m2.group(1);
				    answer = answer.trim();
				} else if (m3.find()){
					String learnerAnswer = m3.group(3);
					learnerAnswer = learnerAnswer.trim();
					System.out.println(questionId+"\t"+assignmentId+"\t"+m3.group(1)+"\t"+m3.group(2)+"\t"+m3.group(3)+"\t"+question+"\t"+answer);
				} else {
				    // do not do anything
				}		
				line = br.readLine();
			}
			br.close();
			assignmentId++;
		}
		
	}

}
