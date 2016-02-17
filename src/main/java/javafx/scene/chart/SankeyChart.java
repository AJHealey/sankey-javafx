package javafx.scene.chart;

import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

import java.util.*;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.max;
import static java.util.Comparator.comparingDouble;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static javafx.collections.FXCollections.emptyObservableSet;

/**
 * @author Adrian Healey <adrian.j.healey@gmail.com>
 */
public class SankeyChart extends Chart {

    private double nodeWidth = 24;
    private double nodePadding = 8;

    private double valueToHeightRatio = 0.0;

    private ObservableSet<SankeyNode> nodes;
    private ObservableSet<SankeyLink> links;
    private Set<SankeyNode> newNodes = new HashSet<>();

    public SankeyChart() {
        this(emptyObservableSet(), emptyObservableSet());
    }

    public SankeyChart(ObservableSet<SankeyNode> nodes,
                       ObservableSet<SankeyLink> links) {
        this.nodes = nodes;
        this.nodes.addListener(nodesChangeListener);
        this.newNodes.addAll(nodes);
        this.nodes.stream()
                .forEach(node -> node.setChart(this));

        this.links = links;
        this.links.addListener(linksChangeListener);
        this.links.stream()
                .forEach(link -> link.setChart(this));

        getChartChildren().addAll(links);
        getChartChildren().addAll(nodes);
    }

    @Override
    protected void layoutChartChildren(double top, double left, double width, double height) {
        computeNodesValue();
        computeNodesHorizontalPosition();
        computeNodesVerticalPosition();
        computeValueToHeightRatio(height);
        computeCoordinatesForNewNodes(top, left, width, height);
        // Links layout
        computeLinksStartCoordinates();
        computeLinksEndCoordinates();
        computeLinksControlsPoints();
        computeLinksStroke();
    }

    private void computeLinksStroke() {
        links.stream()
                .forEach(link -> link.setStrokeWidth(link.getValue() * valueToHeightRatio));
    }

    /**
     * Compute the control points of all the curves
     */
    private void computeLinksControlsPoints() {
        links.stream()
                .forEach(this::computeControlPointsFor);
    }

    /**
     * Compute control points of the curve representing the given
     * link.
     *
     * @param link the link for which the control points will be
     *             computed
     */
    private void computeControlPointsFor(SankeyLink link) {
        double interDistance = (link.getEndX() - link.getStartX()) / 3;
        link.setControlX1(link.getStartX() + interDistance);
        link.setControlY1(link.getStartY());
        link.setControlX2(link.getStartX() + 2 * interDistance);
        link.setControlY2(link.getEndY());
    }

    /**
     * Compute start coordinates for all nodes
     */
    void computeLinksStartCoordinates() {
        nodes.stream()
                .forEach(this::computeCoordinatesForLinksOutgoingFrom);
    }

    private void computeCoordinatesForLinksOutgoingFrom(SankeyNode node) {
        List<SankeyLink> outgoingLinks = links.stream()
                .filter(link -> link.getSource().equals(node))
                .sorted(comparingDouble(link -> link.getTarget().getY()))
                .collect(toList());

        double currentY = node.getY();
        for(SankeyLink link : outgoingLinks) {
            link.setStartY(currentY + link.getValue() * valueToHeightRatio / 2);
            link.setStartX(node.getX() + node.getWidth());
            currentY += link.getValue() * valueToHeightRatio;
        }
    }

    /**
     * Compute end coordinates for all nodes
     */
    private void computeLinksEndCoordinates() {
        nodes.stream()
                .forEach(this::computeCoordinatesForLinksIncomingTo);
    }

    private void computeCoordinatesForLinksIncomingTo(SankeyNode node) {
        List<SankeyLink> incomingNodes = links.stream()
                .filter(link -> link.getTarget().equals(node))
                .sorted(comparingDouble(link -> link.getSource().getY()))
                .collect(toList());

        double currentY = node.getY();
        for (SankeyLink link : incomingNodes) {
            link.setEndY(currentY + link.getValue() * valueToHeightRatio / 2);
            link.setEndX(node.getX());
            currentY += link.getValue() * valueToHeightRatio;
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
    void computeCoordinatesForNewNodes(double top, double left, double width, double height) {
        // define nodes height
        nodes.stream()
                .forEach(node -> node.setHeight(node.getValue() * this.valueToHeightRatio));
        // define nodes width
        nodes.stream()
                .forEach(node -> node.setWidth(this.nodeWidth));
        // define nodes x coordinate
        double xNodesPadding = computeNodesHorizontalPadding(width);
        newNodes.stream()
                .forEach(node -> node.setX(left + node.getHorizontalPosition() * xNodesPadding));
        // define nodes y coordinate
        computeNodesYCoordinate(top);
        newNodes.clear();
    }

    /**
     * Compute the Y coordinate of the nodes.
     *
     * @param top y coordinate of the top-left corner of the frame
     */
    private void computeNodesYCoordinate(double top) {
        newNodes.stream()
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

        return numberOfColumn > 1 ? (width - numberOfColumn * nodeWidth) / (numberOfColumn - 1) : 0.0;
    }

    /**
     * Compute the ratio between the value of a node and its
     * height. The main constraint is that each column has to
     * be displayed completely in the frame.
     *
     * @param height height of the frame
     */
    private void computeValueToHeightRatio(double height) {
        OptionalDouble totalValueOfTheBiggestColumn = nodes.stream()
                .mapToInt(SankeyNode::getHorizontalPosition)
                .distinct()
                .mapToDouble(this::computeTotalValueForColumn)
                .max();

        this.valueToHeightRatio = totalValueOfTheBiggestColumn.isPresent()  ?
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

    // Events

    private SetChangeListener<SankeyNode> nodesChangeListener = new SetChangeListener<SankeyNode>() {
        @Override
        public void onChanged(Change<? extends SankeyNode> change) {

        }
    };

    private SetChangeListener<SankeyLink> linksChangeListener = new SetChangeListener<SankeyLink>() {
        @Override
        public void onChanged(Change<? extends SankeyLink> change) {

        }
    };

    public void valueHasChangedFor(SankeyLink sankeyLink) {
        requestChartLayout();
    }

    public void nameHasChangedFor(SankeyNode sankeyNode) {
    }

    public void positionHasChangedFor(SankeyNode sankeyNode) {
        requestChartLayout();
    }
}
