package utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javafx.scene.chart.XYChart;

import stockapi.StockData;

public class XYSeriesGenerator {
    ArrayList<XYChart.Series<Number, Number>> chartSeries = new ArrayList<>();
    ArrayList<String> allDates = new ArrayList<>();

    // A hashMap for looking up the yValues for each series given an index integer
    // from the xAxis
    public HashMap<Integer, List<XYChart.Data<Number, Number>>> hashMap = new HashMap<>();

    public ArrayList<XYChart.Series<Number, Number>> getSeries() {
        return chartSeries;
    }

    public ArrayList<String> getAllDates() {
        return allDates;
    }

    public void populateSeries(Collection<StockData> data) {
        // Clear all previous data
        hashMap.clear();
        chartSeries.clear();
        allDates.clear();

        // Collect all dates present in stockdata
        setAllDates(data);

        // set all dates to another arraylist and sorts them
        Collections.sort(allDates);

        // iterate over all the stockdata objects and create XYCHART.series out of them
        for (var stockDataObject : data) {
            XYChart.Series<Number, Number> tempChart = createXYSeries(stockDataObject);
            chartSeries.add(tempChart);
        }
    }

    public void setAllDates(Collection<StockData> data) {
        for (var item : data) {
            for (var tick : item.getTicks()) {
                if (!(allDates.contains(tick.getRawDate()))) {
                    allDates.add(tick.getRawDate());
                }
            }
        }
    }

    public void reset() {
        allDates.clear();
        chartSeries.clear();
        hashMap.clear();
    }

    public XYChart.Series<Number, Number> createXYSeries(StockData data) {
        XYChart.Series<Number, Number> series = new XYChart.Series();
        series.setName(data.getSymbol());

        for (var tick : data.getTicks()) {
            XYChart.Data tickData = new XYChart.Data();
            tickData.setYValue(tick.getClose());
            tickData.setExtraValue(data.getSymbol());

            // Get the right position on the Xaxis by using the index of the tickdate from
            // the alldates Array
            int xValue = allDates.indexOf(tick.getRawDate());
            tickData.setXValue(xValue);
            series.getData().add(tickData);

            List<XYChart.Data<Number, Number>> list = hashMap.get(xValue);
            if (list == null) {
                hashMap.put(xValue, list = new ArrayList<>());
            }
            list.add(tickData);
        }

        return series;
    }
}
