package javafx.scene.chart;

import java.util.*;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.max;
import static java.util.Comparator.comparingDouble;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static javafx.scene.paint.Color.TRANSPARENT;
import static javafx.scene.shape.StrokeLineCap.BUTT;

/**
 * @author Adrian Healey <adrian.j.healey@gmail.com>
 */
public class SankeyChart extends Chart {

    private double nodeWidth = 24;
    private double nodePadding = 8;

    private double valueToHeigthRatio = 0.0;

    private Set<SankeyNode> nodes;
    private Set<SankeyLink> links;

    public SankeyChart() {
        nodes = new HashSet<>();
        links = new HashSet<>();
    }

    public Set<SankeyNode> getNodes() {
        return nodes;
    }

    public void addNodes(Collection<SankeyNode> nodes) {
        nodes.stream()
                .forEach(this::addNode);
    }

    public void addNode(SankeyNode newNode) {
        this.nodes.add(newNode);
        newNode.setChart(this);
        this.getChartChildren().add(newNode);
    }

    public void addLink(SankeyLink newLink) {
        this.links.add(newLink);
    }

    @Override
    protected void layoutChartChildren(double top, double left, double width, double height) {
        // Nodes
        computeNodesValue();
        resetNodesHorizontalPosition();
        computeNodesHorizontalPosition();
        computeNodesVerticalPosition();
        computeNodesCoordinates(top, left, width, height);
        // Links
        computeLinksStartCoordinates();
        computeLinksEndCoordinates();
        computeLinksControlsPoints();
        setLinksColors();
    }

    private void computeLinksControlsPoints() {
        links.stream()
                .forEach(this::computeControlPointsFor);
    }

    private void computeControlPointsFor(SankeyLink link) {
        link.setControlY1(link.getStartY());
        link.setControlY2(link.getEndY());
        double interDistance = (link.getEndX() - link.getStartX()) / 3;
        link.setControlX1(link.getStartX() + interDistance);
        link.setControlX2(link.getStartX() + 2 * interDistance);
    }

    private void setLinksColors() {
        links.stream()
                .forEach(link -> link.setStroke(link.getSource().getFill()));
        links.stream()
                .forEach(link -> link.setOpacity(0.3));
        links.stream()
                .forEach(link -> link.setFill(TRANSPARENT));
        links.stream()
                .forEach(link -> link.setStrokeLineCap(BUTT));
        links.stream()
                .forEach(link -> link.setStrokeWidth(link.getValue() * valueToHeigthRatio));
    }

    void computeLinksStartCoordinates() {
        nodes.stream()
                .forEach(this::computeLinksCoordinatesOutgoingFrom);
    }

    private void computeLinksCoordinatesOutgoingFrom(SankeyNode node) {
        List<SankeyLink> outgoingLinks = links.stream()
                .filter(link -> link.getSource().equals(node))
                .sorted(comparingDouble(link -> link.getTarget().getY()))
                .collect(toList());

        double currentY = node.getY();
        for(SankeyLink link : outgoingLinks) {
            link.setStartY(currentY + link.getValue() * valueToHeigthRatio / 2);
            link.setStartX(node.getX() + node.getWidth());
            currentY += link.getValue() * valueToHeigthRatio;
        }
    }

    private void computeLinksEndCoordinates() {
        nodes.stream()
                .forEach(this::computeLinksCoordinatesIncomingTo);
    }

    private void computeLinksCoordinatesIncomingTo(SankeyNode node) {
        List<SankeyLink> incomingNodes = links.stream()
                .filter(link -> link.getTarget().equals(node))
                .sorted(comparingDouble(link -> link.getSource().getX()))
                .collect(toList());

        double currentY = node.getY();
        for (SankeyLink link : incomingNodes) {
            link.setEndY(currentY + link.getValue() * valueToHeigthRatio / 2);
            link.setEndX(node.getX());
            currentY += link.getValue() * valueToHeigthRatio;
        }
    }

