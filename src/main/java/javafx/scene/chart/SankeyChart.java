package javafx.scene.chart;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Adrian Healey <adrian.j.healey@gmail.com>
 */
public class SankeyChart extends Chart{

    private double nodeWidth = 24;
    private double nodePadding = 8;

    private Set<SankeyNode> nodes;
    private Set<SankeyLink> links;

    public SankeyChart() {
        nodes = new HashSet<SankeyNode>();
        links = new HashSet<SankeyLink>();
    }

    public Set<SankeyNode> getNodes() {
        return nodes;
    }

    public Set<SankeyLink> getLinks() {
        return links;
    }

    public double getNodePadding() {
        return nodePadding;
    }

    public void setNodePadding(double nodePadding) {
        this.nodePadding = nodePadding;
    }

    public double getNodeWidth() {
        return nodeWidth;
    }

    public void setNodeWidth(double nodeWidth) {
        this.nodeWidth = nodeWidth;
    }

    @Override
    protected void layoutChartChildren(double top, double left, double width, double height) {

    }

}
