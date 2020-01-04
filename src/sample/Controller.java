package sample;

import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import Main.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Paint;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    Button searchButton;
    @FXML
    Button fillLineChartButton;
    @FXML
    LineChart<String, Number> lineChart;
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

    private GraphDrawer graphDrawer = new GraphDrawer();
    private SearchFunction searchFunction = new SearchFunction();

    public void threadedDrawFunction(javafx.event.ActionEvent actionEvent){
        graphDrawer.restart();
    }

    public void threadedSearchFunction(javafx.event.ActionEvent actionEvent){
        searchFunction.restart();
    }

    public void clearLineChart(javafx.event.ActionEvent actionEvent) {
        lineChart.getData().clear();
        drawnTickers.clear();
        yAxis.setUpperBound(100);
        yAxis.setLowerBound(0);
    }

    public void deleteTicker(javafx.event.ActionEvent actionEvent) {
        Object objectToRemove = tickerTable.getSelectionModel().getSelectedItem();
        tickerTable.getItems().remove(objectToRemove);
    }

    public void addTicker(javafx.event.ActionEvent actionEvent) {
        searchResultObject item = leftComboBox.getSelectionModel().getSelectedItem();
        System.out.println(item.getSymbol());
        tickerTable.getItems().add(item);
    }

    public void displayTooltip(Parent parent, String message) {
        final Tooltip emptySearchWarning = new Tooltip();
        Bounds boundsInScene = parent.localToScreen(parent.getBoundsInLocal());
        emptySearchWarning.setAutoHide(true);
        emptySearchWarning.setText(message);
        emptySearchWarning.show(parent, boundsInScene.getCenterX(), boundsInScene.getCenterY());
    }


    public String getTimeSerie() {
        String intervalboxString = intervalCombobox.getSelectionModel().getSelectedItem();
        if (intervalboxString == null) {
            intervalboxString = "15min";
        }
        ArrayList<String> series = new ArrayList<>(List.of("15min", "5min", "1min", "Monthly", "Weekly", "Daily"));
        ArrayList<String> timeSeries = new ArrayList<>(List.of("TIME_SERIES_INTRADAY", "TIME_SERIES_INTRADAY", "TIME_SERIES_INTRADAY", "TIME_SERIES_MONTHLY", "TIME_SERIES_WEEKLY", "TIME_SERIES_DAILY"));
        return timeSeries.get(series.indexOf(intervalboxString));
    }


    public void reset(javafx.event.ActionEvent actionEvent) {
        //reset everything to the startup state
    }

    public void fliplegends(javafx.event.ActionEvent actionEvent){
        lineChart.setLegendVisible(!lineChart.isLegendVisible());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        SymbolSearch searchResults = new SymbolSearch();
        ObservableList<searchResultObject> observables = searchResults.getObservables();
        lineChart.setCreateSymbols(false);
        leftComboBox.setItems(observables);
        tickerTable.setPlaceholder(new Label("NO TICKERS SELECTED"));
        symbolColumn.setCellValueFactory(new PropertyValueFactory<>("symbol"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        intervalCombobox.getItems().addAll("15min", "5min", "1min", "Monthly", "Weekly", "Daily");

        //Java, sometimes it really be do like that

        lineChart.setAxisSortingPolicy(LineChart.SortingPolicy.NONE);
        yAxis.setAutoRanging(true);
        yAxis.setForceZeroInRange(false);
        xAxis.setAnimated(false);
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
                        String interval = intervalCombobox.getSelectionModel().getSelectedItem();
                        if (interval == null) {
                            interval = "15min";
                        }
                        ArrayList<String> symbolStrings = new ArrayList<String>();
                        //add all stock symbols from tableview to list
                        for (searchResultObject item : tickerTable.getItems()) {
                            if (!drawnTickers.contains(item.getSymbol())) {
                                symbolStrings.add(item.getSymbol());
                                drawnTickers.add(item.getSymbol());
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
                        XYSeriesGenerator gen = new XYSeriesGenerator();
                        gen.populateSeries(stockDataArrayList);
                        return gen.getSeries();

                    } else {
                        displayTooltip(fillLineChartButton, "Please add stonks to the list");
                        return null;
                    }
                }

                @Override
                protected void succeeded() {
                    for (XYChart.Series item : getValue()) {
                        lineChart.getData().add(item);
                    }
                }

                @Override
                protected void failed() {
                    Throwable error = getException();
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
