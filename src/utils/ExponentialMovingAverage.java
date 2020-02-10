package utils;

import javafx.scene.chart.XYChart;

public class ExponentialMovingAverage {

    public static XYChart.Series<Number, Number> calculate(XYChart.Series<Number, Number> stockChart, int sampleSize) {
        XYChart.Series<Number, Number> exponentialMovingAverage = new XYChart.Series<>();
        exponentialMovingAverage.setName(stockChart.getName() + "_EMA");
        //get initial SMA for samplesize in stockChart
        double initSMA = 0;
        for (int i = 0; i < sampleSize; i++) {
            initSMA += stockChart.getData().get(i).getYValue().doubleValue();
        }
        double soothingValue = 2.0 / (sampleSize + 1);
        //start calculating EMAs
        for (int i = sampleSize; i < stockChart.getData().size(); i++) {
            double currentValue = stockChart.getData().get(i).getYValue().doubleValue();
            double expMA = (currentValue - initSMA) * soothingValue + initSMA;
            XYChart.Data<Number, Number> dataPoint = new XYChart.Data<>(i, expMA);
            exponentialMovingAverage.getData().add(dataPoint);
        }
        return exponentialMovingAverage;
    }
}
