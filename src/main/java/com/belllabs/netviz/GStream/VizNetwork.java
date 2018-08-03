package main.java.com.belllabs.netviz.GStream;

import static java.lang.Math.toIntExact;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.geom.Point2;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.graphicGraph.stylesheet.StyleConstants;
import org.graphstream.ui.swingViewer.DefaultView;
import org.graphstream.ui.swingViewer.LayerRenderer;
import org.graphstream.ui.swingViewer.Viewer;
import org.graphstream.ui.swingViewer.ViewerListener;
import org.graphstream.ui.swingViewer.ViewerPipe;
import org.graphstream.ui.swingViewer.util.DefaultCamera;
import org.graphstream.ui.swingViewer.util.GraphMetrics;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.StoreLockException;

import javafx.util.Pair;
import main.java.com.belllabs.Helpers.VisualizationCSS;
import main.java.com.belllabs.Helpers.PortPropObject;
import main.java.com.belllabs.Utilities.GetGraphDbInfo;
import main.java.com.belllabs.algorithms.PathTraversals;

import org.graphstream.algorithm.Toolkit;

public class VizNetwork extends JPanel {
	protected static GraphDatabaseService graphDb;
	static Graph graph;
	private static final Logger LOGGER = Logger.getLogger(VizDeviceTemplates.class.getName());
	
	Result results; // query results of an execution
    

	public VizNetwork(String dbPath) throws Exception{
		/**
		 * @param dbPath
		 * @throws Exception
		 */
		
		File databaseDirectory = new File(dbPath);
		
		if (graphDb != null)
			graphDb.shutdown();

		graphDb = null;
				

		try{
			graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(databaseDirectory); // Open existing database
		}catch (Exception e) {
			Throwable t = e;

			while (t.getCause() != null) {
				t = t.getCause();

				if (t instanceof StoreLockException)
					throw new Error("neo4j db is locked by another connection", t);
			}

			throw new Exception(e);
		}
		
		LOGGER.info("Neo4j db connected");
		
		// Create the GStream Graph
		graph = new SingleGraph("Detailed View");
		
	    
	}
	
	public static Graph createGraph() {
	     
        // Global Switch starting
        float xStart = 1f, yStart = 1f;


        graph.setStrict(false);

        for (org.graphstream.graph.Node node : graph) {
            node.addAttribute("ui.label", node.getId());
        }
       
        // 1. Get the device List
        List<Pair<String, String>> deviceList = GetGraphDbInfo.getDeviceList(graphDb);

		// 2 Add ports and links in the GStream graph instance
        VizDeviceTemplates.AddPortsLinksToGraph(graphDb, graph, deviceList);

        //3. Visualize each device separately
//        LayoutDesign.SwitchLayout(graphDb, graph, device, xStart, yStart, PortSpacing.getPortXSpacingSwitch(), PortSpacing.getPortYSpacingSwitch()));
        LayoutDesign.ROADMLayout_2(graphDb, graph, deviceList, xStart, yStart);
//        LayoutDesign.MultiplSwitchesLayout(graphDb, graph, deviceList, xStart, yStart, PortSpacing.getPortXSpacingSwitch(), PortSpacing.getPortYSpacingSwitch());
//        LayoutDesign.switchROADMLayout(graphDb, graph, xStart, yStart, PortSpacing.getPortXSpacingSwitch(), PortSpacing.getPortYSpacingSwitch());
//        LayoutDesign.multipleROADMSLayout(graphDb, graph, xStart, yStart, PortSpacing.getPortXSpacingSwitch(), PortSpacing.getPortYSpacingSwitch());
//      LayoutDesign.AdapterLayout(graphDb, graph, new Pair<String, String>("Adapter", "M1"), xStart,  yStart, PortSpacing.getPortXSpacingAdapter(), PortSpacing.getPortYSpacingAdapter(), "left");
//        LayoutDesign.AbstractSwitchLayout(graphDb, graph,  xStart, 
//				yStart, PortSpacing.getPortXSpacingSwitch(), PortSpacing.getPortYSpacingSwitch());
      
  	
      return graph;

	}
	
