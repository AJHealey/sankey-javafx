package javafx.scene.chart;

import com.google.common.base.Preconditions;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.shape.CubicCurve;

import static javafx.scene.paint.Color.TRANSPARENT;
import static javafx.scene.shape.StrokeLineCap.BUTT;

/**
 * Created by Adrian Healey <adrian.j.healey@gmail.com>
 */
public class SankeyLink extends CubicCurve {
    private SankeyNode source;
    private SankeyNode target;

    private DoubleProperty value = new DoublePropertyBase() {
        @Override
        protected void invalidated() {
            if(chart.get() != null) {
                chart.get().valueHasChangedFor(SankeyLink.this);
            }
        }

        @Override
        public Object getBean() {
            return this;
        }

        @Override
        public String getName() {
            return "linkValue";
        }
    };

    public SankeyLink(SankeyNode source, SankeyNode target, Double value) {
        Preconditions.checkArgument(source != null, "source cannot be null");
        Preconditions.checkArgument(target != null, "target cannot be null");

        SankeyLink.this.source = source;
        SankeyLink.this.target = target;
        SankeyLink.this.value.setValue(value);
        setColor();
    }

    public SankeyNode getSource() {
        return source;
    }

    public SankeyNode getTarget() {
        return target;
    }

    /**
     * The chart which this data belongs to.
     */
    private ReadOnlyObjectWrapper<SankeyChart> chart = new ReadOnlyObjectWrapper<>();

    public double getValue() {
        return value.get();
    }

    public DoubleProperty valueProperty() {
        return value;
    }

    public void setValue(double value) {
        SankeyLink.this.value.set(value);
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

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + source.hashCode();
        result = 31 * result + target.hashCode();
        result = 31 * result + value.hashCode();
        result = 31 * result + chart.hashCode();
        return result;
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

    public boolean isRelatedTo(SankeyNode node) {
        return source.equals(node) || target.equals(node);
    }

    public void setChart(SankeyChart chart) {
        this.chart.setValue(chart);
    }
}
