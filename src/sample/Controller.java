package sample;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import Main.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Paint;
import javafx.util.StringConverter;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Controller implements Initializable {
    @FXML
    Button searchButton;
    @FXML
    Button fillLineChartButton;
    @FXML
    LineChart<String, Long> lineChart;
    @FXML
    CategoryAxis xAxis;
    @FXML
    NumberAxis yAxis;
    @FXML
    TextField leftTextField;
    @FXML
    TableView<searchResultObject> tickerTable;
    @FXML
    ComboBox<searchResultObject> leftComboBox;
    @FXML
    TableColumn<searchResultObject, String> symbolColumn;
    @FXML
    TableColumn<searchResultObject, Double> priceColumn;
    @FXML
    ComboBox<String> intervalCombobox;
    @FXML
    Button flipbutton;


    ArrayList<String> drawnTickers = new ArrayList<>();
    XYSeriesGenerator gen = new XYSeriesGenerator();
    private GraphDrawer graphDrawer = new GraphDrawer();
    private SearchFunction searchFunction = new SearchFunction();

    String currentDrawnInterval = "15min";

    public void threadedDrawFunction(javafx.event.ActionEvent actionEvent){
        graphDrawer.restart();
    }

    public void threadedSearchFunction(javafx.event.ActionEvent actionEvent){
        searchFunction.restart();
    }

    public void hideHiddenSeriesLegend() {
        lineChart.getData().get(0).getNode().setVisible(false);
        Set<Node> legends = lineChart.lookupAll("Label.chart-legend-item");
        legends.stream().findFirst().get().setVisible(!legends.stream().findFirst().get().isVisible());
    }


    public void runLaterfillLineChart(ArrayList<XYChart.Series> series){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                fillLineChart(series);
            }
        });
    }

    public void fillLineChart(ArrayList<XYChart.Series> series){
        System.out.println("ADDING ITEMS TO LINECHART NOW!");

        boolean firstpass = true;
        for (XYChart.Series item : series) {
            //Attempts to update the hiddenSeries
            if(firstpass){
                try {
                    System.out.println("UPDATING HIDDENSERIES");
                    //always update the hidden series
                    lineChart.getData().set(0, item);
                    firstpass = false;
                    continue;
                }catch (IndexOutOfBoundsException e){firstpass=false;}
            }
            if (!drawnTickers.contains(item.getName())) {
                System.out.println("adding item : "+ item.getName());
                lineChart.getData().add(item);
                drawnTickers.add(item.getName());

            }
        }
        hideHiddenSeriesLegend();
        System.out.println("ITEMS ADDED TO LINECHART");
    }

    public void testbutton(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                clearLineChart();
            }
        });
    }

    public void clearLineChart() {
        System.out.println("Clearing LineChart NOW");
        lineChart.getData().clear();
        //gen.reset();
        drawnTickers.clear();
        yAxis.setUpperBound(100);
        yAxis.setLowerBound(0);
        System.out.println("LINECHART CLEARED");
    }

    public void deleteTicker(javafx.event.ActionEvent actionEvent) {
        Object objectToRemove = tickerTable.getSelectionModel().getSelectedItem();
        tickerTable.getItems().remove(objectToRemove);
    }

    //Removes a series from the linechart
    public void undrawTicker(String tickerName){
        for(XYChart.Series series: lineChart.getData()){
            if(series.getName()==tickerName){
                lineChart.getData().remove(series);
            }
        }
    }


    public void addTicker(javafx.event.ActionEvent actionEvent) {
        searchResultObject item = leftComboBox.getSelectionModel().getSelectedItem();
        System.out.println(item.getSymbol());
        tickerTable.getItems().add(item);
    }

    //Display a tooltip at the parent node of the caller
    public void displayTooltip(Parent parent, String message) {
        final Tooltip emptySearchWarning = new Tooltip();
        Bounds boundsInScene = parent.localToScreen(parent.getBoundsInLocal());
        emptySearchWarning.setAutoHide(true);
        emptySearchWarning.setText(message);
        emptySearchWarning.show(parent, boundsInScene.getMaxX(), boundsInScene.getMaxY());
    }

    //Find the right time interval key for the URLBuilder and JSONParser//Stockdata
    public String getTimeSerie() {
        String intervalboxString = intervalCombobox.getSelectionModel().getSelectedItem();
        if (intervalboxString == null) {
            intervalboxString = "15min";
        }
        ArrayList<String> series = new ArrayList<>(List.of("15min", "5min", "1min", "Monthly", "Weekly", "Daily"));
        ArrayList<String> timeSeries = new ArrayList<>(List.of("TIME_SERIES_INTRADAY", "TIME_SERIES_INTRADAY", "TIME_SERIES_INTRADAY", "TIME_SERIES_MONTHLY", "TIME_SERIES_WEEKLY", "TIME_SERIES_DAILY"));
        return timeSeries.get(series.indexOf(intervalboxString));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //Start up values
        //TODO : Move as many lines as possible from here to new.FXML
        SymbolSearch searchResults = new SymbolSearch();
        ObservableList<searchResultObject> observables = searchResults.getObservables();
        lineChart.setCreateSymbols(false);
        leftComboBox.setItems(observables);
        tickerTable.setPlaceholder(new Label("NO TICKERS SELECTED"));
        symbolColumn.setCellValueFactory(new PropertyValueFactory<>("symbol"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        intervalCombobox.getItems().addAll("15min", "5min", "1min", "Monthly", "Weekly", "Daily");
        xAxis.setAnimated(false);
        lineChart.setAnimated(false);
        yAxis.setAutoRanging(true);
        yAxis.setForceZeroInRange(false);
        lineChart.layout();

    }

    private class GraphDrawer extends Service<ArrayList<XYChart.Series>> {

        @Override
        protected Task<ArrayList<XYChart.Series>> createTask() {
            return new Task<ArrayList<XYChart.Series>>() {
                @Override
                protected ArrayList<XYChart.Series> call() throws Exception {
                    if (!tickerTable.getItems().isEmpty()) {
                        //if user doesn't set an interval time default to 15min
                        //TODO: possibly move this to another method or make one
                        //TODO: Create method do detect if the time interval has been changed
                        String interval = intervalCombobox.getSelectionModel().getSelectedItem();
                        if (interval == null) {
                            interval = "15min";
                        }

                        //check if the timeInterval has been changed and update it
                        if(!(currentDrawnInterval == interval)){
                            currentDrawnInterval = interval;
                            testbutton();
                            //drawnTickers is also cleared in testbutton() but needs to be done here due to threading
                            drawnTickers.clear();
                        }

                        ArrayList<String> symbolStrings = new ArrayList<String>();
                        //add all stock symbols from tableview to list, skip items already drawn
                        for (searchResultObject item : tickerTable.getItems()) {
                            if (!drawnTickers.contains(item.getSymbol())) {
                                symbolStrings.add(item.getSymbol());
                            }
                        }

                        ArrayList<StockData> stockDataArrayList = new ArrayList<StockData>();
                        for (String symbol : symbolStrings) {
                            String timeSeries = getTimeSerie();
                            String url = URLBuilder.queryString(timeSeries, symbol, interval, true);
                            System.out.println(url);
                            //GET JSON object
                            JSONObject jsonData = GetJSONData.getJsonFromUrl(url);
                            //Parse to StockData object
                            StockData stockDataObject = new StockData(jsonData, symbol, interval);
                            //Feed into the Generator to create a XYChart.Series object
                            stockDataArrayList.add(stockDataObject);
                        }

                        gen.populateSeries(stockDataArrayList);
                        return gen.getSeries();

                    } else {
                        displayTooltip(fillLineChartButton, "Please add stonks to the list");
                        return null;
                    }
                }

                @Override
                protected void succeeded() {
                    var results = getValue();
                    runLaterfillLineChart(results);
                }

                @Override
                protected void failed() {
                    System.out.println("Big error");
                    Throwable error = getException();
                    System.out.println(error);
                }
            };
        }
    }


    private class SearchFunction extends Service<ObservableList<searchResultObject>>{
        @Override
        protected Task<ObservableList<searchResultObject>> createTask() {
            return new Task<ObservableList<searchResultObject>>() {
                @Override
                protected ObservableList<searchResultObject> call() throws Exception {
                    String searchString = leftTextField.getText();
                    if (!(searchString.isEmpty())) {
                        SymbolSearch searchResults = new SymbolSearch();
                        searchResults.search(searchString);
                        ObservableList<searchResultObject> observables = searchResults.getObservables();
                        return observables;
                    } else {
                        displayTooltip(searchButton, "Please input a search string!");
                        return null;
                    }
                }
                @Override
                protected void succeeded(){
                    leftComboBox.getItems().clear(); // clear the previous items from the box
                    leftComboBox.getItems().addAll(getValue()); //add the new once received from the task
                }
                @Override
                protected void failed() {
                    Throwable error = getException();
                }
            };
        }
    }
}
