package main.java.com.belllabs.netviz.GStream;

import static java.lang.Math.toIntExact;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.graphstream.algorithm.Toolkit;
import org.graphstream.graph.Graph;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;


import javafx.util.Pair;

import org.graphstream.ui.graphicGraph.stylesheet.StyleConstants;
import org.graphstream.ui.swingViewer.*;
import org.graphstream.ui.swingViewer.util.GraphMetrics;

import main.java.com.belllabs.Helpers.VisualizationCSS;
import main.java.com.belllabs.Utilities.GetGraphDbInfo;
import main.java.com.belllabs.netviz.test_swing.drawRect;
import javafx.util.Pair;


public class LayoutDesign {
	public static void ROADMLayout_1(GraphDatabaseService graphDb, Graph graph, List<Pair<String, String>> deviceList, 
											float xStart, float yStart){
		
		// Store the number of WSS ports
		
		int maxSPorts = 0;	
		for(Pair<String, String> device: deviceList){
	    	if(device.getValue().contains("WSS")){
				Map<String, Object> params = new HashMap<>();
				String query;
				
				params.put("type", "in");
				params.put("dName", device.getValue());
				query = "MATCH (n) WHERE n.portName CONTAINS $type MATCH(n) WHERE n.portName contains $dName RETURN count(n) as count"  ; 
				Result results = graphDb.execute(query, params);
						
				Long numSPorts = (long) 0;
				while (results.hasNext()) {
					Map<String, Object> row = results.next();
					numSPorts = (Long) row.get("count");
				}
				
				if(toIntExact(numSPorts) > maxSPorts)
					maxSPorts = toIntExact(numSPorts);
	    	}
		}
				
		 for(Pair<String, String> device: deviceList){
//		    	System.out.println("Device: " + device.getKey());
		    	if(device.getValue().contains("WSS")){
		    		float xStartWSSCurr = xStart ;
	    			float yStartWSSCurr = yStart ;
	    			SwitchLayout(graphDb, graph, device.getValue(), xStartWSSCurr, 
	    						yStartWSSCurr, PortSpacing.getPortXSpacingSwitch(), PortSpacing.getPortYSpacingSwitch(), false);
		    	}
		    	if(device.getValue().contains("AM")){
		    		float xStartAdapCurr = xStart - 2*PortSpacing.getPortXSpacingAdapter() ;
	    			float yStartAdapCurr = yStart + 2*PortSpacing.getPortYSpacingSwitch();
	    				    			
	    			AdapterLayout(graphDb, graph, device, xStartAdapCurr, 
	    						yStartAdapCurr, PortSpacing.getPortXSpacingAdapter(), PortSpacing.getPortYSpacingAdapter(),"left");
		    	}
		    	
		    	if(device.getValue().contains("DDM")){
		    		float xStartAdapCurr = xStart + PortSpacing.getPortXSpacingSwitch() + 2*PortSpacing.getPortXSpacingAdapter() ;
	    			float yStartAdapCurr = yStart + 2*PortSpacing.getPortYSpacingSwitch();
	    				    			
	    			AdapterLayout(graphDb, graph, device, xStartAdapCurr, 
	    						yStartAdapCurr, PortSpacing.getPortXSpacingAdapter(), PortSpacing.getPortYSpacingAdapter(), "right");
		    	}
		    	if(device.getValue().contains("DB")){
		    		float xStartAdapCurr = xStart - 2*PortSpacing.getPortXSpacingAdapter() ;
	    			float yStartAdapCurr = yStart - ((maxSPorts) * PortSpacing.getPortYSpacingSwitch()) - ( 2*PortSpacing.getPortYSpacingSwitch());
	    				    			
	    			AdapterLayout(graphDb, graph, device, xStartAdapCurr, 
	    						yStartAdapCurr, PortSpacing.getPortXSpacingAdapter(), PortSpacing.getPortYSpacingAdapter(), "left");
		    	}
//		    	
		    	if(device.getValue().contains("AB")){
		    		float xStartAdapCurr = xStart +  PortSpacing.getPortXSpacingSwitch() + 2*PortSpacing.getPortXSpacingAdapter() ;
	    			float yStartAdapCurr = yStart - ((maxSPorts) * PortSpacing.getPortYSpacingSwitch()) - ( 2*PortSpacing.getPortYSpacingSwitch());
	    			
	    			
	    			AdapterLayout(graphDb, graph, device, xStartAdapCurr, 
	    						yStartAdapCurr, PortSpacing.getPortXSpacingAdapter(), PortSpacing.getPortYSpacingAdapter(), "right");
		    	}
		 }
		
	}
	
