package main.java.com.belllabs.Utilities;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.StatementResultCursor;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;

import static org.neo4j.driver.v1.Values.parameters;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class Utilities implements AutoCloseable
{
    private final Driver driver;

    public Utilities( String uri, String user, String password )
    {
        driver = GraphDatabase.driver( uri, AuthTokens.basic( user, password ) );
    }

    @Override
    public void close() throws Exception
    {
        driver.close();
    }

    public CompletionStage<Void> exportGraph( final String message )
    {
        try ( Session session = driver.session() )
        {
        	String query = "CALL apoc.export.graphml.all(\'complete-graph.graphml\'";
        	Map<String,Object> parameters = Collections.singletonMap( "id", 0 );

        	Function<Transaction, CompletionStage<Void>> printSingleTitle = tx ->
        	        tx.runAsync( query, parameters )
        	                .thenCompose( StatementResultCursor::singleAsync )
        	                .thenApply( record -> record.get( 0 ).asString() )
        	                .thenApply( title ->
        	                {
        	                    // single title fetched successfully
        	                    System.out.println( title );
        	                    return true; // signal to commit the transaction
        	                } )
        	                .exceptionally( error ->
        	                {
        	                    // query execution failed
        	                    error.printStackTrace();
        	                    return false; // signal to rollback the transaction
        	                } )
        	                .thenCompose( commit -> commit ? tx.commitAsync() : tx.rollbackAsync() );

        	return session.beginTransactionAsync()
        	        .thenCompose( printSingleTitle )
        	        .exceptionally( error ->
        	        {
        	            // either commit or rollback failed
        	            error.printStackTrace();
        	            return null;
        	        } )
        	        .thenCompose( ignore -> session.closeAsync() );
        	
//            String greeting = session.writeTransaction( new TransactionWork<String>()
//            {
//                @Override
//                public String execute( Transaction tx )
//                {
//                    StatementResult result = tx.run( "CALL apoc.export.graphml.all(\'complete-graph.graphml\', {})",
//                            parameters( "message", message ) );
//                    return result.single().get( 0 ).asString();
//                }
//            } );
//            System.out.println( greeting );
        }
    }

//    public static void main( String... args ) throws Exception
//    {
//        try (Utilities exp = new Utilities( "bolt://localhost:7687", "neo4j", "Justdoit5!" ) )
//        {
//            exp.exportGraph( "hello, world" );
//        }
//    }
}