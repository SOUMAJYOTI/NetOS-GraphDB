/**
 * 
 * @author Soumajyoti Sarkar
 * @version 1.0
 * @since 06-01-2018
 */

package main.java.com.belllabs.Utilities;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import main.java.com.belllabs.Helpers.RelationPropObject;

public class RelationProperties {
	public static void setRelationProperties(GraphDatabaseService graphDb, String relName, int used, List<String> flowList){
		/**
		 * This method sets the relation properties in the links when the relation name is known
		 * 
		 * @param graphDb - Graph Database
		 * @param relName - relation Name (link name) - unique for every pair of input and output ports
		 * @param used - whether link is used or not
		 * @param flowList - List of strings denoting flows
		 * 
		 */
		Map<String, Object> params = new HashMap<>();
		final StringBuilder propertiesQuery  = new StringBuilder();

		RelationPropObject relProp = new RelationPropObject(relName); // Ideally this should be the input argument 
		relProp.setProperties(used, flowList);		
		Map<String, Object> relPropertiesObject = relProp.propertiesObject; // ideally, these should be protected and should be accessed through methods

		// Set the relation properties
		params = new HashMap<>();
		params.put("rName", relName); 
		
		// Iterate over all the properties passed to the relation - Integers
		propertiesQuery.setLength(0); //  clear
		propertiesQuery.append("MATCH (n1)-[r]->(n2) WHERE r.name = $rName  ");
		propertiesQuery.append("SET ");
		
		// The loop is run since it is not known here what properties exist - it can be dynamic
		/*
		 *  Equivalent to:
		 *  			MATCH (n1)-[r]->(n2) WHERE r.name = $rName SET r.valueProp1 = $valueProp1, r.valueProp2 = $valueProp2
		 */
		int cntProp = 0;
		for(Map.Entry<String, Object> entry : relPropertiesObject.entrySet()){
			params.put("valueProp" + Integer.toString(cntProp), entry.getValue());
			propertiesQuery.append("r.");
			propertiesQuery.append(entry.getKey()); // property Name
			propertiesQuery.append("=");
			propertiesQuery.append("$valueProp" + Integer.toString(cntProp)); // Property Value
			propertiesQuery.append(", ");
			cntProp += 1;
		}
		
		propertiesQuery.delete(propertiesQuery.length()-2, propertiesQuery.length());  // Delete the last ", " from the query string
		String rpQuery = propertiesQuery.toString();
		graphDb.execute(rpQuery, params);
		
	}
	
	public static void setRelationProperties(GraphDatabaseService graphDb, String nodeFrom, String nodeTo, int used, List<String> flowList){
		/**
		 * This method sets the relation properties in the links when the fromNode and toNode are known
		 * 
		 * @param graphDb - Graph Database
		 * @param nodeFrom
		 * @param NodeTo
		 * @param used - whether link is used or not
		 * @param flowList - List of strings denoting flows
		 * 
		 */
		Map<String, Object> params = new HashMap<>();
		final StringBuilder propertiesQuery  = new StringBuilder();
		String relName = nodeFrom + "." + nodeTo;

		RelationPropObject relProp = new RelationPropObject(relName); // Ideally this should be the input argument 
		relProp.setProperties(used, flowList);		
		Map<String, Object> relPropertiesObject = relProp.propertiesObject; // ideally, these should be protected and should be accessed through methods

		// Set the relation properties
		params = new HashMap<>();
		params.put("rName", relName); 
		
		// Iterate over all the properties passed to the relation - Integers
		params.put("nodeFrom", nodeFrom);
		params.put("nodeTo", nodeTo);
		params.put("rName", relName );
		propertiesQuery.setLength(0); //  clear
		propertiesQuery.append("MATCH (n1)-[r]->(n2) WHERE (n1.portName = $nodeFrom AND n2.portName = $nodeTo) SET r.name = $rName  ");
		propertiesQuery.append("SET ");
		
		// The loop is run since it is not known here what properties exist - it can be dynamic
		/*
		 *  Equivalent to:
		 *  			MATCH (n1)-[r]->(n2) WHERE r.name = $rName SET r.valueProp1 = $valueProp1, r.valueProp2 = $valueProp2
		 */
		int cntProp = 0;
		for(Map.Entry<String, Object> entry : relPropertiesObject.entrySet()){
			params.put("valueProp" + Integer.toString(cntProp), entry.getValue());
			propertiesQuery.append("r.");
			propertiesQuery.append(entry.getKey()); // property Name
			propertiesQuery.append("=");
			propertiesQuery.append("$valueProp" + Integer.toString(cntProp)); // Property Value
			propertiesQuery.append(", ");
			cntProp += 1;
		}
		
		propertiesQuery.delete(propertiesQuery.length()-2, propertiesQuery.length());  // Delete the last ", " from the query string
		String rpQuery = propertiesQuery.toString();
		graphDb.execute(rpQuery, params);
		
	}
}
