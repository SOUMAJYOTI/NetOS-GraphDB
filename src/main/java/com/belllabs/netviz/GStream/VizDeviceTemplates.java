package main.java.com.belllabs.netviz.GStream;

import static java.lang.Math.toIntExact;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.StoreLockException;

import javafx.util.Pair;

import org.graphstream.ui.swingViewer.*;

import main.java.com.belllabs.Helpers.VisualizationCSS;
import main.java.com.belllabs.Helpers.PortPropObject;
import main.java.com.belllabs.Utilities.GetGraphDbInfo;
import main.java.com.belllabs.netviz.test_swing.drawRect;


public class VizDeviceTemplates extends JPanel{
	public static void AddPortsLinksToGraph(GraphDatabaseService graphDb, Graph graph, List<Pair<String, String>> deviceList){
		/**
		 *  deviceList - list of device names
		 */
		Map<String, Object> params = new HashMap<>();
		String query;
		
		Map<String, Integer> mapColorToIndex = new HashMap<String, Integer>();
		// Iterate over the devices 
		// Add the ports each device at a time
		

		for(Pair<String, String> device: deviceList){
			ArrayList<PortPropObject> portList = GetGraphDbInfo.getPortList(graphDb, device.getValue());
			
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
    		String deviceType = "";
    		if(numInpPorts > numOutPorts)
    			deviceType = "MUX";
			else
				deviceType = "DEMUX";
    		
    		
    		System.out.println(portList.size());
			// 1. Add nodes (ports)
    		int c_in = 0, c_out = 0, c_in_dwdm=0, c_out_dwdm=0;
			for(int i=0; i<portList.size(); i++){
				// *************************** The following Node is the interface belonging to GraphStream and not Neo4j ************************************
				// The methods can be later separated based on separate interfaces for visualization
				
//				System.out.println((String) portList.get(i).getPropViz("portName") + " --> " +  (String) portList.get(i).getPropViz("color"));
				org.graphstream.graph.Node n = graph.addNode((String) portList.get(i).getPropViz("portName"));
				n.addAttribute("ui.label", n.getId());
				if(device.getKey().equals("Switch") || device.getValue().contains("AbsROADM")){
					if( ((String) portList.get(i).getPropViz("portName")).contains("in"))
						n.setAttribute("ui.class", "switch_input");
					else
						n.setAttribute("ui.class", "switch_output");

				}
				if(device.getKey().contains("Adapter") || device.getValue().contains("AM") || device.getValue().contains("DDM") ){
	        		if(deviceType == "MUX" && ((String) portList.get(i).getPropViz("portName")).contains("in")) {
	        			
						n.setAttribute("ui.class", "oxc_digital_mux");
						
	        			StringBuilder cssOptions = new StringBuilder();
						cssOptions.append("node.oxc_digital_mux" + " {");
						cssOptions.append(" size: 10px, 10px; ");
						cssOptions.append(" fill-color: black; ");
						cssOptions.append(" text-alignment: above; ");
						cssOptions.append(" text-size: 1px; ");
						cssOptions.append("}");
						if(! (VisualizationCSS.ROADMStyleSheet.contains("oxc_digital_mux")) )
							VisualizationCSS.ROADMStyleSheet += cssOptions.toString();
	        		}
					else if(deviceType == "MUX" && ((String) portList.get(i).getPropViz("portName")).contains("out")){
							n.setAttribute("ui.class", "oxc_optical_mux");
							
		        			StringBuilder cssOptions = new StringBuilder();
							cssOptions.append("node.oxc_optical_mux" + " {");
							cssOptions.append(" size: 10px, 10px; ");
							cssOptions.append(" fill-color: white; ");
							cssOptions.append("	stroke-mode: plain;");
							cssOptions.append("	stroke-color: black;");
							cssOptions.append(" text-alignment: under; ");
							cssOptions.append(" text-size: 1px; ");
							cssOptions.append("}");
							if(! (VisualizationCSS.ROADMStyleSheet.contains("oxc_optical_mux")) )
								VisualizationCSS.ROADMStyleSheet += cssOptions.toString();
					}
					else if(deviceType == "DEMUX" && ((String) portList.get(i).getPropViz("portName")).contains("in")){
						n.setAttribute("ui.class", "oxc_optical_demux");

						StringBuilder cssOptions = new StringBuilder();
						cssOptions.append("node.oxc_optical_demux" + " {");
						cssOptions.append(" size: 10px, 10px; ");
						cssOptions.append(" fill-color: white; ");
						cssOptions.append("	stroke-mode: plain;");
						cssOptions.append("	stroke-color: black;");
						cssOptions.append(" text-alignment: under; ");
						cssOptions.append(" text-size: 1px; ");
						cssOptions.append("}");
						if(! (VisualizationCSS.ROADMStyleSheet.contains("oxc_optical_demux")) )
							VisualizationCSS.ROADMStyleSheet += cssOptions.toString();
					}
					else{
						n.setAttribute("ui.class", "oxc_digital_demux");
						
						StringBuilder cssOptions = new StringBuilder();
						cssOptions.append("node.oxc_digital_demux" + " {");
						cssOptions.append(" size: 10px, 10px; ");
						cssOptions.append(" fill-color: black; ");
						cssOptions.append(" text-alignment: above; ");
						cssOptions.append(" text-size: 1px; ");
						cssOptions.append("}");
						if(! (VisualizationCSS.ROADMStyleSheet.contains("oxc_digital_demux")) )
							VisualizationCSS.ROADMStyleSheet += cssOptions.toString();
					}

				}
				if(device.getValue().contains("WSS")){
//					if( ((String) portList.get(i).getPropViz("portName")).contains("in") && ((String) portList.get(i).getPropViz("color")).contains("rgb"))
//						System.out.println(portList.get(i).getPropViz("portName") + " color --> " + portList.get(i).getPropViz("color"));
					if( ((String) portList.get(i).getPropViz("color")).contains("None") && ((String) portList.get(i).getPropViz("portName")).contains("out")){
						
						n.setAttribute("ui.class", "wss_optical_out");
						
						StringBuilder cssOptions = new StringBuilder();
						cssOptions.append("node.wss_optical_out" + " {");
						cssOptions.append(" size: 10px, 10px; ");
						cssOptions.append(" fill-color: white; ");
						cssOptions.append("	stroke-mode: plain;");
						cssOptions.append("	stroke-color: black;");
						cssOptions.append(" text-alignment: at-right; ");
						cssOptions.append(" text-size: 1px; ");
						cssOptions.append("}");
						if(! (VisualizationCSS.ROADMStyleSheet.contains("wss_optical_out")) )
							VisualizationCSS.ROADMStyleSheet += cssOptions.toString();
					}
					else if( ((String) portList.get(i).getPropViz("color")).contains("None") && ((String) portList.get(i).getPropViz("portName")).contains("in")){
						n.setAttribute("ui.class", "wss_optical_inp");
						
						StringBuilder cssOptions = new StringBuilder();
						cssOptions.append("node.wss_optical_inp" + " {");
						cssOptions.append(" size: 10px, 10px; ");
						cssOptions.append(" fill-color: white; ");
						cssOptions.append("	stroke-mode: plain;");
						cssOptions.append("	stroke-color: black;");
						cssOptions.append(" text-alignment: at-left; ");
						cssOptions.append(" text-size: 1px; ");
						cssOptions.append("}");
						if(! (VisualizationCSS.ROADMStyleSheet.contains("wss_optical_inp")) )
							VisualizationCSS.ROADMStyleSheet += cssOptions.toString();
					}
					else if( (((String) portList.get(i).getPropViz("color")).contains("rgb")) && ((String) portList.get(i).getPropViz("portName")).contains("in")){						
						System.out.println(portList.get(i).getPropViz("portName") + " color --> " + portList.get(i).getPropViz("color"));

						String colorPort = (String) portList.get(i).getPropViz("color");
						if(mapColorToIndex.containsKey(colorPort + "_inp"))
							n.setAttribute("ui.class", "wss_dwdm_inp_c" + Integer.toString(mapColorToIndex.get(colorPort + "_inp")));
						else{
							mapColorToIndex.put(colorPort + "_inp", c_in);
							StringBuilder cssOptions = new StringBuilder();
							cssOptions.append("node.wss_dwdm_inp_c" + Integer.toString(c_in) + " {");
							cssOptions.append(" size: 10px, 10px; ");
							cssOptions.append(" fill-color: ");
							cssOptions.append( colorPort + "; ");
							cssOptions.append(" text-alignment: at-left; ");
							cssOptions.append(" text-size: 1px; ");
							cssOptions.append("}");
							if(! (VisualizationCSS.ROADMStyleSheet.contains(colorPort + "_inp")) )
								VisualizationCSS.ROADMStyleSheet += cssOptions.toString();
							n.setAttribute("ui.class", "wss_dwdm_inp_c" + Integer.toString(mapColorToIndex.get(colorPort + "_inp")));

							c_in += 1;
						}
					}
					else if( ((String) portList.get(i).getPropViz("color")).contains("rgb")  && ((String) portList.get(i).getPropViz("portName")).contains("out")){
						String colorPort = (String) portList.get(i).getPropViz("color");
						if(mapColorToIndex.containsKey(colorPort + "_out")){
//							System.out.println(mapColorToIndex.get(colorPort + '_out'));
							n.setAttribute("ui.class", "wss_dwdm_out_c" + Integer.toString(mapColorToIndex.get(colorPort + "_out")));
						}
						else{
							mapColorToIndex.put(colorPort + "_out", c_out);
							StringBuilder cssOptions = new StringBuilder();
							cssOptions.append("node.wss_dwdm_out_c" + Integer.toString(c_out) + " {");
							cssOptions.append(" size: 10px, 10px; ");
							cssOptions.append(" fill-color: ");
							cssOptions.append( colorPort + "; ");
							cssOptions.append(" text-alignment: at-right; ");
							cssOptions.append(" text-size: 1px; ");
							cssOptions.append("}");
							if(! (VisualizationCSS.ROADMStyleSheet.contains(colorPort + "_out")) )
								VisualizationCSS.ROADMStyleSheet += cssOptions.toString();
							n.setAttribute("ui.class", "wss_dwdm_out_c" + Integer.toString(mapColorToIndex.get(colorPort + "_out")));

							c_out += 1;

						}
					}
				}
				if(device.getValue().contains("AB")){
					if( (((String) portList.get(i).getPropViz("color")).contains("rgb")) && ((String) portList.get(i).getPropViz("portName")).contains("in")){						
//						System.out.println(portList.get(i).getPropViz("portName") + " color --> " + portList.get(i).getPropViz("color"));

						String colorPort = (String) portList.get(i).getPropViz("color");
						if(mapColorToIndex.containsKey(colorPort + "_inp_dwdm"))
							n.setAttribute("ui.class", "dwdm_inp_c" + Integer.toString(mapColorToIndex.get(colorPort + "_inp_dwdm")));
						else{
//							System.out.println(portList.get(i).getPropViz("portName") + " color --> " + portList.get(i).getPropViz("color"));
							mapColorToIndex.put(colorPort + "_inp_dwdm", c_in_dwdm);
							StringBuilder cssOptions = new StringBuilder();
							cssOptions.append("node.dwdm_inp_c" + Integer.toString(c_in_dwdm) + " {");
							cssOptions.append(" size: 10px, 10px; ");
							cssOptions.append(" fill-color: ");
							cssOptions.append( colorPort + "; ");
							cssOptions.append(" text-alignment: above; ");
							cssOptions.append(" text-size: 1px; ");
							cssOptions.append("}");
							if(! (VisualizationCSS.ROADMStyleSheet.contains(colorPort + "_inp")) )
								VisualizationCSS.ROADMStyleSheet += cssOptions.toString();
							n.setAttribute("ui.class", "dwdm_inp_c" + Integer.toString(mapColorToIndex.get(colorPort + "_inp_dwdm")));

							c_in_dwdm += 1;
						}
					}
					else if(((String) portList.get(i).getPropViz("portName")).contains("out")){
						n.setAttribute("ui.class", "dwdm_out_nc");
						
						StringBuilder cssOptions = new StringBuilder();
						cssOptions.append("node.dwdm_out_nc" + " {");
						cssOptions.append(" size: 10px, 10px; ");
						cssOptions.append(" fill-color: black; ");
						cssOptions.append(" text-alignment: under; ");
						cssOptions.append(" text-size: 1px; ");
						cssOptions.append("}");
						if(! (VisualizationCSS.ROADMStyleSheet.contains("dwdm_out_nc")) )
							VisualizationCSS.ROADMStyleSheet += cssOptions.toString();
					}
				}
				if(device.getValue().contains("DB")){
					if( (((String) portList.get(i).getPropViz("color")).contains("rgb")) && ((String) portList.get(i).getPropViz("portName")).contains("out")){						
						System.out.println(portList.get(i).getPropViz("portName") + " color --> " + portList.get(i).getPropViz("color"));

						String colorPort = (String) portList.get(i).getPropViz("color");
						if(mapColorToIndex.containsKey(colorPort + "_out_dwdm"))
							n.setAttribute("ui.class", "dwdm_out_c" + Integer.toString(mapColorToIndex.get(colorPort + "_out_dwdm")));
						else{
							mapColorToIndex.put(colorPort + "_out_dwdm", c_out_dwdm);
							StringBuilder cssOptions = new StringBuilder();
							cssOptions.append("node.dwdm_out_c" + Integer.toString(c_out_dwdm) + " {");
							cssOptions.append(" size: 10px, 10px; ");
							cssOptions.append(" fill-color: ");
							cssOptions.append( colorPort + "; ");
							cssOptions.append(" text-alignment: above; ");
							cssOptions.append(" text-size: 1px; ");
							cssOptions.append("}");
							if(! (VisualizationCSS.ROADMStyleSheet.contains(colorPort + "_out")) )
								VisualizationCSS.ROADMStyleSheet += cssOptions.toString();
							n.setAttribute("ui.class", "dwdm_out_c" + Integer.toString(mapColorToIndex.get(colorPort + "_out_dwdm")));

							c_out_dwdm += 1;
						}
					}
					else if(((String) portList.get(i).getPropViz("portName")).contains("in")){
						n.setAttribute("ui.class", "dwdm_inp_nc");
						
						StringBuilder cssOptions = new StringBuilder();
						cssOptions.append("node.dwdm_inp_nc" + " {");
						cssOptions.append(" size: 10px, 10px; ");
						cssOptions.append(" fill-color: black; ");
						cssOptions.append(" text-alignment: under; ");
						cssOptions.append(" text-size: 1px; ");
						cssOptions.append("}");
						if(! (VisualizationCSS.ROADMStyleSheet.contains("dwdm_inp_nc")) )
							VisualizationCSS.ROADMStyleSheet += cssOptions.toString();
					}
				}
			}
			
		}
//		
		// Add the edges to the graph
		for(Pair<String, String> device: deviceList){
			ArrayList<PortPropObject> portList = GetGraphDbInfo.getPortList(graphDb, device.getValue());
			// 2. Add edges (links between ports)
			for(int i=0; i<portList.size(); i++){
				// Find the neighbors of the current port
				params.clear();
				params.put("pName", (String) portList.get(i).getPropViz("portName"));
				
				
				query = "MATCH (n)-[r]->(nbr) WHERE n.portName contains $pName RETURN r as link, r.name as rName, nbr.portName as nbrPName";
				Result results = graphDb.execute(query, params);
				
				// Connect the current port with its neighbors 
				while (results.hasNext()) {
					Map<String, Object> row = results.next();
					Relationship r = (Relationship) row.get("r");
//					String relName = (String) row.get("rName");
		            Node x = (Node)row.get("nbr");
					String nbrPort = (String) row.get("nbrPName");
		            
					System.out.println((String) portList.get(i).getPropViz("portName") + " to " + nbrPort);
					String relName = (String) portList.get(i).getPropViz("portName") + "." + nbrPort;
//					if(nbrPort.contains("RDM"))
//						continue;
					try{
					org.graphstream.graph.Edge e = graph.addEdge(relName, (String) portList.get(i).getPropViz("portName"), nbrPort, true);
					if(device.getKey().contains("Switch") || device.getValue().contains("WSS"))
						e.setAttribute("ui.class", "switch");
					}catch(Exception e){
						continue;
					}
				}
			}
		}
	}
	
