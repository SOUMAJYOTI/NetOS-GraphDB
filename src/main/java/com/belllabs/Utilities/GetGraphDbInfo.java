package main.java.com.belllabs.Utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import javafx.util.Pair;
import main.java.com.belllabs.Helpers.PortPropObject;

public class GetGraphDbInfo {
	public static ArrayList<PortPropObject> getPortList(GraphDatabaseService graphDb, String deviceName){
		/**
		 * 
		 * The purpose of this method is to extract all the nodes of a device from the graph database and then store them in a list of  portPropObject
		 * 
		 * @param deviceName - name of device whose string has to be derived
		 * 
		 */
				
		Map<String, Object> params = new HashMap<>();
		String query;
		
		params.clear();
		params.put("dName", deviceName);
		
		query = "MATCH (n) WHERE n.portName contains $dName RETURN n as port, n.portName as pid, labels(n) as l";
		Result results = graphDb.execute(query, params);
		
		ArrayList<PortPropObject> portList = new ArrayList<PortPropObject>();
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
            Node x = (Node)row.get("port");
            String pid = (String)row.get("pid");
            List<String> pLabel = (List<String>)row.get("l");
            
            PortPropObject portObj = new PortPropObject(pLabel.get(0));
            
            try ( Transaction tx = graphDb.beginTx() ){
	            for (String prop_x : x.getPropertyKeys()) {
	            	portObj.propertiesObject.put(prop_x, x.getProperty(prop_x));
	            }
	            tx.success();
	            tx.close();
            }
        	portList.add(portObj);
	     }
		
		return portList;	
	}
	
	public static List<Pair<String, String>> getDeviceList(GraphDatabaseService graphDb){
		// The device names can be extracted from the first component of each port portName property of that device
		// This can be changed by adding device name property to the ports 
		
		String query = "MATCH (n) RETURN n.portName as pName, labels(n) as pLabel";
		Result results = graphDb.execute(query);
		
		String portNames = "";
		List<Pair<String, String>> deviceList = new ArrayList<Pair<String, String>>();
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
            String pid = (String)row.get("pName");
            List<String> pLabel = (List<String>)row.get("pLabel");
            String [] pidSplit = pid.split("\\.");
            StringBuilder s  = new StringBuilder();
            for(int i=0; i<pidSplit.length-1; i++){ // leave the last segment - that contains port info
            	s.append(pidSplit[i]);
            	s.append(".");
            }
            s.delete(s.length()-1, s.length());
            String pDeviceName = s.toString();
//            System.out.println(pDeviceName);
            if(!deviceList.contains(new Pair<String, String>(pLabel.get(0), pDeviceName)))
            	deviceList.add(new Pair<String, String>(pLabel.get(0), pDeviceName) );
		}
		
		return deviceList;
	}
	
	public static List<Pair<String, String>> getAbstractDeviceList(GraphDatabaseService graphDb){
		// The device names can be extracted from the first component of each port portName property of that device
		// This can be changed by adding device name property to the ports 
		
		String query = "MATCH (n) RETURN n.portName as pName, labels(n) as pLabel";
		Result results = graphDb.execute(query);
		
		String portNames = "";
		List<Pair<String, String>> deviceList = new ArrayList<Pair<String, String>>();
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
            String pid = (String)row.get("pName");
            List<String> pLabel = (List<String>)row.get("pLabel");
            String [] pidSplit = pid.split("\\.");
            StringBuilder s  = new StringBuilder();
            for(int i=0; i<pidSplit.length-1; i++){ // leave the last segment - that contains port info
            	s.append(pidSplit[i]);
            	s.append(".");
            }
            s.delete(s.length()-1, s.length());
            String pDeviceName = s.toString();
//            System.out.println(pDeviceName);
            if(!deviceList.contains(new Pair<String, String>(pLabel.get(0), pDeviceName)))
            	deviceList.add(new Pair<String, String>(pLabel.get(0), pDeviceName) );
		}
		
		return deviceList;
	}
	
	public static List<Pair<String, String>> getDeviceListByName(GraphDatabaseService graphDb, String deviceName){
		// The device names can be extracted from the first component of each port portName property of that device
		// This can be changed by adding device name property to the ports 
		
		Map<String, Object> params = new HashMap<>();
		
		params.clear();
		params.put("dName", deviceName);

		String query = "MATCH (n) WHERE n.portName STARTS WITH $dName RETURN n.portName as pName, labels(n) as pLabel";
		Result results = graphDb.execute(query, params);
		
		String portNames = "";
		List<Pair<String, String>> deviceList = new ArrayList<Pair<String, String>>();
		while (results.hasNext()) {
			Map<String, Object> row = results.next();
            String pid = (String)row.get("pName");
            List<String> pLabel = (List<String>)row.get("pLabel");
            String [] pidSplit = pid.split("\\.");
            StringBuilder s  = new StringBuilder();
            for(int i=0; i<pidSplit.length-1; i++){ // leave the last segment - that contains port info
            	s.append(pidSplit[i]);
            	s.append(".");
            }
            s.delete(s.length()-1, s.length());
            String pDeviceName = s.toString();
//            System.out.println(pDeviceName);
            if(!deviceList.contains(new Pair<String, String>(pLabel.get(0), pDeviceName)))
            	deviceList.add(new Pair<String, String>(pLabel.get(0), pDeviceName) );
		}
		
		return deviceList;
	}
	
}
