package javafx.scene.chart;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.max;
import static java.util.stream.Collectors.toSet;

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
        computeNodesValue();
        resetNodesHorizontalPosition();
        computeNodesHorizontalPositionFor(nodes);

    }

    // TODO - manage cycles
    void computeNodesHorizontalPositionFor(Collection<SankeyNode> nodes) {
        checkArgument(nodes != null, "nodes cannot be null");

        if(! nodes.isEmpty()) {
            Set<SankeyNode> targetedNodes = links.stream()
                    .filter(link -> nodes.contains(link.getSource()))
                    .map(SankeyLink::getTarget)
                    .collect(toSet());
            targetedNodes.stream()
                    .forEach(SankeyNode::moveRight);
            computeNodesHorizontalPositionFor(targetedNodes);
        }
    }

    void resetNodesHorizontalPosition() {
        nodes.stream()
                .forEach(node -> node.setColumn(0));
    }

    void computeNodesValue() {
        nodes.stream()
                .forEach(this::updateValueFor);
    }

    void updateValueFor(SankeyNode node) {
        checkArgument(node != null, "node cannot be null");

        node.setValue(
                max(
                        sumOfLinksFrom(node),
                        sumOfLinksTargeting(node)
                )
        );
    }

    double sumOfLinksTargeting(SankeyNode node) {
        return links.stream()
                .filter(link -> link.getTarget().equals(node))
                .mapToDouble(SankeyLink::getValue)
                .sum();
    }

    private double sumOfLinksFrom(SankeyNode node) {
        return links.stream()
                .filter(link -> link.getSource().equals(node))
                .mapToDouble(SankeyLink::getValue)
                .sum();
    }

}