	public static void AddPortsLinksToGraph(GraphDatabaseService graphDb, Graph graph){
		/**
		 *  deviceList - list of device names
		 */
		Map<String, Object> params = new HashMap<>();
		
		String query = "MATCH (n) RETURN n.portName as pName, labels(n) as pLabel";
		Result results = graphDb.execute(query);
		
		String portNames = "";
		List<String> portNamesList = new ArrayList<String>();
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
            String pid = (String)row.get("pName");
            List<String> pLabelList = (List<String>)row.get("pLabel");
            String pLabel = pLabelList.get(0);
            
            String [] pidSplit = pid.split("\\.");
            StringBuilder s  = new StringBuilder();
            for(int i=0; i<pidSplit.length-1; i++){ // leave the last segment - that contains port info
            	s.append(pidSplit[i]);
            	s.append(".");
            }
            s.delete(s.length()-1, s.length());
            String pDeviceName = s.toString();
	
			org.graphstream.graph.Node n = graph.addNode(pid);
			n.addAttribute("ui.label", n.getId());
			if(pLabel.equals("Switch") || pLabel.contains("AbsROADM")){
				if( pid.contains("in"))
					n.setAttribute("ui.class", "switch_input");
				else
					n.setAttribute("ui.class", "switch_output");
			}
			
			portNamesList.add(pid);
		}

	
		for(String p: portNamesList){
			// Find the neighbors of the current port
			params.clear();
			params.put("pName", p);
			
			
			query = "MATCH (n)-[r]->(nbr) WHERE n.portName contains $pName RETURN r as link, r.name as rName, nbr.portName as nbrPName";
			results = graphDb.execute(query, params);

			// Connect the current port with its neighbors 
			while (results.hasNext()) {
				Map<String, Object> row = results.next();
				Relationship r = (Relationship) row.get("r");
				String relName = (String) row.get("rName");
	            Node x = (Node)row.get("nbr");
				String nbrPort = (String) row.get("nbrPName");
				
				System.out.println(p + "--->" + nbrPort);
	            	            
				// RelName is null for now
				org.graphstream.graph.Edge e = graph.addEdge(p + "." + nbrPort, p, nbrPort, true);
//				e.setAttribute("ui.class", "switch");

//				if(relname.equals("Switch") || pLabel.contains("AbsROADM"))
//					e.setAttribute("ui.class", "switch");
			}
		}
	}

	
	public static void vizAdapters(GraphDatabaseService graphDb, Graph graph, String deviceName, String position, float xStart, float yStart, float portXSpacing, 
									float portYSpacing, String above, String below){
		/**
		 * This method sets the relative position of the ports in the viewer
		 * 
		 * @param deviceName - the name of the device to be visualized
		 * @param deviceType - MUX/DEMUX/DWDM -AB/DB
		 * @param xStart - starting x-coord of the first input port
		 * @param yStart - starting y-coord of the first input port
		 * @param portXSpacing - spacing between the ports on the X axis
		 * @param portYSpacing - spacing between the ports on the Y axis
		 */
		
		Map<String, Object> params = new HashMap<>();
		String query;
		
		float xStartCurr = xStart, yStartCurr=yStart;
		
		
		if(position == "right"){
			// ----------- Set the output ports position

			// Get the count of output ports
			params.put("type", above);
			params.put("dName", deviceName);
			query = "MATCH (n) WHERE n.portName CONTAINS $type MATCH(n) WHERE n.portName contains $dName RETURN count(n) as count"  ; 
			Result results = graphDb.execute(query, params);
				
			Long numOutPorts = (long) 0;
			while (results.hasNext()) {
				Map<String, Object> row = results.next();
				numOutPorts = (Long) row.get("count");
			}
			int numIntOutPorts = toIntExact(numOutPorts);
			for(int i=numIntOutPorts; i>=1; i--){
				org.graphstream.graph.Node port = graph.getNode(deviceName  + "." + above  + Integer.toString(i));
		        port.addAttribute("layout.frozen");
				port.setAttribute("xy", xStartCurr, yStartCurr); // input ports are placed horizontally
				xStartCurr+= portXSpacing;
			}
			
			// ----------- Set the input ports position
			
			// Get the count of input ports
			params.put("type", below);
			params.put("dName", deviceName);
			query = "MATCH (n) WHERE n.portName CONTAINS $type MATCH(n) WHERE n.portName contains $dName RETURN count(n) as count"  ; 
			results = graphDb.execute(query, params);
					
			Long numInpPorts = (long) 0;
			while (results.hasNext()) {
				Map<String, Object> row = results.next();
				numInpPorts = (Long) row.get("count");
			}
			int numIntInpPorts = toIntExact(numInpPorts);
			int OutToInpRatio = numIntOutPorts/numIntInpPorts;
			xStartCurr = xStart + (float)(OutToInpRatio-1)/2  ;
			yStartCurr = yStart-portYSpacing;
			
			for(int i=numIntInpPorts; i>=1; i--){
				org.graphstream.graph.Node port = graph.getNode(deviceName  + "." + below  + Integer.toString(i));
		        port.addAttribute("layout.frozen");
				port.setAttribute("xy", xStartCurr, yStartCurr);
				xStartCurr += (portXSpacing*OutToInpRatio);
			}
		}
		else{
			// Get the count of input ports
			params.put("type", above);
			params.put("dName", deviceName);
			query = "MATCH (n) WHERE n.portName CONTAINS $type MATCH(n) WHERE n.portName contains $dName RETURN count(n) as count"  ; 
			Result results = graphDb.execute(query, params);
			
			Long numInpPorts = (long) 0;
			while (results.hasNext()) {
				Map<String, Object> row = results.next();
				numInpPorts = (Long) row.get("count");
			}
			int numIntInpPorts = toIntExact(numInpPorts);
			for(int i=numIntInpPorts; i>=1; i--){
				org.graphstream.graph.Node port = graph.getNode(deviceName  + "." + above  + Integer.toString(i));
		        port.addAttribute("layout.frozen");
				port.setAttribute("xy", xStartCurr, yStartCurr); // input ports are placed horizontally
				xStartCurr-= portXSpacing;
			}
			
			// ----------- Set the output ports position

			// Get the count of output ports
			params.put("type", below);
			params.put("dName", deviceName);
			query = "MATCH (n) WHERE n.portName CONTAINS $type MATCH(n) WHERE n.portName contains $dName RETURN count(n) as count"  ; 
			results = graphDb.execute(query, params);
					
			Long numOutPorts = (long) 0;
			while (results.hasNext()) {
				Map<String, Object> row = results.next();
				numOutPorts = (Long) row.get("count");
			}
			int numIntOutPorts = toIntExact(numOutPorts);
			
			int InpToOutRatio = numIntInpPorts/numIntOutPorts;
			xStartCurr = xStart - (float) (InpToOutRatio-1)/2  ;			
			yStartCurr = yStart-portYSpacing;
			
			for(int i=numIntOutPorts; i>=1; i--){
				org.graphstream.graph.Node port = graph.getNode(deviceName  + "." + below  + Integer.toString(i));
		        port.addAttribute("layout.frozen");
				port.setAttribute("xy", xStartCurr, yStartCurr);
				xStartCurr -= (portXSpacing*InpToOutRatio);
			}
		}
		
	}
	
	public static void vizAdaptersAlignSideway(GraphDatabaseService graphDb, Graph graph, String deviceName, String position, float xStart, float yStart, float portXSpacing, 
			float portYSpacing, String above, String below){
		/**
		* This method sets the relative position of the ports in the viewer
		* 
		* @param deviceName - the name of the device to be visualized
		* @param deviceType - MUX/DEMUX/DWDM -AB/DB
		* @param xStart - starting x-coord of the first input port
		* @param yStart - starting y-coord of the first input port
		* @param portXSpacing - spacing between the ports on the X axis
		* @param portYSpacing - spacing between the ports on the Y axis
		*/
		
		Map<String, Object> params = new HashMap<>();
		String query;
		
		float xStartCurr = xStart, yStartCurr=yStart;
		
		
		if(position == "right"){
			// ----------- Set the above ports position
			
			// Get the count of output ports
			params.put("type", above);
			params.put("dName", deviceName);
			query = "MATCH (n) WHERE n.portName CONTAINS $type MATCH(n) WHERE n.portName contains $dName RETURN count(n) as count"  ; 
			Result results = graphDb.execute(query, params);
			
			Long numOutPorts = (long) 0;
			while (results.hasNext()) {
				Map<String, Object> row = results.next();
				numOutPorts = (Long) row.get("count");
			}
			int numIntOutPorts = toIntExact(numOutPorts);
			for(int i=1; i<=numIntOutPorts; i++){
				org.graphstream.graph.Node port = graph.getNode(deviceName  + "." + above  + Integer.toString(i));
				port.addAttribute("layout.frozen");
				port.setAttribute("xy", xStartCurr, yStartCurr); // input ports are placed horizontally
				yStartCurr-= portYSpacing;
			}
			
			// ----------- Set the below ports position
			
			// Get the count of input ports
			params.put("type", below);
			params.put("dName", deviceName);
			query = "MATCH (n) WHERE n.portName CONTAINS $type MATCH(n) WHERE n.portName contains $dName RETURN count(n) as count"  ; 
			results = graphDb.execute(query, params);
			
			Long numInpPorts = (long) 0;
			while (results.hasNext()) {
				Map<String, Object> row = results.next();
				numInpPorts = (Long) row.get("count");
			}
			int numIntInpPorts = toIntExact(numInpPorts);
			int OutToInpRatio = numIntOutPorts/numIntInpPorts;
			yStartCurr = yStart - (float)(OutToInpRatio-1)/2  ;
			xStartCurr = xStart+portXSpacing;
			
			for(int i=1; i<=numIntInpPorts; i++){
				org.graphstream.graph.Node port = graph.getNode(deviceName  + "." + below  + Integer.toString(i));
				port.addAttribute("layout.frozen");
				port.setAttribute("xy", xStartCurr, yStartCurr);
				yStartCurr -= (portYSpacing*OutToInpRatio);
			}
		}
		else{
			// Get the count of above ports
			params.put("type", above);
			params.put("dName", deviceName);
			query = "MATCH (n) WHERE n.portName CONTAINS $type MATCH(n) WHERE n.portName contains $dName RETURN count(n) as count"  ; 
			Result results = graphDb.execute(query, params);
			
			Long numInpPorts = (long) 0;
			while (results.hasNext()) {
				Map<String, Object> row = results.next();
				numInpPorts = (Long) row.get("count");
			}
			int numIntInpPorts = toIntExact(numInpPorts);
			for(int i=1; i<=numIntInpPorts; i++){
				org.graphstream.graph.Node port = graph.getNode(deviceName  + "." + above  + Integer.toString(i));
				port.addAttribute("layout.frozen");
				port.setAttribute("xy", xStartCurr, yStartCurr); // input ports are placed horizontally
				yStartCurr-= portYSpacing;
			}
			
			// ----------- Set the below ports position
			
			// Get the count of output ports
			params.put("type", below);
			params.put("dName", deviceName);
			query = "MATCH (n) WHERE n.portName CONTAINS $type MATCH(n) WHERE n.portName contains $dName RETURN count(n) as count"  ; 
			results = graphDb.execute(query, params);
			
			Long numOutPorts = (long) 0;
			while (results.hasNext()) {
				Map<String, Object> row = results.next();
				numOutPorts = (Long) row.get("count");
			}
			int numIntOutPorts = toIntExact(numOutPorts);
			
			int InpToOutRatio = numIntInpPorts/numIntOutPorts;
			yStartCurr = yStart - (float) (InpToOutRatio-1)/2  ;			
			xStartCurr = xStart-portXSpacing;
			
			for(int i=1; i<=numIntOutPorts; i++){
				org.graphstream.graph.Node port = graph.getNode(deviceName  + "." + below  + Integer.toString(i));
				port.addAttribute("layout.frozen");
				port.setAttribute("xy", xStartCurr, yStartCurr);
				yStartCurr -= (portYSpacing*InpToOutRatio);
			}
		}

}
	
	public static void vizSwitch(GraphDatabaseService graphDb, Graph graph, String deviceName, float xStart, float yStart, float portXSpacing, float portYSpacing, boolean flip){
		/**
		 * This method sets the relative position of the ports in the viewer
		 * 
		 * @param deviceName - the name of the device to be visualized
		 * @param xStart - starting x-coord of the first input port
  		 * @param yStart - starting y-coord of the first input port
		 * @param portXSpacing
		 * @param portYSpacing
		 * @param flip - whether to flip the switch display
		 */
		
		
		Map<String, Object> params = new HashMap<>();
		String query;

		String left = "", right="";
		if(flip==true){
			left = "out";
			right = "in";
		}
		else{
			left = "in";
			right = "out";
		}
		// ----------- Set the input ports position
		float xStartCurr = xStart, yStartCurr=yStart;
		// Get the count of input ports
		params.put("type", left);
		params.put("dName", deviceName);
		query = "MATCH (n) WHERE n.portName CONTAINS $type MATCH(n) WHERE n.portName contains $dName RETURN count(n) as count"  ; 
		Result results = graphDb.execute(query, params);
				
		Long numInpPorts = (long) 0;
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
			numInpPorts = (Long) row.get("count");
		}
		int numIntInpPorts = toIntExact(numInpPorts);
		for(int i=1; i<=numIntInpPorts; i++){
//			System.out.println(deviceName  + "."  + left  + Integer.toString(i));
			org.graphstream.graph.Node port = graph.getNode(deviceName  + "."  + left  + Integer.toString(i));
	        port.addAttribute("layout.frozen");
//	        System.out.println(xStartCurr + " " + yStartCurr);
			port.setAttribute("xy", xStartCurr, yStartCurr);
			yStartCurr -= portYSpacing;
		}
		
		// ----------- Set the output ports position

		// Get the count of output ports
		xStartCurr = xStart + portXSpacing;
		yStartCurr =yStart;
		params.put("type", right);
		params.put("dName", deviceName);
		query = "MATCH (n) WHERE n.portName CONTAINS $type MATCH(n) WHERE n.portName contains $dName RETURN count(n) as count"  ; 
		results = graphDb.execute(query, params);
				
		Long numOutPorts = (long) 0;
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
			numOutPorts = (Long) row.get("count");
		}
		int numIntOutPorts = toIntExact(numOutPorts);
		for(int i=1; i<=numIntOutPorts; i++){
			org.graphstream.graph.Node port = graph.getNode(deviceName  + "."  + right  + Integer.toString(i));
	        port.addAttribute("layout.frozen");
			port.setAttribute("xy", xStartCurr, yStartCurr);
			yStartCurr -= portYSpacing;
		}
		
	}
	
	public static void vizAbstractSwitch(GraphDatabaseService graphDb, Graph graph, List<String> InpPorts, List<String> OutPorts, float xStart, float yStart, float portXSpacing, float portYSpacing, boolean flip){
		/**
		 * This method sets the relative position of the ports in the viewer
		 * 
		 * @param deviceName - the name of the device to be visualized
		 * @param xStart - starting x-coord of the first input port
  		 * @param yStart - starting y-coord of the first input port
		 * @param portXSpacing
		 * @param portYSpacing
		 * @param flip - whether to flip the switch display
		 */

		float xStartCurr = xStart, yStartCurr=yStart;
	
//		System.out.println(numIntInpPorts);
		for(int i=0; i<InpPorts.size(); i++){
//			System.out.println(deviceName  + "."  + left  + Integer.toString(i));
			org.graphstream.graph.Node port = graph.getNode(InpPorts.get(i));
	        port.addAttribute("layout.frozen");
			port.setAttribute("xy", xStartCurr, yStartCurr);
			yStartCurr -= portYSpacing;
		}

		xStartCurr = xStart + portXSpacing;
		yStartCurr =yStart;
		
		for(int i=0; i<OutPorts.size(); i++){
			org.graphstream.graph.Node port = graph.getNode(OutPorts.get(i));
	        port.addAttribute("layout.frozen");
			port.setAttribute("xy", xStartCurr, yStartCurr);
			yStartCurr -= portYSpacing;
		}
		
	}


	
	public void explore(org.graphstream.graph.Node source) {
	        Iterator<? extends org.graphstream.graph.Node> k = source.getBreadthFirstIterator();

	        while (k.hasNext()) {
	        	org.graphstream.graph.Node next = k.next();
	            next.setAttribute("ui.class", "marked");
	            sleep();
	        }
	    }

	
    protected void sleep() {
        try { Thread.sleep(1000); } catch (Exception e) {}
    }


}
