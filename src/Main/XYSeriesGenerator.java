package Main;

import javafx.scene.chart.XYChart;

import java.util.*;


public class XYSeriesGenerator {

    ArrayList<XYChart.Series> chartSeries = new ArrayList<>();
    ArrayList<String> allDates = new ArrayList<>();


    public ArrayList<XYChart.Series> getSeries(){
        return chartSeries;
    }


    public void populateSeries(Collection<StockData> data) {
        chartSeries.clear();
        allDates.clear();
        setAllDates(data);
        ArrayList<StockData> items = new ArrayList<>();
        items.addAll(data);
        Collections.sort(items);

        chartSeries.add(createHiddenSeries(data));
        for (StockData stockDataObject : items) {
            XYChart.Series tempChart = createXYChart(stockDataObject);
            chartSeries.add(tempChart);
        }
    }


    public XYChart.Series createXYChart(StockData stockData) {
        XYChart.Series series = new XYChart.Series();
        series.setName(stockData.getStockSymbol());
        for (StockTick tick : stockData.getStockTicks()) {
            XYChart.Data tempData = new XYChart.Data();
            tempData.setXValue(tick.rawDate);
            tempData.setYValue(tick.close);
            series.getData().add(tempData);
        }
        return series;
    }

    public void setAllDates(Collection<StockData> data){
        for (StockData item:data){
            for (StockTick tick : item.getStockTicks()){
                if(!(allDates.contains(tick.rawDate))){
                    allDates.add(tick.rawDate);
                }
            }
        }
    }
    public void reset(){
        allDates.clear();
        chartSeries.clear();
    }

    //Create a hidden series that contains all the values of the
    public XYChart.Series createHiddenSeries(Collection<StockData> data){
        //Get the arbitrary close value of an item in the collection and assign the hidden series to use this as its value
        double value = data.stream().findFirst().get().getStockTicks().stream().findFirst().get().close;
        XYChart.Series hiddenSeries = new XYChart.Series();

        hiddenSeries.getData().add(new XYChart.Data("", value));

        for (int i = 0; i < allDates.size(); i++) {
            String date = allDates.get(i);
            XYChart.Data dataPoint = new XYChart.Data(date,value);
            hiddenSeries.getData().add(dataPoint);
        }
        return hiddenSeries;
    }


}