    /**
     * Compute the coordinates for all the nodes in this graph.
     *
     * It will position all the nodes without collision and
     * with a best effort to use the totality of the height and width of the
     * frame.
     *
     * @param top x coordinate of the top-left corner
     * @param left y coordinate of the top-left corner
     * @param width of the frame
     * @param height of the frame
     */
    void computeNodesCoordinates(double top, double left, double width, double height) {
        computeNodeValueToHeightRatio(height);
        // define nodes height
        nodes.stream()
                .forEach(node -> node.setHeight(node.getValue() * this.valueToHeigthRatio));
        // define nodes width
        nodes.stream()
                .forEach(node -> node.setWidth(this.nodeWidth));
        // define nodes x coordinate
        double xNodesPadding = computeNodesHorizontalPadding(width);
        nodes.stream()
                .forEach(node -> node.setX(left + node.getHorizontalPosition() * xNodesPadding));
        // define nodes y coordinate
        computeNodesYCoordinate(top);
    }

    /**
     * Compute the Y coordinate of the nodes.
     *
     * @param top y coordinate of the top-left corner of the frame
     */
    private void computeNodesYCoordinate(double top) {
        nodes.stream()
                .mapToInt(SankeyNode::getHorizontalPosition)
                .distinct()
                .forEach(column -> computeYCoordinateForNodesInColumn(column, top));
    }

    /**
     * Compute the y coordinate for the nodes in given column
     *
     * @param column the column of the nodes in which the y coordinate
     *               will be computed
     * @param top y coordinate of the top-left corner of the frame
     */
    private void computeYCoordinateForNodesInColumn(int column, double top) {
        List<SankeyNode> nodesInColumn = nodes.stream()
                .filter(node -> node.getHorizontalPosition() == column)
                .sorted((o1, o2) -> o1.getValue().compareTo(o2.getValue()))
                .collect(toList());

        double currentY = top;
        for(SankeyNode node : nodesInColumn) {
            node.setY(currentY);
            currentY += node.getHeight() + nodePadding;
        }
    }

    /**
     * Compute the horizontal padding between the nodes to
     * arrange them along all the frame's width
     *
     * @param width of the frame
     * @return the horizontal padding
     */
    private double computeNodesHorizontalPadding(double width) {
        long numberOfColumn = nodes.stream()
                .mapToInt(SankeyNode::getHorizontalPosition)
                .distinct()
                .count();

        return numberOfColumn > 1 ? width / (numberOfColumn - 1) : 0.0;
    }

    /**
     * Compute the ratio between the value of a node and its
     * height. The main constraint is that each column has to
     * be displayed completely in the frame.
     *
     * @param height height of the frame
     */
    private void computeNodeValueToHeightRatio(double height) {
        OptionalDouble totalValueOfTheBiggestColumn = nodes.stream()
                .mapToInt(SankeyNode::getHorizontalPosition)
                .distinct()
                .mapToDouble(this::computeTotalValueForColumn)
                .max();

        this.valueToHeigthRatio = totalValueOfTheBiggestColumn.isPresent()  ?
                height/totalValueOfTheBiggestColumn.getAsDouble() :
                0.0;
    }

    /**
     * Compute the total value of a node column
     *
     * @param column column to sum
     * @return the total of the given column
     */
    private double computeTotalValueForColumn(int column) {
        return nodes.stream()
                .filter(nodes -> nodes.getHorizontalPosition() == column)
                .mapToDouble(SankeyNode::getValue)
                .sum();
    }

    /**
     * Compute the vertical position of each node.
     * Each nodes will receive an unique index per column.
     * For any column, each contained node will receive an index
     * between 0 and n-1 where n is the number of nodes in that
     * column.
     */
    void computeNodesVerticalPosition() {
        nodes.stream()
                .mapToInt(SankeyNode::getHorizontalPosition)
                .distinct()
                .forEach(this::computeVerticalPositionForNodesInColumn);
    }


