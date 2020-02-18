package gui.controllers;

import gui.LineChartWithMarkers;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseButton;
import utils.GraphGenerator;

public class LineChartMouseController {
    // Start value for zoom
    Integer startValue;

    public void setMouseController(LineChartWithMarkers lineChart, NumberAxis xAxis, NumberAxis yAxis,
                                   GraphGenerator gen) {
        final Node chartBackground = lineChart.lookup(".chart-plot-background");

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
                        if ((String) oldValue.getExtraValue() == (String) newValue.getExtraValue()) {
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
