package gui.controllers;

import gui.LineChartWithMarkers;
import javafx.beans.InvalidationListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseButton;
import javafx.scene.text.Text;
import javafx.scene.control.TextField;
import javafx.util.Pair;
import utils.GraphGenerator;

import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class LineChartMouseController {
    // Start value for zoom
    Integer startValue;
    TextField startDateField;
    TextField stopDateField;
    NumberAxis xAxis;
    NumberAxis yAxis;
    LineChartWithMarkers lineChart;
    GraphGenerator gen;

    public LineChartMouseController(LineChartWithMarkers lineChart, NumberAxis xAxis, NumberAxis yAxis,
                                    GraphGenerator gen, TextField startDateField, TextField stopDateField){

        this.startDateField = startDateField;
        this.stopDateField = stopDateField;
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.lineChart = lineChart;
        this.gen = gen;
    }

    public void updateTextField(){
        //get a DD/MM/YYYY string of the current upper and lower bound
    }


    public int zoomInWithString() throws ParseException {
        Date firstDate = convertStringtoDate(startDateField.getText());
        Date secondDate = convertStringtoDate(stopDateField.getText());

        //ZoomInCords assign both values to be the index of the first item
        Integer firstZoom = 0;
        Integer secondZoom = 0;


        //get all dates from linechart using the graphgenerators xIndexList which contains a bunch of compareables
        ArrayList<Comparable> allDates = gen.getxIndexList();

        //find the right indexes - Iterate over the list- an find the right indexes
        //order will not matter in this case and can be input in which ever order


        for(Comparable item: allDates){
            //if the date item is greater than the current item being iterated over take it or the previous item as
            //the first zoom coordinate. Else continue
            if(item.compareTo(firstDate)>=0){
                //set as a zoom cord
               firstZoom = allDates.indexOf(item);
            }
        }
        //same for second zoom cord
        for(Comparable item: allDates){
            if(item.compareTo(secondDate)>=0){
                //set as a zoom cord
                secondZoom = allDates.indexOf(item);
            }
        }

        //check if both cord correspond to either the first or last item, i.e. The range is invalid
        if(secondZoom == firstZoom && (firstZoom == allDates.get(0) || firstZoom == allDates.get(allDates.size()-1))){
            return 1;
        }


        //call the normal zoomIn function
        if(secondZoom > firstZoom){
            lineChart.zoomIn(firstZoom,secondZoom);
        }else{
            lineChart.zoomIn(secondZoom,firstZoom);
        }

        return 0;
    }






    public Date convertStringtoDate(String string) throws ParseException {
        SimpleDateFormat formatter1=new SimpleDateFormat("dd/MM/yyyy");
        Date newDate = formatter1.parse(string);
        return newDate;
    }









    public void setMouseController() {
        final Node chartBackground = lineChart.lookup(".chart-plot-background");

        startDateField.setPromptText("dd/MM/yyyy");
        stopDateField.setPromptText("dd/MM/yyyy");


        // Get the amount of values on the xAxis
        // if the amount is over a certain limit, employ a range to check if the values
        // of data should be updated
        chartBackground.setOnMousePressed(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                int xValue = xAxis.getValueForDisplay(mouseEvent.getX()).intValue();
                startValue = xValue;

                // Get the date from the hiddenseries
                int dateArraySize = gen.getxIndexList().size();
                if (xValue < dateArraySize) {
                    XYChart.Data<Number, Number> item = new XYChart.Data<Number, Number>();
                    item.setYValue(0);
                    item.setXValue(xValue);
                }
            } else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                if (startValue != null) {
                    // Cancel zooming
                    lineChart.removeAllVeritcalZoomMarkers();
                    lineChart.removeAllRectangleMarkers();
                    startValue = null;
                } else {
                    // Zoom out
                    lineChart.removeAllVeritcalZoomMarkers();
                    xAxis.setAutoRanging(true);
                }
            }
        });

        chartBackground.setOnMouseReleased(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY && startValue != null) {
                int xValue = xAxis.getValueForDisplay(mouseEvent.getX()).intValue();
                lineChart.zoomIn(startValue, xValue);

                startValue = null;
                lineChart.removeAllRectangleMarkers();
                lineChart.removeAllVeritcalZoomMarkers();
            }
        });

        chartBackground.setOnMouseDragged(mouseEvent -> {
            // Only the primary mouse button supports dragging
            if (mouseEvent.getButton() != MouseButton.PRIMARY) {
                return;
            }

            // IF the firstZoomInterger hsa been set then create a rectangle spanning from
            // firstZoomintgercords
            // to the mouse position
            lineChart.removeAllStackPanes();
            int xValue = xAxis.getValueForDisplay(mouseEvent.getX()).intValue();

            // Check bounds (prevents selecting past end of graph)
            if (xValue >= gen.getxIndexList().size()) {
                return;
            }

            // If we are zooming in
            if (startValue != null) {
                lineChart.removeAllRectangleMarkers();
                lineChart.removeAllVeritcalZoomMarkers();
                lineChart.removeAllStackPanes();
                XYChart.Data<Number, Number> item = new XYChart.Data<>();
                if (xValue > startValue) {
                    item.setXValue(startValue);
                    item.setYValue(xValue);
                } else {
                    item.setXValue(xValue);
                    item.setYValue(startValue);
                }
                lineChart.addRectangleMarker(item);

                XYChart.Data<Number, Number> dateDiff = new XYChart.Data<>();
                dateDiff.setXValue((startValue + xValue) / 2);
                Number yPosistion = (yAxis.getUpperBound() - ((yAxis.getUpperBound() - yAxis.getLowerBound()) / 10));
                dateDiff.setYValue(yPosistion.intValue());
                lineChart.removeAllDateLabels();

                String dateDiffString = String.format("%s - %s", gen.getxIndexList().get(startValue),
                        gen.getxIndexList().get(xValue));
                lineChart.addDateLabel(dateDiff, dateDiffString);

                // ADDS A VERTICAL LINE TO THE END OF THE RECTANGLE
                lineChart.addVerticalZoomMarker(new XYChart.Data<>(startValue, 0));
                lineChart.addVerticalZoomMarker(new XYChart.Data<>(xValue, 0));
            }
            lineChart.removeAllVerticalValueMarkers();
            Number yOffSet = ((yAxis.getUpperBound() - yAxis.getLowerBound()) / 10);
            int priceLabelCount = 2;
            if (gen.hashMap.containsKey(xValue)) {
                for (var newValue : gen.hashMap.get(xValue)) {
                    Number yPosition = (yAxis.getUpperBound() - yOffSet.doubleValue() * priceLabelCount);

                    // Find the corrsponding old stock value for each new value if applicable
                    for (var oldValue : gen.hashMap.get(startValue)) {
                        var oldName = (Pair) oldValue.getExtraValue();
                        var newName = (Pair) newValue.getExtraValue();
                        if(oldName.getKey().equals(newName.getKey())){
                            priceLabelCount++;

                            // Calculate the difference in price and set a boolean depending on value
                            double priceDelta = newValue.getYValue().doubleValue() - oldValue.getYValue().doubleValue();
                            boolean priceDeltaBoolean = priceDelta > 0;

                            // Create A string to display the delta
                            double percentDiffDouble = ((newValue.getYValue().doubleValue()
                                    / oldValue.getYValue().doubleValue()) - 1) * 100;
                            String priceDeltaString = String.format("%S : %.2f%s", oldValue.getExtraValue(),
                                    percentDiffDouble, "%");

                            // Sets the xAxis position to the middle of the drawn rectangle
                            Number xPosition = (newValue.getXValue().intValue() + oldValue.getXValue().intValue()) / 2;

                            // Create StackPanes for each one,
                            XYChart.Data data = new XYChart.Data(xPosition.intValue(), yPosition.intValue());
                            lineChart.addStackPane(data, priceDeltaString, priceDeltaBoolean);
                        }
                    }
                }
            }
        });

        chartBackground.setOnMouseMoved(mouseEvent -> {
            // USE THE xAXIS VALUE TO FIND A RANDOM SERIES CORRESPONDING YVALUE LOCATION
            // Get the X cord of the mouse and round to the nearest integer value
            // Get the Y values of each series with x cord value
            // We round the mouse cords to Integer so that the line wont flicker or
            // disappear if the mouse if between
            // two value on the xAxis
            Number xMouseCord = mouseEvent.getX();
            int xValue = xAxis.getValueForDisplay(xMouseCord.intValue()).intValue();

            // Check if the current xCord has a new value in the range and update it, if it
            // different, update the items.
            lineChart.updateVerticalValueMarker(new XYChart.Data(xValue, 0));

            if (gen.hashMap.containsKey(xValue)) {
                lineChart.removeAllStackPanes();
                for (XYChart.Data<Number, Number> item : gen.hashMap.get(xValue)) {
                    String dataString = String.format("%s : %s%.2f", item.getExtraValue(), "$", item.getYValue());
                    XYChart.Data<Number, Number> xyData = new XYChart.Data<>(item.getXValue(), item.getYValue());
                    lineChart.addStackPane(xyData, dataString, true);
                }
            }
        });

        chartBackground.setOnMouseExited(mouseEvent -> {
            lineChart.removeAllVeritcalZoomMarkers();
            lineChart.removeAllVerticalValueMarkers();
            lineChart.removeAllRectangleMarkers();
            lineChart.removeAllStackPanes();
            lineChart.removeAllDateLabels();
        });
    }
}
