package javafx.scene.chart;

import javafx.beans.property.*;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Rectangle;

import java.util.*;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.max;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * @author Adrian Healey <adrian.j.healey@gmail.com>
 */
public class SankeyChart extends Chart {

    private double nodeWidth = 24;
    private double nodePadding = 8;

    private Set<SankeyNode> nodes;
    private Set<SankeyLink> links;

    public SankeyChart() {
        nodes = new HashSet<>();
        links = new HashSet<>();
    }

    public Set<SankeyNode> getNodes() {
        return nodes;
    }

    public void addNode(SankeyNode newNode) {
        this.nodes.add(newNode);
    }


    public void addLink(SankeyLink newLink) {
        this.links.add(newLink);
    }

    @Override
    protected void layoutChartChildren(double top, double left, double width, double height) {
        computeNodesValue();
        resetNodesHorizontalPosition();
        computeNodesHorizontalPosition();
        computeNodesVerticalPosition();

        computeNodesCoordinates(top, left, width, height);
        createLinksCurves();

        // Add nodes and links
        nodes.stream()
                .forEach(node -> this.getChartChildren().add(node));
        links.stream()
                .forEach(link -> this.getChartChildren().add(link));

    }

    void createLinksCurves() {

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
        double ratio = computeNodeValueToHeightRatio(height);
        // define nodes height
        nodes.stream()
                .forEach(node -> node.setHeight(node.value * ratio));
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
                .sorted((o1, o2) -> o1.value.compareTo(o2.value))
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
     * @return the ratio between value and height of a node.
     */
    private double computeNodeValueToHeightRatio(double height) {
        OptionalDouble totalValueOfTheBiggestColumn = nodes.stream()
                .mapToInt(SankeyNode::getHorizontalPosition)
                .distinct()
                .mapToDouble(this::computeTotalValueForColumn)
                .max();

        return totalValueOfTheBiggestColumn.isPresent()  ?
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
                .filter(node -> node.horizontalPosition == column)
                .sorted((o1, o2) -> o1.value.compareTo(o2.value))
                .collect(toList());
        IntStream.range(0, orderedNodes.size())
                .forEach(i -> orderedNodes.get(i).verticalPosition = i);
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
                        .filter(incomingNode -> incomingNode.horizontalPosition <= node.horizontalPosition)
                        .collect(toSet());
                if(!incomingNodes.isEmpty()) {
                    node.horizontalPosition ++;
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

    private void valueHasChangedFor(SankeyLink sankeyLink) {
    }

    private void valueHasChangedFor(SankeyNode sankeyNode) {
    }


    private void nameHasChangedFor(SankeyNode sankeyNode) {
    }

    // Data classes

    /**
     * Represent a node of the sankey chart
     */
    public static class SankeyNode extends Rectangle {

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

        public void setHorizontalPosition(int horizontalPosition) {
            this.horizontalPosition = horizontalPosition;
        }

        public int getVerticalPosition() {
            return verticalPosition;
        }

        /**
         * The chart which this data belongs to.
         */
        private ReadOnlyObjectWrapper<SankeyChart> chart = new ReadOnlyObjectWrapper<>(this, "chart");

        public SankeyNode(String name) {
            super(0, 0, 0, 0);
            SankeyNode.this.name.setValue(name);
            SankeyNode.this.value = 0.0;
            SankeyNode.this.horizontalPosition = 0;
            SankeyNode.this.verticalPosition = 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            SankeyNode that = (SankeyNode) o;

            if (!name.equals(that.name)) return false;
            return chart != null ? chart.equals(that.chart) : that.chart == null;

        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + name.hashCode();
            result = 31 * result + (chart != null ? chart.hashCode() : 0);
            return result;
        }
    }

    /**
     * Represent a link between two node of the Sankey
     * chart.
     */
    public static class SankeyLink extends CubicCurve {
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
                return SankeyLink.this;
            }

            @Override
            public String getName() {
                return "linkValue";
            }
        };

        public SankeyLink(SankeyNode source, SankeyNode target, Double value) {
            SankeyLink.this.source = source;
            SankeyLink.this.target = target;
            SankeyLink.this.value.setValue(value);
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
        private ReadOnlyObjectWrapper<SankeyChart> chart = new ReadOnlyObjectWrapper<>(this, "chart");

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
    }



}
