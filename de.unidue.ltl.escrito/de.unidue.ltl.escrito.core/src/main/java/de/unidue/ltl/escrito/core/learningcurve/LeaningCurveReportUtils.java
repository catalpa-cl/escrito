package de.unidue.ltl.escrito.core.learningcurve;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class LeaningCurveReportUtils {

	
	
	static void writeLatex(List<Double> numInstances, List<Double> minValues, List<Double> maxValues,
			List<Double> avgValues, String taskId, String ylabel, File file) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			bw.write("\\begin{tikzpicture}\n"
					//	+ "		\\node [draw=none] at (1,3) {2.3};\n"
					+ "\\begin{semilogxaxis}[\n"
					+ "ylabel = {"+ylabel+"},\n"
					+ "xlabel={\\# Trainingsdaten},\n"
					+ "height=5cm,\n"
					+ "width=6cm,\n"
					+ "ymin=0.0,\n"
					+ "ymax= 1.0,\n"
					+ "	    log basis x=2,\n"
					+ "		xtick={4,8,16,32,64,128},\n"
					+ "		ytick={0.0,0.1,...,1.1},legend style={at={(1.1,0.5)},anchor=west}\n"
					+ "		]\n"
					+ "		\\addplot[solid, color=red, thick] table[x=AmountTraining, y=Kappa] {AmountTraining Kappa\n");
			for (int i=0; i<numInstances.size(); i++){
				bw.write(numInstances.get(i)+" "+minValues.get(i)+"\n");
			}
			bw.write("};\n"
					+ "	\\addlegendentry{worst}\\legend{}\n"
					+ "	\\addplot[solid, color=green, thick] table[x=AmountTraining, y=Kappa] {AmountTraining Kappa\n");
			for (int i=0; i<numInstances.size(); i++){
				bw.write(numInstances.get(i)+" "+maxValues.get(i)+"\n");
			}
			bw.write("	};\n"
					+ "	\\addlegendentry{best}\\legend{}\n"
					+ "		\\addplot[solid, color=black, thick] table[x=AmountTraining, y=Kappa] {AmountTraining Kappa\n");
			for (int i=0; i<numInstances.size(); i++){
				bw.write(numInstances.get(i)+" "+avgValues.get(i)+"\n");
			}	
			bw.write("	};\n"
					+ "\\addlegendentry{average}\\legend{}\n"
					+ "	\\end{semilogxaxis}\n"
					+ "\\end{tikzpicture}\n");
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
}
