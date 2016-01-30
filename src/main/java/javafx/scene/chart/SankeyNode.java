package javafx.scene.chart;

/**
 * @author Adrian Healey <adrian.j.healey@gmail.com>
 */
public class SankeyNode {
    private String name;
    private double value;

    public SankeyNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    double getValue() {
        return value;
    }

    void setValue(double value) {
        this.value = value;
    }
}
