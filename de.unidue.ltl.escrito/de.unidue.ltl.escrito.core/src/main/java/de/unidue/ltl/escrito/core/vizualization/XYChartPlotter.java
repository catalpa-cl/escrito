package de.unidue.ltl.escrito.core.vizualization;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class XYChartPlotter {


	XYSeriesCollection dataset;
	String xLabel;
	String yLabel;
	String titel;

	public XYChartPlotter(String xLabel, String yLabel, String titel){
		dataset = new XYSeriesCollection( );
		this.xLabel = xLabel;
		this.yLabel = yLabel;
		this.titel = titel;
	}


	public void addSeries(List<Double> xValues, List<Double> yValues, String name){
		XYSeries series = new XYSeries(name);
		for (int i = 0; i<xValues.size(); i++){
			series.add(xValues.get(i), yValues.get(i));
		}
		dataset.addSeries(series);
	}


	public void plot(File file){
		JFreeChart xylineChart = ChartFactory.createXYLineChart(
				titel, 
				xLabel,
				yLabel, 
				dataset,
				PlotOrientation.VERTICAL, 
				true, true, false);

		int width = 640;   /* Width of the image */
		int height = 480;  /* Height of the image */ 
		try {
			ChartUtilities.saveChartAsJPEG( file, xylineChart, width, height);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}






}
