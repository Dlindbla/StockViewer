package Main;

import javafx.scene.chart.XYChart;

import java.lang.reflect.Array;
import java.util.*;


public class XYSeriesGenerator {

    ArrayList<XYChart.Series> chartSeries = new ArrayList<>();
    ArrayList<String> allDates = new ArrayList<>();

    public ArrayList<XYChart.Series> getSeries(){
        return chartSeries;
    }

    public void populateSeries(Collection<StockData> data) {
        setKnownDates(data);
        ArrayList<StockData> items = new ArrayList<>();
        items.addAll(data);
        Collections.sort(items);
        createHiddenSeries(data);
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

    public void testSeries(){

        XYChart.Series emptySeries = new XYChart.Series();
        emptySeries.setName("you can't see me");

        emptySeries.getData().add(new XYChart.Data("0",0));
        emptySeries.getData().add(new XYChart.Data("1",0));
        emptySeries.getData().add(new XYChart.Data("2",0));
        emptySeries.getData().add(new XYChart.Data("1",0));
        emptySeries.getData().add(new XYChart.Data("3",0));
        emptySeries.getData().add(new XYChart.Data("4",0));




        XYChart.Series series = new XYChart.Series();
        series.setName("Test short");
        series.getData().add(new XYChart.Data("1",1));
        //series.getData().add(new XYChart.Data("2",2));
        series.getData().add(new XYChart.Data("3",3));
        series.getData().add(new XYChart.Data("2",2));

        XYChart.Series series2 = new XYChart.Series();
        series.setName("Test longer");
        series2.getData().add(new XYChart.Data("0",1));
        series2.getData().add(new XYChart.Data("1",2));
      //  series2.getData().add(new XYChart.Data("2",3));
        series2.getData().add(new XYChart.Data("3",4));
        series2.getData().add(new XYChart.Data("4",5));

        chartSeries.add(emptySeries);
        chartSeries.add(series2);
        chartSeries.add(series);
    }

    public void setKnownDates(Collection<StockData> data){
        for (StockData item:data){
            for (StockTick tick : item.getStockTicks()){
                if(!(allDates.contains(tick.rawDate))){
                    allDates.add(tick.rawDate);
                }
            }
        }
    }

    //Create a hidden series that contains all the values of the
    public void createHiddenSeries(Collection<StockData> data){
        //Get the arbitrary close value of an item in the collection and assign the hidden series to use this as its value
        double value = data.stream().findFirst().get().getStockTicks().stream().findFirst().get().close;
        XYChart.Series hiddenSeries = new XYChart.Series();
        for (int i = 0; i < allDates.size(); i++) {
            String date = allDates.get(i);
            XYChart.Data dataPoint = new XYChart.Data(date,value);
            //dataPoint.getNode().setVisible(false);
            hiddenSeries.getData().add(dataPoint);
        }
        chartSeries.add(hiddenSeries);
    }

    public void updateHiddenSeries(Collection<StockData> data){
        double value = data.stream().findFirst().get().getStockTicks().stream().findFirst().get().close;
        XYChart.Series hiddenSeries = chartSeries.get(0);

        //TODO: Implementing a method that updates and adds all new dates to the hiddenSeries

    }

}