	public static void ROADMLayout_2(GraphDatabaseService graphDb, Graph graph, List<Pair<String, String>> deviceList, 
			float xStart, float yStart){

		// Store the number of WSS ports
		
		int maxSPorts = 0;	
		for(Pair<String, String> device: deviceList){
			if(device.getValue().contains("WSS")){
				Map<String, Object> params = new HashMap<>();
				String query;
				
				params.put("type", "in");
				params.put("dName", device.getValue());
				query = "MATCH (n) WHERE n.portName CONTAINS $type MATCH(n) WHERE n.portName contains $dName RETURN count(n) as count"  ; 
				Result results = graphDb.execute(query, params);
				
				Long numSPorts = (long) 0;
				while (results.hasNext()) {
					Map<String, Object> row = results.next();
					numSPorts = (Long) row.get("count");
				}
				
				if(toIntExact(numSPorts) > maxSPorts)
					maxSPorts = toIntExact(numSPorts);
			}
		}
		
		for(Pair<String, String> device: deviceList){
		//System.out.println("Device: " + device.getKey());
			if(device.getValue().contains("WSS")){
				float xStartWSSCurr = xStart ;
				float yStartWSSCurr = yStart ;
				SwitchLayout(graphDb, graph, device.getValue(), xStartWSSCurr, 
				yStartWSSCurr, PortSpacing.getPortXSpacingSwitch(), PortSpacing.getPortYSpacingSwitch(), false);
			}
			if(device.getValue().contains("AM")){
				float xStartAdapCurr = xStart - 2*PortSpacing.getPortXSpacingAdapter() ;
				float yStartAdapCurr = yStart + 2*PortSpacing.getPortYSpacingSwitch();
						
				AdapterLayout(graphDb, graph, device, xStartAdapCurr, 
				yStartAdapCurr, PortSpacing.getPortXSpacingAdapter(), PortSpacing.getPortYSpacingAdapter(),"left");
			}
			
			if(device.getValue().contains("DDM")){
				float xStartAdapCurr = xStart + PortSpacing.getPortXSpacingSwitch() + 2*PortSpacing.getPortXSpacingAdapter() ;
				float yStartAdapCurr = yStart + 2*PortSpacing.getPortYSpacingSwitch();
						
				AdapterLayout(graphDb, graph, device, xStartAdapCurr, 
						yStartAdapCurr, PortSpacing.getPortXSpacingAdapter(), PortSpacing.getPortYSpacingAdapter(), "right");
			}
			if(device.getValue().contains("DB")){
				float xStartAdapCurr = xStart - 3*PortSpacing.getPortXSpacingAdapter() ;
				float yStartAdapCurr = yStart - ((maxSPorts+1) * PortSpacing.getPortYSpacingSwitch());
						
				AdapterLayoutAlignSideway(graphDb, graph, device, xStartAdapCurr, 
				yStartAdapCurr, PortSpacing.getPortXSpacingAdapter(), PortSpacing.getPortYSpacingAdapter(), "left");
			}
			//
			if(device.getValue().contains("AB")){
				float xStartAdapCurr = xStart +  PortSpacing.getPortXSpacingSwitch() + 3*PortSpacing.getPortXSpacingAdapter() ;
				float yStartAdapCurr = yStart - ((maxSPorts+1) * PortSpacing.getPortYSpacingSwitch());
				
				
				AdapterLayoutAlignSideway(graphDb, graph, device, xStartAdapCurr, 
				yStartAdapCurr, PortSpacing.getPortXSpacingAdapter(), PortSpacing.getPortYSpacingAdapter(), "right");
			}
		}

}

