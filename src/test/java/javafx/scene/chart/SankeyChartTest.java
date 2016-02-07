package javafx.scene.chart;

import javafx.application.Application;
import javafx.embed.swing.JFXPanel;
import javafx.scene.chart.SankeyChart.SankeyLink;
import javafx.scene.chart.SankeyChart.SankeyNode;
import javafx.stage.Stage;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Adrian Healey <adrian.j.healey@gmail.com>
 */
public class SankeyChartTest {
    private SankeyChart sankey;
    private SankeyNode node1;
    private SankeyNode node2;
    private SankeyNode node3;
    private SankeyNode node4;

    private SankeyLink link1;
    private SankeyLink link2;
    private SankeyLink link3;
    private SankeyLink link4;

    @Before
    public void setUp() {
        new JFXPanel();

        sankey = new SankeyChart();

        node1 = new SankeyNode("node1");
        node2 = new SankeyNode("node2");
        node3 = new SankeyNode("node3");
        node4 = new SankeyNode("node4");
        sankey.addNode(node1);
        sankey.addNode(node2);
        sankey.addNode(node3);
        sankey.addNode(node4);

        link1 = new SankeyLink(node1, node2, 2.);
        link2 = new SankeyLink(node1, node3, 5.);
        link3 = new SankeyLink(node3, node4, 1.);
        link4 = new SankeyLink(node1, node4, 6.);
        sankey.addLink(link1);
        sankey.addLink(link2);
        sankey.addLink(link3);
        sankey.addLink(link4);
    }

    @Test
    public void computeNodesVerticalPositionTest() {
        node1.setHorizontalPosition(0);
        node2.setHorizontalPosition(1);
        node3.setHorizontalPosition(1);
        node4.setHorizontalPosition(2);

        node1.setValue(13.0);
        node2.setValue(2.0);
        node3.setValue(5.0);
        node4.setValue(7.0);

        sankey.computeNodesVerticalPosition();

        assertThat(node1.getVerticalPosition(), is(equalTo(0)));
        assertThat(node2.getVerticalPosition(), is(equalTo(0)));
        assertThat(node3.getVerticalPosition(), is(equalTo(1)));
        assertThat(node4.getVerticalPosition(), is(equalTo(0)));
    }

    @Test
    public void computeNodesHorizontalPositionForTest() {
        sankey.computeNodesHorizontalPosition();

        assertThat(node1.getHorizontalPosition(), is(equalTo(0)));
        assertThat(node2.getHorizontalPosition(), is(equalTo(1)));
        assertThat(node3.getHorizontalPosition(), is(equalTo(1)));
        assertThat(node4.getHorizontalPosition(), is(equalTo(2)));

    }

    @Test
    public void computeNodesValueTest() {
        sankey.computeNodesValue();

        assertThat(node1.getValue(), is(13.0));
        assertThat(node2.getValue(), is(2.0));
        assertThat(node3.getValue(), is(5.0));
        assertThat(node4.getValue(), is(7.0));
    }

    @Test
    public void sumOfLinksFromTest() {
        assertThat(sankey.sumOfLinksFrom(node1), is(equalTo(13.0)));
        assertThat(sankey.sumOfLinksFrom(node2), is(equalTo(0.0)));
        assertThat(sankey.sumOfLinksFrom(node3), is(equalTo(1.0)));
        assertThat(sankey.sumOfLinksFrom(node4), is(equalTo(0.0)));
    }

    @Test
    public void sumOfLinksTargetingTest() {
        assertThat(sankey.sumOfLinksTargeting(node1), is(equalTo(0.0)));
        assertThat(sankey.sumOfLinksTargeting(node2), is(equalTo(2.0)));
        assertThat(sankey.sumOfLinksTargeting(node3), is(equalTo(5.0)));
        assertThat(sankey.sumOfLinksTargeting(node4), is(equalTo(7.0)));
    }

}
