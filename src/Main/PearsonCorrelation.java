package Main;

import javafx.scene.chart.XYChart;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

public class PearsonCorrelation {

    public double calculateCorrelation(XYChart.Series<Number, Number> firstSeries, XYChart.Series<Number, Number> secondSeries) {


        //Create doubleArrays of size of firstSeries
        double[] item1Array = new double[firstSeries.getData().size()];
        double[] item2Array = new double[firstSeries.getData().size()];


        for (XYChart.Data<Number, Number> firstSeriesData : firstSeries.getData()) {
            int firstSeriesXValue = firstSeriesData.getXValue().intValue();//xAxis index value
            for (XYChart.Data<Number, Number> secondSeriesData : secondSeries.getData()) {
                int secondSeriesXValue = secondSeriesData.getXValue().intValue();
                if (firstSeriesXValue == secondSeriesXValue) {
                    //ITEM HAVE THE SAME X INDEX SO BOTH Y VALUES GET ADDED TO THE DOUBLE ARRAYS
                    //STOPS THE INNER FOR LOOP
                    item1Array[firstSeriesXValue] = firstSeriesData.getYValue().doubleValue();
                    item2Array[firstSeriesXValue] = secondSeriesData.getYValue().doubleValue();
                    continue;
                } else if (secondSeriesXValue > firstSeriesXValue) {
                    //IF THE SECOND ITEM HAS A SMALLER X VALUE WE WILL CONTINUE THE LOOP
                    //HOWEVER IS THE SECOND ITEM X VALUE IS LARGER THEN WE KNOW THAT IT WILL ONLY GROW AND NEVER MATCH THE FIRST X VALUE
                    continue;
                }
            }
        }
        double correlaction = new PearsonsCorrelation().correlation(item1Array, item2Array);
        return correlaction;
    }

}