	public static void SwitchLayout(GraphDatabaseService graphDb, Graph graph, String device, float xStart, 
									float yStart, float portXSwitchSpacing, float portYSwitchSpacing, boolean flip){
		float xStartSwitchCurr = xStart, yStartSwitchCurr = yStart;
	    
		VizDeviceTemplates.vizSwitch(graphDb, graph, device, xStartSwitchCurr, yStartSwitchCurr, portXSwitchSpacing, portYSwitchSpacing, flip);
	    	
	}
	
	

	public static void AdapterLayout(GraphDatabaseService graphDb, Graph graph, Pair<String, String> device, 
											float xStart, float yStart, float portXAdapSpacing, float portYAdapSpacing, String position){
		
		/**
		 * @param graphDb
		 * @param graph
		 * @param device
		 * @param xStart
		 * @param yStart
		 * @param portXAdapSpacing
		 * @param portYAdapSpacing
		 * @param position - letf or right depending on where to place the adapter wrt to the device from which the signals are sent 
		 */
		
		Map<String, Object> params = new HashMap<>();
		String query; 
		int maxSPorts = 0;
//	
		// Number of input ports
		params.put("type", "in");
		params.put("dName", device.getValue());
		query = "MATCH (n) WHERE n.portName CONTAINS $type MATCH(n) WHERE n.portName contains $dName RETURN count(n) as count"  ; 
		Result results = graphDb.execute(query, params);
				
		Long numInpPorts = (long) 0;
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
			numInpPorts = (Long) row.get("count");
		}
		
		// Number of output ports
		params.put("type", "out");
		params.put("dName", device.getValue());
		query = "MATCH (n) WHERE n.portName CONTAINS $type MATCH(n) WHERE n.portName contains $dName RETURN count(n) as count"  ; 
		results = graphDb.execute(query, params);
				
