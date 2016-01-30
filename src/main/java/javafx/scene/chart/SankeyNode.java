package javafx.scene.chart;

/**
 * @author Adrian Healey <adrian.j.healey@gmail.com>
 */
public class SankeyNode {
    private String name;
    private double value = 0.0;
    private int column = 0;

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

    int getColumn() {
        return column;
    }

    void setColumn(int column) {
        this.column = column;
    }

    void moveRight() {
        this.column++;
    }
}
