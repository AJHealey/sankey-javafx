package javafx.scene.chart;

import javafx.beans.property.*;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Rectangle;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

        computeNodesHeight(height);

        // Add nodes and links
        nodes.stream()
                .forEach(node -> this.getChartChildren().add(node));
        links.stream()
                .forEach(link -> this.getChartChildren().add(link));

    }

    private void computeNodesHeight(double height) {
    }

    void computeNodesVerticalPosition() {
        nodes.stream()
                .mapToInt(SankeyNode::getHorizontalPosition)
                .distinct()
                .forEach(this::computeVerticalPositionForNodesInColumn);
    }

    private void computeVerticalPositionForNodesInColumn(int column) {
        List<SankeyNode> orderedNodes = nodes.stream()
                .filter(node -> node.horizontalPosition == column)
                .sorted((o1, o2) -> o1.value.compareTo(o2.value))
                .collect(toList());
        IntStream.range(0, orderedNodes.size())
                .forEach(i -> orderedNodes.get(i).verticalPosition = i);
    }

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

    void resetNodesHorizontalPosition() {
        nodes.stream()
                .forEach(node -> node.setValue(0.0));
    }

    void computeNodesValue() {
        nodes.stream()
                .forEach(this::updateValueFor);
    }

    void updateValueFor(SankeyNode node) {
        checkArgument(node != null, "node cannot be null");

        node.setValue(
                max(sumOfLinksFrom(node), sumOfLinksTargeting(node)));
    }

    double sumOfLinksTargeting(SankeyNode node) {
        return links.stream()
                .filter(link -> link.getTarget().equals(node))
                .mapToDouble(SankeyLink::getValue)
                .sum();
    }

    double sumOfLinksFrom(SankeyNode node) {
        return links.stream()
                .filter(link -> link.getSource().equals(node))
                .mapToDouble(SankeyLink::getValue)
                .sum();
    }

    Collection<SankeyNode> incomingNodesOf(SankeyNode node) {
        return links.stream()
                .filter(link -> link.getTarget().equals(node))
                .map(SankeyLink::getSource)
                .collect(toList());
    }

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