		Long numOutPorts = (long) 0;
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
			numOutPorts = (Long) row.get("count");
		}
		
		int numIntInpPorts = numInpPorts.intValue();
		int numIntOutPorts = numOutPorts.intValue();
		
		if (numIntInpPorts > numIntOutPorts && position == "left"){
			VizDeviceTemplates.vizAdapters(graphDb, graph, device.getValue(), position, xStart, yStart,  portXAdapSpacing, portYAdapSpacing, "in", "out");
		}
		else if (numIntInpPorts > numIntOutPorts && position == "right"){
			VizDeviceTemplates.vizAdapters(graphDb, graph, device.getValue(), position, xStart, yStart,  portXAdapSpacing, portYAdapSpacing, "in", "out");
		}
		else if (numIntInpPorts < numIntOutPorts && position == "left"){
			VizDeviceTemplates.vizAdapters(graphDb, graph, device.getValue(), position, xStart, yStart,  portXAdapSpacing, portYAdapSpacing, "out", "in");
		}
		else if (numIntInpPorts < numIntOutPorts && position == "right"){
			VizDeviceTemplates.vizAdapters(graphDb, graph, device.getValue(), position, xStart, yStart,  portXAdapSpacing, portYAdapSpacing, "out", "in");
		}
	}

	public static void AdapterLayoutAlignSideway(GraphDatabaseService graphDb, Graph graph, Pair<String, String> device, 
			float xStart, float yStart, float portXAdapSpacing, float portYAdapSpacing, String position){

		/**
		* @param graphDb
		* @param graph
		* @param device
		* @param xStart
		* @param yStart
		* @param portXAdapSpacing
		* @param portYAdapSpacing
		* @param position - letf or right depending on where to place the adapter wrt to the device from which the signals are sent 
		*/
		
		Map<String, Object> params = new HashMap<>();
		String query; 
		int maxSPorts = 0;
		//
		// Number of input ports
		params.put("type", "in");
		params.put("dName", device.getValue());
		query = "MATCH (n) WHERE n.portName CONTAINS $type MATCH(n) WHERE n.portName contains $dName RETURN count(n) as count"  ; 
		Result results = graphDb.execute(query, params);
		
		Long numInpPorts = (long) 0;
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
			numInpPorts = (Long) row.get("count");
		}
		
		// Number of output ports
		params.put("type", "out");
		params.put("dName", device.getValue());
		query = "MATCH (n) WHERE n.portName CONTAINS $type MATCH(n) WHERE n.portName contains $dName RETURN count(n) as count"  ; 
		results = graphDb.execute(query, params);
		
		Long numOutPorts = (long) 0;
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
			numOutPorts = (Long) row.get("count");
		}
		
		int numIntInpPorts = numInpPorts.intValue();
		int numIntOutPorts = numOutPorts.intValue();
		
		if (numIntInpPorts > numIntOutPorts && position == "left"){
			VizDeviceTemplates.vizAdaptersAlignSideway(graphDb, graph, device.getValue(), position, xStart, yStart,  portXAdapSpacing, portYAdapSpacing, "in", "out");
		}
		else if (numIntInpPorts > numIntOutPorts && position == "right"){
			VizDeviceTemplates.vizAdaptersAlignSideway(graphDb, graph, device.getValue(), position, xStart, yStart,  portXAdapSpacing, portYAdapSpacing, "in", "out");
		}
		else if (numIntInpPorts < numIntOutPorts && position == "left"){
			VizDeviceTemplates.vizAdaptersAlignSideway(graphDb, graph, device.getValue(), position, xStart, yStart,  portXAdapSpacing, portYAdapSpacing, "out", "in");
		}
		else if (numIntInpPorts < numIntOutPorts && position == "right"){
			VizDeviceTemplates.vizAdaptersAlignSideway(graphDb, graph, device.getValue(), position, xStart, yStart,  portXAdapSpacing, portYAdapSpacing, "out", "in");
		}
	}
	

	public static void MultiplSwitchesLayout(GraphDatabaseService graphDb, Graph graph, List<Pair<String, String>> deviceList, 
			float xStart, float yStart, float portXSwitchSpacing, float portYSwitchSpacing){
		float xStartSwitchCurr = xStart, yStartSwitchCurr = yStart;
	    
		VizDeviceTemplates.vizSwitch(graphDb, graph, "S1", xStartSwitchCurr, yStartSwitchCurr, portXSwitchSpacing, portYSwitchSpacing, false);
		
		xStartSwitchCurr = xStart + 3*portXSwitchSpacing;
		yStartSwitchCurr = yStart;
		
		VizDeviceTemplates.vizSwitch(graphDb, graph, "S2", xStartSwitchCurr, yStartSwitchCurr, portXSwitchSpacing, portYSwitchSpacing, false);

		xStartSwitchCurr = xStart + 3*portXSwitchSpacing;
		yStartSwitchCurr = yStart - 10*portYSwitchSpacing;
		
		VizDeviceTemplates.vizSwitch(graphDb, graph, "S3", xStartSwitchCurr, yStartSwitchCurr, portXSwitchSpacing, portYSwitchSpacing, false);

	}
	
	public static void switchAbstractROADMLayout(GraphDatabaseService graphDb, Graph graph, float xStart, float yStart, float portXSwitchSpacing, float portYSwitchSpacing){
		
		Map<String, Object> params = new HashMap<>();
		String query;
		
		// -----------------------------------------------------------------------------
		float xStartSwitchCurr = xStart, yStartSwitchCurr = yStart;
        SwitchLayout(graphDb, graph, "S1", xStartSwitchCurr, yStartSwitchCurr, PortSpacing.getPortXSpacingSwitch(), PortSpacing.getPortYSpacingSwitch(), true);
        xStartSwitchCurr = xStart + 2*portXSwitchSpacing;
		yStartSwitchCurr = yStart - 9*portYSwitchSpacing;
		
		List<String> pNames = new ArrayList<String>();
		params.clear();
		pNames.add("RDM1");
		params.put("portNames", pNames);
		params.put("type","in");
		
		query = "WITH $portNames AS portNamesList "
				+ "UNWIND portNamesList as pName "
				+ "MATCH (n) WHERE n.portName STARTS WITH  pName "
				+ "AND n.portName CONTAINS $type "
				+ "RETURN n.portName as portN"; // Can only return node n
		Result results = graphDb.execute(query, params);
		
		List<String> inpPortNames = new ArrayList<String>();
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
			inpPortNames.add((String) row.get("portN"));
		}
		
		pNames = new ArrayList<String>();
		params.clear();
		pNames.add("RDM1");
		params.put("portNames", pNames);
		params.put("type","out");
		
		query = "WITH $portNames AS portNamesList "
				+ "UNWIND portNamesList as pName "
				+ "MATCH (n) WHERE n.portName STARTS WITH  pName "
				+ "AND n.portName CONTAINS $type "
				+ "RETURN n.portName as portN"; // Can only return node n
		results = graphDb.execute(query, params);
		
		List<String> outPortNames = new ArrayList<String>();
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
			outPortNames.add((String) row.get("portN"));
		}

		VizDeviceTemplates.vizAbstractSwitch(graphDb, graph, inpPortNames, outPortNames, xStartSwitchCurr, yStartSwitchCurr, portXSwitchSpacing, portYSwitchSpacing, false);

		// -----------------------------------------------------------------------------

