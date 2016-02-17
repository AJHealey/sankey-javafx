package javafx.scene.chart;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.shape.CubicCurve;

import static com.google.common.base.Preconditions.checkArgument;
import static javafx.scene.paint.Color.TRANSPARENT;
import static javafx.scene.shape.StrokeLineCap.BUTT;

/**
 * Created by Adrian Healey <adrian.j.healey@gmail.com>
 */
public class SankeyLink extends CubicCurve {
    private SankeyNode source;
    private SankeyNode target;

    private DoubleProperty value;

    public SankeyLink(SankeyNode source, SankeyNode target, DoubleProperty value) {
        checkArgument(source != null, "source cannot be null");
        checkArgument(target != null, "target cannot be null");

        this.source = source;
        this.target = target;
        this.value = value;
        this.value.addListener((observable, oldValue, newValue) -> {
            SankeyLink.this.getChart().valueHasChangedFor(SankeyLink.this);
        });

        setColor();
    }

    public double getValue() {
        return value.get();
    }

    public void setValue(double value) {
        this.value.set(value);
    }

    public DoubleProperty valueProperty() {
        return value;
    }

    public SankeyNode getSource() {
        return source;
    }

    public SankeyNode getTarget() {
        return target;
    }

    /**
     * The color of the link is the color of its source node
     */
    private void setColor() {
        this.setStroke(source.getFill());
        this.setOpacity(0.3);
        this.setFill(TRANSPARENT);
        this.setStrokeLineCap(BUTT);
    }

    public void setDarkerColor() {
        this.setOpacity(0.5);
    }

    public void setNormalColor() {
        this.setOpacity(0.3);
    }

    /**
     * The chart which this data belongs to.
     */
    private ReadOnlyObjectWrapper<SankeyChart> chart = new ReadOnlyObjectWrapper<>();

    public void setChart(SankeyChart chart) {
        this.chart.setValue(chart);
    }

    public SankeyChart getChart() {
        return chart.get();
    }

    public ReadOnlyObjectWrapper<SankeyChart> chartProperty() {
        return chart;
    }

    public boolean isRelatedTo(SankeyNode node) {
        return source.equals(node) || target.equals(node);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + source.hashCode();
        result = 31 * result + target.hashCode();
        result = 31 * result + value.hashCode();
        result = 31 * result + chart.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SankeyLink that = (SankeyLink) o;

        if (!source.equals(that.source)) return false;
        if (!target.equals(that.target)) return false;
        if (!value.equals(that.value)) return false;
        return chart.equals(that.chart);

    }
}
