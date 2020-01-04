package Main;

import javafx.scene.chart.XYChart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;


public class XYSeriesGenerator {

    ArrayList<XYChart.Series> chartSeries = new ArrayList<>();

    public ArrayList<XYChart.Series> getSeries(){
        return chartSeries;
    }

    public void populateSeries(Collection<StockData> data) {
        for (StockData stockDataObject : data) {
            XYChart.Series tempChart = createXYChart(stockDataObject);
            chartSeries.add(tempChart);
        }
    }

    public XYChart.Series createXYChartByDate(StockData stockData, Date startDate, Date stopDate){
        XYChart.Series series = new XYChart.Series();
        series.setName(stockData.getStockSymbol());
        for (StockTick tick: stockData.getStockTicks()){
            if(tick.dateTime.after(startDate) && tick.dateTime.before(stopDate)){
                XYChart.Data tempData = new XYChart.Data();
                tempData.setXValue(tick.dateTime.toString());
                tempData.setYValue(tick.close);
                series.getData().add(tempData);
            }
        }
        return series;
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

    public XYChart.Series numberTest(StockData stockData) {
        XYChart.Series series = new XYChart.Series();
        series.setName(stockData.getStockSymbol());
        for (StockTick tick : stockData.getStockTicks()) {
            XYChart.Data tempData = new XYChart.Data();
            tempData.setXValue(tick.dateTime.getTime());
            tempData.setYValue(tick.close);
            series.getData().add(tempData);
        }
        return series;
    }






}
