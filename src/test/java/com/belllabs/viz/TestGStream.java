package test.java.com.belllabs.viz;

import java.awt.Dimension;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.swingViewer.DefaultView;
import org.graphstream.ui.swingViewer.View;
import org.graphstream.ui.swingViewer.Viewer;

public class TestGStream {
	public static void main(String[] args) {
		System.setProperty("org.graphstream.ui", "swing"); // With Graphstream 2.0
			
		Graph graph = new MultiGraph("Graph");
		Node node1 = graph.addNode("1");
		Node node2 = graph.addNode("2");
		Node node3 = graph.addNode("3");
		Node node4 = graph.addNode("4");
			
		node1.setAttribute("xyz", new double[] { 0, 0, 0 });
		node2.setAttribute("xyz", new double[] { 0, 10, 0 });
		node3.setAttribute("xyz", new double[] { 10, 0, 0 });
		node4.setAttribute("xyz", new double[] { 10, 10, 0 });
		
		// Get the panel
		Viewer viewer = graph.display(false);
		org.graphstream.ui.swingViewer.View viewPanel = viewer.addDefaultView(false);
        viewPanel.setPreferredSize(new Dimension(1400, 800));
		View panel = (DefaultView) viewer.getDefaultView();
			
		// Allow time for the viewer to build
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
			
		// Draw with graphics
		panel.getGraphics().drawRect(140, 20, 500, 500);
	}
}
