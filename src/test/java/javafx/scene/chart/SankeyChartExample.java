package javafx.scene.chart;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.SankeyChart.SankeyLink;
import javafx.scene.chart.SankeyChart.SankeyNode;
import javafx.stage.Stage;

/**
 * Created by Adrian Healey <adrian.j.healey@gmail.com>
 */
public class SankeyChartExample extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        SankeyChart sankey = new SankeyChart();

        SankeyNode node1 = new SankeyNode("node1");
        SankeyNode node2 = new SankeyNode("node2");
        SankeyNode node3 = new SankeyNode("node3");
        SankeyNode node4 = new SankeyNode("node4");
        sankey.addNode(node1);
        sankey.addNode(node2);
        sankey.addNode(node3);
        sankey.addNode(node4);

        SankeyLink link1 = new SankeyLink(node1, node2, 2.);
        SankeyLink link2 = new SankeyLink(node1, node3, 5.);
        SankeyLink link3 = new SankeyLink(node3, node4, 1.);
        SankeyLink link4 = new SankeyLink(node1, node4, 6.);
        sankey.addLink(link1);
        sankey.addLink(link2);
        sankey.addLink(link3);
        sankey.addLink(link4);

        Scene scene = new Scene(new Group());
        primaryStage.setTitle("Imported Fruits");
        primaryStage.setWidth(500);
        primaryStage.setHeight(500);


        ((Group) scene.getRoot()).getChildren().add(sankey);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
