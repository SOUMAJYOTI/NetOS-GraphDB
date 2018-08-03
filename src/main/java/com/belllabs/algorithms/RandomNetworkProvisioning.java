package main.java.com.belllabs.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;


public class RandomNetworkProvisioning {
	public static List<String> addFlowToNetwork(GraphDatabaseService graphDb, String flowAdded, String portFlowAdd){
		/**
		 * 
		 * NOTE:This is not an algorithm to instantiate a flow in the network - there are no checks on flows based on network resource state.
		 *      This is just a random network provisioning procedure to run queries on networks with flow paths.
		 * 
		 * NOTE: This is a greedy provisioning - find the first available link to pass the flow through
		 * @param graphDb
		 * @param flowAdded - the flow value (now in String) to be added to the network
		 * @param portFlowAdd - the port to where the flow has to be added
		 * 
		 * @return pathNameList - the link names on the path on which the flow is provisioned
		 */
		
		// Get the residual capacity of the portFlowAdd port - check for constraints
		
		String query;
		Map<String, Object> params = new HashMap<>();
		params.put("pName", portFlowAdd);
		
		List<String> pathNameList = new ArrayList<String>();
		try ( Transaction tx = graphDb.beginTx() ){
			query = "MATCH (n) WHERE n.portName=$pName RETURN n";
			Result results = graphDb.execute(query, params);
	
			Node currNode = null;
			while (results.hasNext()) {
				Map<String, Object> row = results.next();
				currNode = (Node) row.get("n");
				float residualCapacity = (float) currNode.getProperty("residualCapacity");
				
				// TODO: In general, the following condition is not correct
				// the flow should be float and the residual capacity should be greater equal than the flow to be added
				if(residualCapacity == 0 ){
					throw new java.lang.Error(" The residual capacity at port: " + portFlowAdd + " is 0. Please check the flow configurations.");
				}
				
			}
			
			System.out.println("Flow inserted at port: " + (String) currNode.getProperty("portName"));

			// Keep streaming the flows through the links as long as there is no new node
			while(true){
				Node nextNode = getNextNode(currNode);
				
				if(nextNode == null)
				{
					System.out.println("The flow is completed");
					break;
				}
				System.out.println("Flow inserted at port: " + (String) nextNode.getProperty("portName"));

				params.put("n1", (String) currNode.getProperty("portName"));
				params.put("n2", (String) nextNode.getProperty("portName"));
				query = "MATCH (n1)-[r]->(n2) WHERE n1.portName=$n1 AND n2.portName=$n2 RETURN r";
				
				results = graphDb.execute(query, params);
				
				while(results.hasNext()){
					Map<String, Object> row = results.next();
					Relationship rel = (Relationship) row.get("r");
					
					// TODO: There needs to be a constraint based on what device it is:
					// The USED/UNUSED should be according to whether the flow on edge exceeds 40 for switch / 1 for WSS
					rel.setProperty("used", 1);
					String[] flow = (String []) rel.getProperty("flow");
					
					List<String> flowList = new ArrayList<String>(Arrays.asList(flow));
					flowList.add(flowAdded);
					pathNameList.add((String) rel.getProperty("name"));
					
					params.clear();
					params.put("n1", (String) currNode.getProperty("portName"));
					params.put("n2", (String) nextNode.getProperty("portName"));
					params.put("flow", flowList);
					query = "MATCH (n1)-[r]->(n2) WHERE n1.portName=$n1 AND n2.portName=$n2 SET r.flow = $flow";
					
					graphDb.execute(query, params);
					
//					rel.setProperty("flow", flowLsist);
				}
				currNode = nextNode;
			
			}
			tx.success();	
			System.out.println("\nNeo4j flow transactions successful end...");
		}
		
		return pathNameList;
	}
	
	public static Node getNextNode(Node currNode){
		
		Iterable<Relationship> rels = currNode.getRelationships();
	    Stream<Relationship> relsStream = StreamSupport.stream(rels.spliterator(), false);
	    Stream<Node> nbrNodes = relsStream.map(r -> {Node nbr = r.getEndNode(); return nbr;});  
	    
	    // Iterate over all neighbor nodes to find the first node that has available capacity > flow
	    int numNbrs = 0;
	    boolean foundNode = false;
	    Node nextNode = null;
	    // CAUTION: The nbr node also contains the currNode, so skip it !!
	    for (Node nbr : (Iterable<Node>) () -> nbrNodes.iterator()) {
	    	
	    	if( ! ((String) nbr.getProperty("portName")).equals( ((String) currNode.getProperty("portName"))) ){
//	    	    System.out.println("CurrNode: " + (String) currNode.getProperty("portName") + " nextNode: " + (String) nbr.getProperty("portName"));

		    	// TODO: The following condition is not correct
		    	if((float) nbr.getProperty("residualCapacity") > 0){
		    		foundNode = true;
		    		nextNode = nbr;
		    		break;
		    	}
		    	numNbrs += 1;
	    	}
	    }
	    
	    // If a node has neighbors but neither has any residual capacity, the flow cannot be forwarded
	    if(numNbrs > 0 && nextNode == null){
	    	throw new java.lang.Error(" The flow cannot be provisioned since the port: " + (String) currNode.getProperty("portName") + "  has no outgoing nodes with available capacity.");
	    }
	    
//	    System.out.println("CurrNode: " + (String) currNode.getProperty("portName") + " nextNode: " + (String) nextNode.getProperty("portName"));
	    return nextNode;
	}
}


