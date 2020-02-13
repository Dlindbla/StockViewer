package gui.controllers;

import gui.LineChartWithMarkers;
import gui.controllers.LineChartMouseController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.layout.Pane;
import utils.GraphGenerator;
import utils.PlottableObject;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import api.ApiSearchResult;
import api.apis.AlphaVantageApi;

import java.io.IOException;
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
    TableView<ApiSearchResult> tickerTable;
    @FXML
    ComboBox<ApiSearchResult> leftComboBox;
    @FXML
    TableColumn<ApiSearchResult, String> symbolColumn;
    @FXML
    TableColumn<ApiSearchResult, Double> priceColumn;
    @FXML
    ComboBox<String> intervalCombobox;
    @FXML
    ComboBox<String> selectedData;
    @FXML
    Tab testTab;
    @FXML
    Pane testTabPane;
    @FXML
    Button testButton;

    public void addToTestTab() throws IOException {
        Pane pane = FXMLLoader.load(getClass().getResource("views/cryptoTab.fxml"));
        pane.getStylesheets().add(getClass().getResource("AppStyle.css").toString());
        testTab.setContent(pane);
    }

    // Searchbox press enter function
    @FXML
    public void onEnter() {
        threadedSearchFunction();
    }

    // When interval is changed
    public void onIntervalChange() {
        var interval = intervalCombobox.getSelectionModel().getSelectedItem();
        selectedData.getItems().setAll(alphaVantage.getInfo().dataTypes.get(interval));

        graphDrawer.restart();
    }

    public void onDataTypeChange() {
        graphDrawer.restart();
    }

    // Initialize stock data sources
    AlphaVantageApi alphaVantage = new AlphaVantageApi();

    LineChartMouseController lineChartMouseController = new LineChartMouseController();
    GraphGenerator gen = new GraphGenerator();
    private GraphDrawer graphDrawer = new GraphDrawer();
    private SearchFunction searchFunction = new SearchFunction();

    String currentDrawnInterval = alphaVantage.getInfo().intervals.get(0);

    public void threadedSearchFunction() {
        searchFunction.restart();
    }

    public void fillLineChart(ArrayList<XYChart.Series<Number, Number>> series) {
        lineChart.fillLineChart(series);
    }

    public void queueLineChartClear() {
        Platform.runLater(() -> lineChart.getData().clear());
        gen.reset();
    }

    public void deleteTicker() {
        var objectToRemove = tickerTable.getSelectionModel().getSelectedItem();
        tickerTable.getItems().remove(objectToRemove);

        for (var item : lineChart.getSeries()) {
            if (item.getName() == objectToRemove.getSymbol()) {
                lineChart.getSeries().remove(item);
                break;
            }
        }
    }

    public void addTicker() {
        var item = leftComboBox.getSelectionModel().getSelectedItem();
        if (!tickerTable.getItems().contains(item) && item != null) {
            tickerTable.getItems().add(item);
            graphDrawer.restart();
        }
    }

    public String getDataType() {
        var selectedDataType = selectedData.getSelectionModel().getSelectedItem();
        if (selectedDataType == null) {
            selectedDataType = "Close";
        }

        return selectedDataType;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        tickerTable.setPlaceholder(new Label("NO TICKERS SELECTED"));
        symbolColumn.setCellValueFactory(new PropertyValueFactory<>("symbol"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        intervalCombobox.getItems().addAll(alphaVantage.getInfo().intervals);
        yAxis.setForceZeroInRange(false);
        lineChartMouseController.setMouseController(lineChart, xAxis, yAxis, gen);
        xAxis.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Number number) {
                // Check if the alldates has been generated
                // iterate over all the Dates and assign them
                if (!gen.getxIndexList().isEmpty() && number.intValue() < gen.getxIndexList().size()) {
                    return gen.getxIndexList().get(number.intValue()).toString();
                }
                return null;
            }

            @Override
            public Number fromString(String s) {
                return null;
            }
        });
    }

    private class GraphDrawer extends Service<ArrayList<XYChart.Series<Number, Number>>> {
        @Override
        protected Task<ArrayList<XYChart.Series<Number, Number>>> createTask() {
            return new Task<>() {
                @Override
                protected ArrayList<XYChart.Series<Number, Number>> call() throws Exception {
                    if (!tickerTable.getItems().isEmpty()) {
                        // if user doesn't set an interval time default to 15min

                        String interval = intervalCombobox.getSelectionModel().getSelectedItem();
                        if (interval == null) {
                            interval = alphaVantage.getInfo().intervals.get(0);
                        }

                        // check if the timeInterval has been changed and update it
                        if (!(currentDrawnInterval.equals(interval))) {
                            currentDrawnInterval = interval;
                            queueLineChartClear();
                        }

                        // Fetch all symbols
                        var stockDataPoints = new ArrayList<PlottableObject>();
                        for (var item : tickerTable.getItems()) {
                            stockDataPoints.add(alphaVantage.query(item.getSymbol(), interval, getDataType()));
                        }

                        queueLineChartClear();
                        gen.populateSeries(stockDataPoints);
                        return gen.getSeries();
                    } else {
                        return new ArrayList<XYChart.Series<Number, Number>>();
                    }
                }

                @Override
                protected void succeeded() {
                    ArrayList<XYChart.Series<Number, Number>> results = getValue();
                    Platform.runLater(() -> fillLineChart(results));
                }

                @Override
                protected void failed() {
                    System.out.printf("failed to query: %s\n", getException().getMessage());
                }
            };
        }
    }

    private class SearchFunction extends Service<ObservableList<ApiSearchResult>> {
        @Override
        protected Task<ObservableList<ApiSearchResult>> createTask() {
            return new Task<ObservableList<ApiSearchResult>>() {
                @Override
                protected ObservableList<ApiSearchResult> call() throws Exception {
                    String searchString = leftTextField.getText();
                    if (!(searchString.isEmpty())) {
                        var res = alphaVantage.search(searchString);
                        ObservableList<ApiSearchResult> list = FXCollections.observableArrayList();
                        list.addAll(res);

                        return list;
                    } else {
                        return null;
                    }
                }

                @Override
                protected void succeeded() {
                    leftComboBox.getItems().clear(); // clear the previous items from the box
                    leftComboBox.getItems().addAll(getValue()); // add the new once received from the task
                }

                @Override
                protected void failed() {
                    System.out.printf("failed to search: %s\n", getException().getMessage());
                }
            };
        }
    }
}
