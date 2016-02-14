package javafx.scene.chart;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.StringProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;

import static javafx.scene.Cursor.OPEN_HAND;

/**
 * Created by Adrian Healey <adrian.j.healey@gmail.com>
 */
public class SankeyNode extends Rectangle {

    private StringProperty name = new StringPropertyBase() {
        @Override
        protected void invalidated() {
            if(chart.get() != null) {
                chart.get().nameHasChangedFor(SankeyNode.this);
            }
        }

        @Override
        public Object getBean() {
            return SankeyNode.this;
        }

        @Override
        public String getName() {
            return "nodeName";
        }
    };

    private Double value;

    private int horizontalPosition;

    private int verticalPosition;

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        SankeyNode.this.value = value;
    }

    public int getHorizontalPosition() {
        return horizontalPosition;
    }

    protected void setHorizontalPosition(int horizontalPosition) {
        this.horizontalPosition = horizontalPosition;
    }

    public int getVerticalPosition() {
        return verticalPosition;
    }

    protected void setVerticalPosition(int verticalPosition) {
        this.verticalPosition = verticalPosition;
    }

    /**
     * The chart which this data belongs to.
     */
    private ReadOnlyObjectWrapper<SankeyChart> chart = new ReadOnlyObjectWrapper<>(null, "chart");

    public void setChart(SankeyChart chart) {
        this.chart.set(chart);
    }

    public SankeyNode(String name) {
        super(0, 0, 0, 0);
        this.name.setValue(name);
        this.value = 0.0;
        this.horizontalPosition = 0;
        this.verticalPosition = 0;
        this.setCursor(OPEN_HAND);

        // Event handler
        this.setOnMousePressed(nodeOnMousePressedEventHandler);
        this.setOnMouseDragged(nodeOnMouseDraggedEventHandler);
    }

    double originalX;
    double originalY;
    EventHandler<MouseEvent> nodeOnMousePressedEventHandler =
            t -> {
                originalX = t.getSceneX();
                originalY = t.getSceneY();
            };

    EventHandler<MouseEvent> nodeOnMouseDraggedEventHandler =
            t -> {
                synchronized (this) {
                    this.setX(this.getX() + t.getSceneX() - originalX);
                    this.setY(this.getY() + t.getSceneY() - originalY);
                    originalX = t.getSceneX();
                    originalY = t.getSceneY();
                    chart.get().positionHasChangedFor(this);
                }
            };

    protected void moveToRight() {
        horizontalPosition ++;
    }
}