    /**
     * Give a unique index between 0 and n with n the number of nodes
     * in the given column for each nodes.
     *
     * @param column column to sort
     */
    private void computeVerticalPositionForNodesInColumn(int column) {
        List<SankeyNode> orderedNodes = nodes.stream()
                .filter(node -> node.getHorizontalPosition() == column)
                .sorted((o1, o2) -> o1.getValue().compareTo(o2.getValue()))
                .collect(toList());
        IntStream.range(0, orderedNodes.size())
                .forEach(i -> orderedNodes.get(i).setVerticalPosition(i));
    }

    /**
     * Put each node in a column and try to limit
     * the collision of the link.
     */
    void computeNodesHorizontalPosition() {
        Set<SankeyNode> frontier = this.nodes;

        while(!frontier.isEmpty()) {
            Set<SankeyNode> newFrontier = new HashSet<>();
            for(SankeyNode node : frontier) {
                Set<SankeyNode> incomingNodes = incomingNodesOf(node).stream()
                        .filter(frontier::contains)
                        .filter(incomingNode -> incomingNode.getHorizontalPosition() <= node.getHorizontalPosition())
                        .collect(toSet());
                if(!incomingNodes.isEmpty()) {
                    node.moveToRight();
                    newFrontier.add(node);
                }
            }
            frontier = newFrontier;
        }
    }

    /**
     * Reset the horizontal position of each node to 0.
     */
    void resetNodesHorizontalPosition() {
        nodes.stream()
                .forEach(node -> node.setHorizontalPosition(0));
    }

    /**
     * Compute the value of each node.
     */
    void computeNodesValue() {
        nodes.stream()
                .forEach(this::updateValueFor);
    }

    /**
     * Compute the value of the given node by taking the maximum
     * between the total value of incoming links and the total
     * value of outgoing links.
     *
     * @param node node of which the value will be computed
     */
    void updateValueFor(SankeyNode node) {
        checkArgument(node != null, "node cannot be null");

        node.setValue(
                max(sumOfLinksFrom(node), sumOfLinksTargeting(node)));
    }

    /**
     * Compute the sum of the values of the links targeting the given
     * node.
     *
     * @param node targeted node.
     * @return the sum of the value of the links targeting the node
     */
    double sumOfLinksTargeting(SankeyNode node) {
        return links.stream()
                .filter(link -> link.getTarget().equals(node))
                .mapToDouble(SankeyLink::getValue)
                .sum();
    }

    /**
     * Compute the sum of the values of the links coming from the given
     * node.
     *
     * @param node targeted node.
     * @return the sum of the value of the links coming from the node
     */
    double sumOfLinksFrom(SankeyNode node) {
        return links.stream()
                .filter(link -> link.getSource().equals(node))
                .mapToDouble(SankeyLink::getValue)
                .sum();
    }

    /**
     * Compute the list of nodes targeting the given node
     *
     * @param node the targeted node
     * @return the list of nodes targeting {@code node}
     */
    Collection<SankeyNode> incomingNodesOf(SankeyNode node) {
        return links.stream()
                .filter(link -> link.getTarget().equals(node))
                .map(SankeyLink::getSource)
                .collect(toList());
    }

    /**
     * Compute the list of nodes targeted by the given node
     *
     * @param node the source node
     * @return the list of nodes targeted by {@code node}
     */
    Collection<SankeyNode> outgoingNodesOf(SankeyNode node) {
        return links.stream()
                .filter(link -> link.getSource().equals(node))
                .map(SankeyLink::getTarget)
                .collect(toList());
    }

    // Events
    protected void valueHasChangedFor(SankeyLink sankeyLink) {
    }

    protected void valueHasChangedFor(SankeyNode sankeyNode) {
    }


    protected void nameHasChangedFor(SankeyNode sankeyNode) {
    }
}
