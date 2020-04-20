package utils;

import javafx.scene.chart.XYChart;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import java.util.ArrayList;

public class PearsonCorrelation {
    public double calculateCorrelation(XYChart.Series<Number, Number> firstSeries,
            XYChart.Series<Number, Number> secondSeries) {
        // Create doubleArrays of size of firstSeries
        double[] item1Array = new double[firstSeries.getData().size()*10];
        double[] item2Array = new double[secondSeries.getData().size()*10];

        System.out.println(firstSeries.getData().size());



        for (XYChart.Data<Number, Number> firstSeriesData : firstSeries.getData()) {
            int firstSeriesXValue = firstSeriesData.getXValue().intValue(); // xAxis index value
            for (XYChart.Data<Number, Number> secondSeriesData : secondSeries.getData()) {
                int secondSeriesXValue = secondSeriesData.getXValue().intValue(); // yAxis index value
                if (firstSeriesXValue == secondSeriesXValue) {
                    // ITEM HAVE THE SAME X INDEX SO BOTH Y VALUES GET ADDED TO THE DOUBLE ARRAYS
                    // STOPS THE INNER FOR LOOP
                    item1Array[firstSeriesXValue] = firstSeriesData.getYValue().doubleValue();
                    item2Array[firstSeriesXValue] = secondSeriesData.getYValue().doubleValue();
                    break;
                }
            }
        }

        return new PearsonsCorrelation().correlation(item1Array, item2Array);
    }
}