	public static Graph createSubGraph(List<Pair<String, String>> deviceList){
		/**
		 * This load only part of the network that needs to be viewed as opposed to the whole network in createGraph()
		 * 
		 * 
		 */
		// Global Switch starting
        float xStart = 1f, yStart = 1f;


        graph.setStrict(false);

        for (org.graphstream.graph.Node node : graph) {
            node.addAttribute("ui.label", node.getId());
        }
        
    	// Decide what layout to use
		String deviceType = "";
		List<Pair<String, String>> deviceListAll = new ArrayList<Pair<String, String>>();
		for(int i=0; i<deviceList.size(); i++){
			deviceType = deviceList.get(i).getKey();
			deviceListAll.addAll(GetGraphDbInfo.getDeviceListByName(graphDb, deviceList.get(i).getValue()));	
		}
		
		VizDeviceTemplates.AddPortsLinksToGraph(graphDb, graph, deviceListAll);
			
		List<Pair<String, String>> switchROADMPairs = new ArrayList<Pair<String, String>>();
		switchROADMPairs.add(new Pair<String, String>("S1", "RDM1"));
        LayoutDesign.multipleSwitchROADMSLayout(graphDb, graph, xStart, yStart, PortSpacing.getPortXSpacingSwitch(), PortSpacing.getPortYSpacingSwitch(), switchROADMPairs);
//        LayoutDesign.switchAbstractROADMLayout(graphDb, graph, xStart, yStart, PortSpacing.getPortXSpacingSwitch(), PortSpacing.getPortYSpacingSwitch());

		return graph;
	}
	
	public void displayGraph(String snapName, int dimX, int dimY) throws IOException{
		/**
		 * This displays the graph created from the database
		 */
//		System.setProperty("org.graphstream.ui", "swing");
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

		JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        
   
		Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_SWING_THREAD);        
        org.graphstream.ui.swingViewer.View viewPanel = viewer.addDefaultView(false);
        viewPanel.setPreferredSize(new Dimension(dimX, dimY));
        
    
        graph.addAttribute("ui.stylesheet", VisualizationCSS.ROADMStyleSheet);
	    graph.addAttribute("ui.quality");
	    graph.addAttribute("ui.antialias");
	    
     	// Allow the graph to be drawn
    	// try {
    	//	Thread.sleep(500);
    	//} catch (InterruptedException e) {
    	//	e.printStackTrace();
    	//}
    
    	JPanel panel = new JPanel(new BorderLayout()){
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(dimX, dimY);
            }
        };
        
        panel.setBorder(BorderFactory.createLineBorder(Color.blue, 5));

        panel.add(viewPanel);
        frame.add(panel);
        
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        

    	// Save the screenshot of the JFrame
    	BufferedImage img = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_RGB);
    	frame.paint(img.getGraphics());
    	File outputfile = new File("data\\snapshots\\ppt\\" + snapName + ".png");
    	ImageIO.write(img, "png", outputfile);
    	
    	graphDb.shutdown();
 	    System.out.println("Graph database shut down...");
 	    

