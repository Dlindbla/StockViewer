package Main;

import javafx.scene.chart.XYChart;

import java.util.*;

public class XYSeriesGenerator {

    ArrayList<XYChart.Series<Number,Number>> chartSeries = new ArrayList<>();
    ArrayList<String> allDates = new ArrayList<>();

    //A hashMap for looking up the yValues for each series given an index integer from the xAxis
    public HashMap<Integer, List<XYChart.Data<Number, Number>>> hashMap = new HashMap<>();

    public ArrayList<XYChart.Series<Number,Number>> getSeries(){
        return chartSeries;
    }

    public ArrayList<String> getAllDates(){return allDates;}

    public void populateSeries(Collection<StockData> data) {
        //Clear all previous data
        hashMap.clear();
        chartSeries.clear();
        allDates.clear();
        //Collect all dates present in stockdata
        setAllDates(data);
        //set all dates to another arraylist and sorts them
        Collections.sort(allDates);
        //iterate over all the stockdata objects and create XYCHART.series out of them
        for (StockData stockDataObject : data) {
            XYChart.Series<Number, Number> tempChart = createXYSeries(stockDataObject);
            chartSeries.add(tempChart);
        }
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
        hashMap.clear();
    }

    public XYChart.Series<Number,Number> createXYSeries(StockData data){
        XYChart.Series<Number, Number> series = new XYChart.Series();
        series.setName(data.getStockSymbol());
        for(StockTick tick: data.getStockTicks()){
            XYChart.Data tickData = new XYChart.Data();
            tickData.setYValue(tick.close);
            tickData.setExtraValue(data.getStockSymbol());
            //Get the right position on the Xaxis by using the index of the tickdate from the alldates Array
            int xValue = allDates.indexOf(tick.rawDate);
            tickData.setXValue(xValue);
            series.getData().add(tickData);

            List<XYChart.Data<Number,Number>> list = hashMap.get(xValue);
            if(list == null) hashMap.put(xValue, list = new ArrayList<>());
            list.add(tickData);

        }
        return series;
    }
}
