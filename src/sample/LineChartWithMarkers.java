package sample;

import javafx.beans.InvalidationListener;
import javafx.beans.NamedArg;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.util.Objects;

/*
* THIS CLASS WAS COPIED FROM : https://stackoverflow.com/questions/28952133/how-to-add-two-vertical-lines-with-javafx-linechart
* This class allows the linechart to display both vertical and horizontal lines.
* */

public class LineChartWithMarkers<X,Y> extends LineChart {

    public ObservableList<Data<X, Y>> horizontalMarkers;
    public ObservableList<Data<X, Y>> verticalMarkers;
    public ObservableList<Data<X, Y>> verticalZoomMarkers;

    public ObservableList<Data <X, X>> rectangleMarkers;

    public LineChartWithMarkers(@NamedArg("xAxis") Axis<X> xAxis, @NamedArg("yAxis") Axis<Y> yAxis) {
        super(xAxis, yAxis);

        horizontalMarkers = FXCollections.observableArrayList(data -> new Observable[] {data.YValueProperty()});
        horizontalMarkers.addListener((InvalidationListener) observable -> layoutPlotChildren());

        verticalMarkers = FXCollections.observableArrayList(data -> new Observable[] {data.XValueProperty()});
        verticalMarkers.addListener((InvalidationListener)observable -> layoutPlotChildren());

        verticalZoomMarkers = FXCollections.observableArrayList(data -> new Observable[] {data.XValueProperty()});
        verticalZoomMarkers.addListener((InvalidationListener)observable -> layoutPlotChildren());

        rectangleMarkers = FXCollections.observableArrayList(data -> new Observable[] {data.XValueProperty()});
        rectangleMarkers.addListener((InvalidationListener)observable -> layoutPlotChildren());
    }

    public void addHorizontalValueMarker(Data<X, Y> marker) {
        Objects.requireNonNull(marker, "the marker must not be null");
        if (horizontalMarkers.contains(marker)) return;
        Line line = new Line();
        marker.setNode(line );
        getPlotChildren().add(line);
        horizontalMarkers.add(marker);
    }

    public void removeHorizontalValueMarker(Data<X, Y> marker) {
        Objects.requireNonNull(marker, "the marker must not be null");
        if (marker.getNode() != null) {
            getPlotChildren().remove(marker.getNode());
            marker.setNode(null);
        }
        horizontalMarkers.remove(marker);
    }

    public void addVerticalValueMarker(Data<X, Y> marker) {
        Objects.requireNonNull(marker, "the marker must not be null");
        if (verticalMarkers.contains(marker)) return;
        Line line = new Line();
        marker.setNode(line);
        getPlotChildren().add(line);
        verticalMarkers.add(marker);
    }

    public void addVerticalZoomMarker(Data<X, Y> marker) {
        Objects.requireNonNull(marker, "the marker must not be null");
        if (verticalZoomMarkers.contains(marker)) return;
        Line line = new Line();
        line.setStyle("-fx-stroke: green; -fx-opacity: 0.4");
        marker.setNode(line);
        getPlotChildren().add(line);
        verticalZoomMarkers.add(marker);
    }

    public void addRectangleMarker(Data<X, X> marker){
        Objects.requireNonNull(marker, "the marker must not be null");
        if (rectangleMarkers.contains(marker)) return;
        Rectangle rectangle = new Rectangle(0,0,0,0);
        rectangle.setStroke(Color.TRANSPARENT);
        rectangle.setFill(Color.GREEN.deriveColor(1, 1, 1, 0.2));
        marker.setNode(rectangle);
        marker.getNode().setMouseTransparent(true);
        getPlotChildren().add(rectangle);
        rectangleMarkers.add(marker);
    }




    public void removeVerticalValueMarker(Data<X, Y> marker) {
        Objects.requireNonNull(marker, "the marker must not be null");
        if (marker.getNode() != null) {
            getPlotChildren().remove(marker.getNode());
            marker.setNode(null);
        }
        verticalMarkers.remove(marker);
    }

    public void removeAllVerticalValueMarkers(){
        for(Data<X, Y> marker: verticalMarkers){
            getPlotChildren().remove(marker.getNode());
            marker.setNode(null);
        }
        verticalMarkers.clear();
    }

    public void removeAllVeritcalZoomMarkers(){
        for(Data<X, Y> marker: verticalZoomMarkers){
            getPlotChildren().remove(marker.getNode());
            marker.setNode(null);
        }
        verticalZoomMarkers.clear();
    }

    public void removeAllRectangleMarkers(){
        for(Data<X, X> marker : rectangleMarkers){
            getPlotChildren().remove(marker.getNode());
            marker.setNode(null);
        }
        rectangleMarkers.clear();
    }





    //Returns only the LineCharts XYCHART.SERIES objects and not MARKERS
    public ObservableList<XYChart.Series> getSeries(){
        return super.getData();
    }

    @Override
    protected void layoutPlotChildren() {
        super.layoutPlotChildren();
        for (Data<X, Y> horizontalMarker : horizontalMarkers) {
            Line line = (Line) horizontalMarker.getNode();
            line.setStartX(0);
            line.setEndX(getBoundsInLocal().getWidth());
            line.setStartY(getYAxis().getDisplayPosition(horizontalMarker.getYValue()) + 0.5); // 0.5 for crispness
            line.setEndY(line.getStartY());
            line.toFront();
        }
        for (Data<X, Y> verticalMarker : verticalMarkers) {
            Line line = (Line) verticalMarker.getNode();
            line.setStartX(getXAxis().getDisplayPosition(verticalMarker.getXValue()) + 0.5);  // 0.5 for crispness
            line.setEndX(line.getStartX());
            line.setStartY(0d);
            line.setEndY(getBoundsInLocal().getHeight());
            line.toFront();
        }
        for (Data<X, Y> verticalMarker : verticalZoomMarkers) {
            Line line = (Line) verticalMarker.getNode();
            line.setStartX(getXAxis().getDisplayPosition(verticalMarker.getXValue()) + 0.5);  // 0.5 for crispness
            line.setEndX(line.getStartX());
            line.setStartY(0d);
            line.setEndY(getBoundsInLocal().getHeight());
            line.toFront();
        }

        for (Data<X, X> rectangleMarker : rectangleMarkers) {

            Rectangle rectangle = (Rectangle) rectangleMarker.getNode();
            rectangle.setX( getXAxis().getDisplayPosition(rectangleMarker.getXValue()) + 0.5);  // 0.5 for crispness
            rectangle.setWidth( getXAxis().getDisplayPosition(rectangleMarker.getYValue()) - getXAxis().getDisplayPosition(rectangleMarker.getXValue()));
            rectangle.setY(0d);
            rectangle.setHeight(getBoundsInLocal().getHeight());
            rectangle.toBack();

        }


    }

}