package utils;

import javafx.scene.chart.XYChart;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class GraphGenerator {
    private ArrayList<XYChart.Series<Number, Number>> chartSeries = new ArrayList<>();
    public ArrayList<Comparable> xIndexList = new ArrayList<>();

    public HashMap<Integer, List<XYChart.Data<Number, Number>>> hashMap = new HashMap<>();

    public ArrayList<XYChart.Series<Number, Number>> getSeries() {
        return chartSeries;
    }

    public ArrayList<Comparable> getxIndexList() {
        return xIndexList;
    }

    public void populateSeries(ArrayList<PlottableObject> plottableObjects) {
        // Clear all previous data
        hashMap.clear();
        chartSeries.clear();
        xIndexList.clear();

        // Collect all xIndexItems, sort them
        setxIndexList(plottableObjects);
        Collections.sort(xIndexList);

        // iterate over all the plottable objects and create XY.ChartSeries items out of
        // them
        for (var item : plottableObjects) {
            XYChart.Series<Number, Number> tempChart = createXYSeries(item);
            chartSeries.add(tempChart);
        }

    }

    public XYChart.Series<Number, Number> createXYSeries(PlottableObject plottableObject) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(plottableObject.getName());

        for (var item : plottableObject.getItems()) {
            XYChart.Data<Number, Number> tempTick = new XYChart.Data<>();
            tempTick.setExtraValue(new Pair<>(plottableObject.getName(), plottableObject.getCurrency()));
            tempTick.setYValue(item.getValue());

            // Get the right index for the xValue by getting the dates index from xIndexList
            int xValue = xIndexList.indexOf(item.getKey());
            tempTick.setXValue(xValue);
            series.getData().add(tempTick);

            // Attempts to find the hashMap key for the ticks xValue and add the tick to it
            // if a value isn't found it then creates a list with the key of xValue
            List<XYChart.Data<Number, Number>> list = hashMap.get(xValue);
            if (list == null) {
                hashMap.put(xValue, list = new ArrayList<>());
            }
            list.add(tempTick);
        }
        return series;
    }



    public void reset() {
        xIndexList.clear();
        chartSeries.clear();
        hashMap.clear();
    }

    public void setxIndexList(ArrayList<PlottableObject> items) {
        for (var item : items) {
            for (var pair : item.getItems()) {
                xIndexList.add(pair.getKey());
            }
        }
    }
}