//		xStartSwitchCurr = xStart +  5*portXSwitchSpacing;
//		yStartSwitchCurr = yStart;
//        SwitchLayout(graphDb, graph, "S2", xStartSwitchCurr, yStartSwitchCurr, PortSpacing.getPortXSpacingSwitch(), PortSpacing.getPortYSpacingSwitch(), false);
//        xStartSwitchCurr = xStart + 7*portXSwitchSpacing;
//		yStartSwitchCurr = yStart - 7*portYSwitchSpacing;
//		
//		params.clear();
//		pNames = new ArrayList<String>();
//		params.clear();
//		pNames.add("RDM2");
//		params.put("portNames", pNames);
//		params.put("type","in");
//		
//		query = "WITH $portNames AS portNamesList "
//				+ "UNWIND portNamesList as pName "
//				+ "MATCH (n) WHERE n.portName STARTS WITH  pName "
//				+ "AND n.portName CONTAINS $type "
//				+ "RETURN n.portName as portN"; // Can only return node n
//		results = graphDb.execute(query, params);
//		
//		inpPortNames = new ArrayList<String>();
//		while (results.hasNext()) {
//			Map<String, Object> row = results.next();
//			inpPortNames.add((String) row.get("portN"));
//		}
//		
//		pNames = new ArrayList<String>();
//		params.clear();
//		pNames.add("RDM2");
//		params.put("portNames", pNames);
//		params.put("type","out");
//		
//		query = "WITH $portNames AS portNamesList "
//				+ "UNWIND portNamesList as pName "
//				+ "MATCH (n) WHERE n.portName STARTS WITH  pName "
//				+ "AND n.portName CONTAINS $type "
//				+ "RETURN n.portName as portN"; // Can only return node n
//		results = graphDb.execute(query, params);
//		
//		outPortNames = new ArrayList<String>();
//		while (results.hasNext()) {
//			Map<String, Object> row = results.next();
//			outPortNames.add((String) row.get("portN"));
//		}
//
//		VizDeviceTemplates.vizAbstractSwitch(graphDb, graph, inpPortNames, outPortNames, xStartSwitchCurr, yStartSwitchCurr, portXSwitchSpacing, portYSwitchSpacing, false);
//
//		// -----------------------------------------------------------------------------
//
//		xStartSwitchCurr = xStart + 10*portXSwitchSpacing;
//		yStartSwitchCurr = yStart;
//        SwitchLayout(graphDb, graph, "S3", xStartSwitchCurr, yStartSwitchCurr, PortSpacing.getPortXSpacingSwitch(), PortSpacing.getPortYSpacingSwitch(), false);
//        xStartSwitchCurr = xStart + 12*portXSwitchSpacing;
//      	yStartSwitchCurr = yStart - 7*portYSwitchSpacing;
//		
//  		params.clear();
//		pNames = new ArrayList<String>();
//		params.clear();
//		pNames.add("RDM3");
//		params.put("portNames", pNames);
//		params.put("type","in");
//		
//		query = "WITH $portNames AS portNamesList "
//				+ "UNWIND portNamesList as pName "
//				+ "MATCH (n) WHERE n.portName STARTS WITH  pName "
//				+ "AND n.portName CONTAINS $type "
//				+ "RETURN n.portName as portN"; // Can only return node n
//		results = graphDb.execute(query, params);
//		
//		inpPortNames = new ArrayList<String>();
//		while (results.hasNext()) {
//			Map<String, Object> row = results.next();
//			inpPortNames.add((String) row.get("portN"));
//		}
//		
//		pNames = new ArrayList<String>();
//		params.clear();
//		pNames.add("RDM3");
//		params.put("portNames", pNames);
//		params.put("type","out");
//		
//		query = "WITH $portNames AS portNamesList "
//				+ "UNWIND portNamesList as pName "
//				+ "MATCH (n) WHERE n.portName STARTS WITH  pName "
//				+ "AND n.portName CONTAINS $type "
//				+ "RETURN n.portName as portN"; // Can only return node n
//		results = graphDb.execute(query, params);
//		
//		outPortNames = new ArrayList<String>();
//		while (results.hasNext()) {
//			Map<String, Object> row = results.next();
//			outPortNames.add((String) row.get("portN"));
//		}
//
//		VizDeviceTemplates.vizAbstractSwitch(graphDb, graph, inpPortNames, outPortNames, xStartSwitchCurr, yStartSwitchCurr, portXSwitchSpacing, portYSwitchSpacing, false);
//

	}
	
	public static void multipleSwitchROADMSLayout(GraphDatabaseService graphDb, Graph graph, float xStart, float yStart, float portXROADMSpacing, float portYROADMSpacing, List<Pair<String, String>> switchROADMPairs){
		for(int i=0; i<switchROADMPairs.size(); i++){
			float xStartSwitchCurr = xStart+ 7*i*portXROADMSpacing;
			float yStartSwitchCurr = yStart;
	        SwitchLayout(graphDb, graph, switchROADMPairs.get(i).getKey(), xStartSwitchCurr, yStartSwitchCurr, PortSpacing.getPortXSpacingSwitch(), PortSpacing.getPortYSpacingSwitch(), true);
	       
	        // Get the number of switch ports
	    	Map<String, Object> params = new HashMap<>();
			String query;
			params.put("dName", switchROADMPairs.get(i).getKey());
			query = "MATCH (n) WHERE n.portName contains $dName RETURN count(n) as count"  ; 
			Result results = graphDb.execute(query, params);
				
			Long numSwitchPorts = (long) 0;
			while (results.hasNext()) {
				Map<String, Object> row = results.next();
				numSwitchPorts = (Long) row.get("count");
			}
			int numSwitchInpPorts = numSwitchPorts.intValue() / 2;
			
	        List<Pair<String, String>> deviceList = GetGraphDbInfo.getDeviceListByName(graphDb, switchROADMPairs.get(i).getValue());
			float xStartRDMCurr = xStart + 7*i*portXROADMSpacing;
			float yStartRDMCurr = yStart - (numSwitchInpPorts+3)*portYROADMSpacing;
			ROADMLayout_2(graphDb, graph, deviceList, xStartRDMCurr, yStartRDMCurr);
		}
		
//		xStartSwitchCurr = xStart + 3*portXSwitchSpacing;
//		yStartSwitchCurr = yStart - 10*portYSwitchSpacing;
//		
//		VizDeviceTemplates.vizSwitch(graphDb, graph, "S3", xStartSwitchCurr, yStartSwitchCurr, portXSwitchSpacing, portYSwitchSpacing, false);

	}
	
	public static void MultipleSwitchAbstractROADMLayout(GraphDatabaseService graphDb, Graph graph, float xStart, float yStart, float portXSwitchSpacing, float portYSwitchSpacing, List<Pair<String, String>> switchROADMPairs){
//		float xStartSwitchCurr = xStart, yStartSwitchCurr = yStart;
//
//		for(int i=0;i<switchROADMPairs.size();i++){
//			xStartSwitchCurr = xStart +  5*i*portXSwitchSpacing;
//		}
//
		Map<String, Object> params = new HashMap<>();
		String query;
		
		// -----------------------------------------------------------------------------
		float xStartSwitchCurr = xStart, yStartSwitchCurr = yStart;
        SwitchLayout(graphDb, graph, "S1", xStartSwitchCurr, yStartSwitchCurr, PortSpacing.getPortXSpacingSwitch(), PortSpacing.getPortYSpacingSwitch(), false);
        xStartSwitchCurr = xStart + 2*portXSwitchSpacing;
		yStartSwitchCurr = yStart - 7*portYSwitchSpacing;
		
		List<String> pNames = new ArrayList<String>();
		params.clear();
		pNames.add("RDM1");
		params.put("portNames", pNames);
		params.put("type","in");
		
		query = "WITH $portNames AS portNamesList "
				+ "UNWIND portNamesList as pName "
				+ "MATCH (n) WHERE n.portName STARTS WITH  pName "
				+ "AND n.portName CONTAINS $type "
				+ "RETURN n.portName as portN"; // Can only return node n
		Result results = graphDb.execute(query, params);
		
		List<String> inpPortNames = new ArrayList<String>();
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
			inpPortNames.add((String) row.get("portN"));
		}
		
		pNames = new ArrayList<String>();
		params.clear();
		pNames.add("RDM1");
		params.put("portNames", pNames);
		params.put("type","out");
		
		query = "WITH $portNames AS portNamesList "
				+ "UNWIND portNamesList as pName "
				+ "MATCH (n) WHERE n.portName STARTS WITH  pName "
				+ "AND n.portName CONTAINS $type "
				+ "RETURN n.portName as portN"; // Can only return node n
		results = graphDb.execute(query, params);
		
		List<String> outPortNames = new ArrayList<String>();
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
			outPortNames.add((String) row.get("portN"));
		}

		VizDeviceTemplates.vizAbstractSwitch(graphDb, graph, inpPortNames, outPortNames, xStartSwitchCurr, yStartSwitchCurr, portXSwitchSpacing, portYSwitchSpacing, false);

		// -----------------------------------------------------------------------------

		xStartSwitchCurr = xStart +  5*portXSwitchSpacing;
		yStartSwitchCurr = yStart;
        SwitchLayout(graphDb, graph, "S2", xStartSwitchCurr, yStartSwitchCurr, PortSpacing.getPortXSpacingSwitch(), PortSpacing.getPortYSpacingSwitch(), false);
        xStartSwitchCurr = xStart + 7*portXSwitchSpacing;
		yStartSwitchCurr = yStart - 7*portYSwitchSpacing;
		
		params.clear();
		pNames = new ArrayList<String>();
		params.clear();
		pNames.add("RDM2");
		params.put("portNames", pNames);
		params.put("type","in");
		
		query = "WITH $portNames AS portNamesList "
				+ "UNWIND portNamesList as pName "
				+ "MATCH (n) WHERE n.portName STARTS WITH  pName "
				+ "AND n.portName CONTAINS $type "
				+ "RETURN n.portName as portN"; // Can only return node n
		results = graphDb.execute(query, params);
		
		inpPortNames = new ArrayList<String>();
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
			inpPortNames.add((String) row.get("portN"));
		}
		
		pNames = new ArrayList<String>();
		params.clear();
		pNames.add("RDM2");
		params.put("portNames", pNames);
		params.put("type","out");
		
		query = "WITH $portNames AS portNamesList "
				+ "UNWIND portNamesList as pName "
				+ "MATCH (n) WHERE n.portName STARTS WITH  pName "
				+ "AND n.portName CONTAINS $type "
				+ "RETURN n.portName as portN"; // Can only return node n
		results = graphDb.execute(query, params);
		
		outPortNames = new ArrayList<String>();
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
			outPortNames.add((String) row.get("portN"));
		}

		VizDeviceTemplates.vizAbstractSwitch(graphDb, graph, inpPortNames, outPortNames, xStartSwitchCurr, yStartSwitchCurr, portXSwitchSpacing, portYSwitchSpacing, false);

		// -----------------------------------------------------------------------------

		xStartSwitchCurr = xStart + 10*portXSwitchSpacing;
		yStartSwitchCurr = yStart;
        SwitchLayout(graphDb, graph, "S3", xStartSwitchCurr, yStartSwitchCurr, PortSpacing.getPortXSpacingSwitch(), PortSpacing.getPortYSpacingSwitch(), false);
        xStartSwitchCurr = xStart + 12*portXSwitchSpacing;
      	yStartSwitchCurr = yStart - 7*portYSwitchSpacing;
		
  		params.clear();
		pNames = new ArrayList<String>();
		params.clear();
		pNames.add("RDM3");
		params.put("portNames", pNames);
		params.put("type","in");
		
		query = "WITH $portNames AS portNamesList "
				+ "UNWIND portNamesList as pName "
				+ "MATCH (n) WHERE n.portName STARTS WITH  pName "
				+ "AND n.portName CONTAINS $type "
				+ "RETURN n.portName as portN"; // Can only return node n
		results = graphDb.execute(query, params);
		
		inpPortNames = new ArrayList<String>();
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
			inpPortNames.add((String) row.get("portN"));
		}
		
		pNames = new ArrayList<String>();
		params.clear();
		pNames.add("RDM3");
		params.put("portNames", pNames);
		params.put("type","out");
		
		query = "WITH $portNames AS portNamesList "
				+ "UNWIND portNamesList as pName "
				+ "MATCH (n) WHERE n.portName STARTS WITH  pName "
				+ "AND n.portName CONTAINS $type "
				+ "RETURN n.portName as portN"; // Can only return node n
		results = graphDb.execute(query, params);
		
		outPortNames = new ArrayList<String>();
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
			outPortNames.add((String) row.get("portN"));
		}

		VizDeviceTemplates.vizAbstractSwitch(graphDb, graph, inpPortNames, outPortNames, xStartSwitchCurr, yStartSwitchCurr, portXSwitchSpacing, portYSwitchSpacing, false);


	}