//        explore(graph.getNode("A"));
    }

	public static void findFlowPaths(String src, String dest, String color, String flowNum){
		/**
		 * @param src
		 * @param dest
		 * @param color
		 * @param flowNum
		 */
		List<String> pathList = PathTraversals.findSingleShortestPathBetweenPorts(graphDb, src, dest);
		for(String p: pathList){
			System.out.println(p + " ==> ");
		}
		FlowViz.flowPathHighlight(graph, pathList, color, flowNum);
	}
	
	
	
	public static void findFlowPaths(String src, String dest, String through, String color, String flowNum){
		/**
		 * @param src
		 * @param dest
		 * @param through
		 * @param color
		 * @param flowNum
		 */
		List<String> pathList = PathTraversals.findSingleShortestPathBetweenPortsThroughPorts(graphDb, src, dest, through);
		for(String p: pathList){
			System.out.println(p + " ==> ");
		}
		FlowViz.flowPathHighlight(graph, pathList, color, flowNum);
	}
	
	public static void findPathBetweenPorts(String srcTypes, String destTypes, String color, String flowNum){
		StringBuilder cssOptions = new StringBuilder();
		cssOptions.append("edge.linkFlow_" + flowNum + "{");
		cssOptions.append(" size: 2px; ");
		cssOptions.append(" arrow-shape:arrow; ");
		cssOptions.append(" arrow-size: 10px, 7px; ");
		cssOptions.append(" fill-color: " + color + "; ");
		cssOptions.append("}");
		if(! (VisualizationCSS.ROADMStyleSheet.contains("linkFlow_" + flowNum)) )
			VisualizationCSS.ROADMStyleSheet += cssOptions.toString();
		
		List<String> srcPorts = new ArrayList<String>();
		List<String> destPorts = new ArrayList<String>();

		Map<String, Object> params = new HashMap<>();
		String query;
		params.clear();
		params.put("dName", srcTypes);
		query = "MATCH (n) WHERE n.portName CONTAINS $dName RETURN n.portName as pName"  ; // Returns count
		Result results = graphDb.execute(query, params);
		
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
			String linkName = (String) row.get("pName");
			srcPorts.add(linkName);
		}
		params.clear();
		params.put("dName", destTypes);
		query = "MATCH (n) WHERE n.portName CONTAINS $dName RETURN n.portName as pName"  ; // Returns count
		results = graphDb.execute(query, params);
		
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
			String linkName = (String) row.get("pName");
			destPorts.add(linkName);
		}
		List<List<String>> allPaths = PathTraversals.findAllShortestPathsBetweenPorts(graphDb, srcPorts, destPorts);
		
		for(List<String> pathList: allPaths){
//			System.out.println(pathList.size());
			if(pathList.size() > 2)
				continue;
			for(String link: pathList){
				System.out.println("Color: " + link);
				try{
					org.graphstream.graph.Edge e = graph.getEdge(link);
					e.setAttribute("ui.class", "linkFlow_" + flowNum);
				}catch(Exception e)
				{
//					String[] portsList = link.split("\\.");

//					String src="", dest="";
//					int i=0;
//					while(true){
//						if(portsList[i].contains("in") || portsList[i].contains("out")){
//							src += portsList[i];
//							i++;
//							break;
//						}
//						src += portsList[i] + ".";
//						i++;
//					}
//					while(i<portsList.length){
//						dest += portsList[i] + ".";
//						i++; 
//					}
//					dest = dest.substring(0, dest.length()-1);
////					System.out.println(src  + "   " + dest);
//					org.graphstream.graph.Edge eNew = graph.addEdge(link, src, dest, true);
//					eNew.setAttribute("ui.class", "linkFlow_" + flowNum);
					System.out.println("problem with  coloring link: " + link);
					continue;
				}
				
			}
		}
		
	}

	public static void findAbstractPathBetweenPorts(String srcTypes, String destTypes, String color, String flowNum){
		StringBuilder cssOptions = new StringBuilder();
		cssOptions.append("edge.linkFlow_" + flowNum + "{");
		cssOptions.append(" size: 3px; ");
		cssOptions.append(" arrow-shape:arrow; ");
		cssOptions.append(" arrow-size: 10px, 7px; ");
		cssOptions.append(" fill-color: " + color + "; ");
		cssOptions.append("}");
		if(! (VisualizationCSS.ROADMStyleSheet.contains("linkFlow_" + flowNum)) )
			VisualizationCSS.ROADMStyleSheet += cssOptions.toString();
		
		List<String> srcPorts = new ArrayList<String>();
		List<String> destPorts = new ArrayList<String>();

		Map<String, Object> params = new HashMap<>();
		String query;
		params.clear();
		params.put("dName", srcTypes);
		query = "MATCH (n) WHERE n.portName CONTAINS $dName RETURN n.portName as pName"  ; // Returns count
		Result results = graphDb.execute(query, params);
		
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
			String linkName = (String) row.get("pName");
			srcPorts.add(linkName);
		}
		params.clear();
		params.put("dName", destTypes);
		query = "MATCH (n) WHERE n.portName CONTAINS $dName RETURN n.portName as pName"  ; // Returns count
		results = graphDb.execute(query, params);
		
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
			String linkName = (String) row.get("pName");
			destPorts.add(linkName);
		}
		
		for(String s: srcPorts){
			for(String d: destPorts){
				org.graphstream.graph.Edge e = graph.getEdge(s + "." + d);
				e.setAttribute("ui.class", "linkFlow_" + flowNum);
			}
		}
		
	}

}
