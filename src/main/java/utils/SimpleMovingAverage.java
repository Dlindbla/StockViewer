package utils;

import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;

public class SimpleMovingAverage {

    public static XYChart.Series<Number, Number> calculateSMA(XYChart.Series<Number,Number> stockChart ,int sampleSize){
        XYChart.Series<Number, Number> simpleMovingAverage = new XYChart.Series<>();
        ObservableList<XYChart.Data<Number,Number>> data = stockChart.getData();
        for(int i = 0; i < data.size()-sampleSize-1; i++){
            double value = 0;
            for(int j = i; j < sampleSize; j++){
                 value += data.get(j).getYValue().doubleValue();
            }
            XYChart.Data<Number,Number> dataPoint = new XYChart.Data<>(i,value);
            simpleMovingAverage.getData().add(dataPoint);
        }
        return simpleMovingAverage;
    }

}