//	
//	public static void setRectanglesAroundDevices(GraphDatabaseService graphDb, Graph graph, List<Pair<String, String>> deviceList, DefaultView panelInst){
//		float thickness = 3;
//    	Graphics2D g2 = (Graphics2D) panelInst.getGraphics();
//    	Stroke oldStroke = g2.getStroke();
//    	g2.setStroke(new BasicStroke(thickness));
//    	
//		float portXSwitchSpacing = 2.5f, portYSwitchSpacing = 1f;
//    	float portXAdapSpacing = 1f, portYAdapSpacing = 2f;
//    	Map<String, Object> params = new HashMap<>();
//		String query;
//    	for(Pair<String, String> device: deviceList){
//    		System.out.println("\n" + device.getKey() );
//	    	
//	    	ArrayList<portPropObject> portList = GetGraphDbInfo.getPortList(graphDb, device.getValue());
//	    	
//	    	params.clear();
//	    	params.put("type", "in");
//    		params.put("dName", device.getValue());
//    		query = "MATCH (n) WHERE n.portName CONTAINS $type MATCH(n) WHERE n.portName contains $dName RETURN count(n) as count"  ; 
//    		Result results = graphDb.execute(query, params);
//    				
//    		Long numPorts = (long) 0;
//    		while (results.hasNext()) {
//    			Map<String, Object> row = results.next();
//    			numPorts = (Long) row.get("count");
//    		}
//    		
//    		int numIntPorts = toIntExact(numPorts)/2;
//    		org.graphstream.graph.Node node = graph.getNode(device.getValue() + ".in1");
//    		double pos[] = Toolkit.nodePosition(node);
//    		
//    		GraphMetrics gm = new GraphMetrics();
//    		double pixelPosX = gm.lengthToPx(pos[0], StyleConstants.Units.GU);
//    		System.out.printf("\n %s %2f : %2f", "Ratio", pos[0], pixelPosX);
//    		int heightDev=0, widthDev=0;
//    		if(device.getKey().equals("Switch") || device.getKey().equals("WSS")){
//        		heightDev = (int)(pos[1] + numIntPorts*(portYSwitchSpacing))*30;
//        		widthDev = (int)(pos[0] + portXSwitchSpacing)*30;
//    		}
//    		if(device.getKey().equals("Adapter")){
//        		widthDev = (int)(pos[0] + numIntPorts*(portXAdapSpacing));
//        		heightDev = (int)(pos[1] + portYAdapSpacing);
//    		}
//    		
//    		System.out.printf("%d, %d, %d %d", (int)pos[0]*30, (int)pos[1]*30, widthDev, heightDev);
//        	g2.drawRect((int)pos[0]*30, (int)pos[1]*30, widthDev, heightDev);
//
//    	}
//	}
}
