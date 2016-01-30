package javafx.scene.chart;

/**
 * @author Adrian Healey <adrian.j.healey@gmail.com>
 */
public class SankeyLink {
    private SankeyNode source;
    private SankeyNode target;

    private double value;

    public SankeyLink(SankeyNode source, SankeyNode target, double value) {
        this.source = source;
        this.target = target;
        this.value = value;
    }

    public SankeyNode getSource() {
        return source;
    }

    public void setSource(SankeyNode source) {
        this.source = source;
    }

    public SankeyNode getTarget() {
        return target;
    }

    public void setTarget(SankeyNode target) {
        this.target = target;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
