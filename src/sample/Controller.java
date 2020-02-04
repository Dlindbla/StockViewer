package sample;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import Main.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import org.json.simple.parser.ParseException;

import java.net.URL;
import java.util.*;

public class Controller implements Initializable {
    @FXML
    Button searchButton;
    @FXML
    Button fillLineChartButton;
    @FXML
    NumberAxis xAxis;
    @FXML
    NumberAxis yAxis;
    @FXML
    LineChartWithMarkers<Number, Number> lineChart;
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


    //Searchbox press enter function
    @FXML
    public void onEnter(){
        threadedSearchFunction();
    }

    LineChartMouseController lineChartMouseController = new LineChartMouseController();
    StockDataCache cache = new StockDataCache();
    XYSeriesGenerator gen = new XYSeriesGenerator();
    private GraphDrawer graphDrawer = new GraphDrawer();
    private SearchFunction searchFunction = new SearchFunction();

    String currentDrawnInterval = "15min";

    public void threadedDrawFunction() {
        graphDrawer.restart();
    }
    public void threadedSearchFunction() {
        searchFunction.restart();
    }

    public void fillLineChart(ArrayList<XYChart.Series> series){
        lineChart.fillLineChart(series);
    }

    public void resetZoom(){
        lineChart.removeAllVeritcalZoomMarkers();
        xAxis.setAutoRanging(true);
    }

    public void queueLineChartClear() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                lineChart.getData().clear();
            }
        });
    }

    public void deleteTicker(javafx.event.ActionEvent actionEvent) {
        Object objectToRemove = tickerTable.getSelectionModel().getSelectedItem();
        tickerTable.getItems().remove(objectToRemove);
    }

    public void addTicker(javafx.event.ActionEvent actionEvent) {
        searchResultObject item = leftComboBox.getSelectionModel().getSelectedItem();
        tickerTable.getItems().add(item);
    }

    //TODO: REPLACE THIS SO THAT THE STOCKDATA DOESNT NEED A KEY AND INSTEAD PEEKS INTO THE KEYSETS DATA TO FIND THE RIGHT KEY
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

        //TODO : Move as many lines as possible from here to new.FXML
        tickerTable.setPlaceholder(new Label("NO TICKERS SELECTED"));
        symbolColumn.setCellValueFactory(new PropertyValueFactory<>("symbol"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        intervalCombobox.getItems().addAll("15min", "5min", "1min", "Monthly", "Weekly", "Daily");
        yAxis.setForceZeroInRange(false);
        lineChartMouseController.setMouseController(new ActionEvent(),lineChart,xAxis,yAxis,gen);
        xAxis.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Number number) {
                //Check if the alldates has been generated
                //iterate over all the Dates and assign them
                if (!gen.getAllDates().isEmpty() && number.intValue() < gen.getAllDates().size()) {
                    String label = gen.getAllDates().get(number.intValue());
                    return label;
                }
                return null;
            }
            @Override
            public Number fromString(String s) {
                return null;
            }
        });
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

                        boolean redraw = false;
                        //check if the timeInterval has been changed and update it
                        if (!(currentDrawnInterval == interval)) {
                            currentDrawnInterval = interval;
                            queueLineChartClear();
                            redraw = true;

                        }

                        ArrayList<String> symbolStrings = new ArrayList<>();

                        //add all stock symbols from tableview to list, skip items already drawn

                        for(searchResultObject item : tickerTable.getItems()) {
                            symbolStrings.add(item.getSymbol());
                        }

                        //Check if the cache contains the stockdata already
                        ArrayList<StockData> stockDataArrayList = new ArrayList<>();
                        for (String item : symbolStrings) {
                            if (cache.contains(item.concat(interval))) {
                                int index = cache.indexOf(item.concat(interval));
                                stockDataArrayList.add((cache.get(index)));
                            }
                        }

                        //remove cached items from symbolStrings
                        for (StockData item : stockDataArrayList){
                            if(symbolStrings.contains(item.getStockSymbol())){
                                symbolStrings.remove(item.getStockSymbol());
                            }
                        }
                        //Set up uncached items for caching
                        ArrayList<StockData> uncachedData = new ArrayList<>();
                        if(!symbolStrings.isEmpty()) {
                            uncachedData.addAll(StockDataGenerator.getArrayList(symbolStrings, interval, getTimeSerie()));
                        }
                        //cache here-to uncached items
                        for(var item : uncachedData) {
                            cache.add(item);
                            stockDataArrayList.add(item);
                        }

                        queueLineChartClear();
                        gen.populateSeries(stockDataArrayList);
                        return gen.getSeries();
                    } else {
                        return null;
                    }
                }

                @Override
                protected void succeeded() {
                    var results = getValue();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            fillLineChart(results);
                        }
                    });
                }

                @Override
                protected void failed() {
                    Throwable error = getException();
                    System.out.println(error);

                }
            };
        }
    }

    private class SearchFunction extends Service<ObservableList<searchResultObject>> {
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
                        return null;
                    }
                }
                @Override
                protected void succeeded() {
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
