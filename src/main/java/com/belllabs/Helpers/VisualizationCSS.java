package main.java.com.belllabs.Helpers;

public class VisualizationCSS {
	public static String styleSheet =
	        "node {" +
	        " size: 20px, 20px; " + 
	        " fill-color: black; " +
	        " text-alignment: above; " +
	        " text-size: 20px; " + 
	        "}" +
	        "node.switch_input {" +
	        " size: 20px, 20px; " + 
	        " fill-color: black; " +
	        " text-alignment: at-left; " +
	        " text-size: 20px; " + 
	        "}"+
	        "node.switch_output {" +
	        " size: 20px, 20px; " + 
	        " fill-color: black; " +
	        " text-alignment: at-right; " +
	        " text-size: 20px; " + 
	        "}"+
	        "node.oxc_optical {" +
	        " size: 20px, 20px; " + 
	        " fill-color: blue; " +
	        " text-alignment: under; " +
	        " text-size: 20px; " + 
	        "}"+
	        "node.oxc_digital {" +
	        " size: 20px, 20px; " + 
	        " fill-color: black; " +
	        " text-alignment: above; " +
	        " text-size: 20px; " + 
	        "}"+
	        "node.wss_optical_inp {" +
	        " size: 20px, 20px; " + 
	        " fill-color: blue; " +
	        " text-alignment: at-left; " +
	        " text-size: 20px; " + 
	        "}"+
	        "node.wss_optical_out {" +
	        " size: 20px, 20px; " + 
	        " fill-color: blue; " +
	        " text-alignment: at-right; " +
	        " text-size: 20px; " + 
	        "}"+
	        "node.wss_dwdm_inp {" +
	        " size: 20px, 20px; " + 
	        " fill-color: green; " +
	        " text-alignment: at-left; " +
	        " text-size: 20px; " + 
	        "}"+
	        "node.wss_dwdm_out {" +
	        " size: 20px, 20px; " + 
	        " fill-color: green; " +
	        " text-alignment: at-right; " +
	        " text-size: 20px; " + 
	        "}"+
	        "edge {"+
			"	size: 3px;" +
			"arrow-shape: arrow; arrow-size: 20px, 10px; " +
	        "}" +
	        "edge.switch {"+
			"	size: 3px;" +
			" arrow-shape: arrow; arrow-size: 15px, 7px; " +
	        "}"
	        ;
	
	public static String ROADMStyleSheet = "node {" +
	        " size: 10px, 10px; " + 
	        " fill-color: black; " +
	        " text-alignment: above; " +
	        " text-size: 1px; " + 
	        "}" +
	        "node.switch_input {" +
	        " size: 10px, 10px; " + 
	        " fill-color: black; " +
	        " text-alignment: at-left; " +
	        " text-size: 1px; " + 
	        "}"+
	        "node.switch_output {" +
	        " size: 10px, 10px; " + 
	        " fill-color: black; " +
	        " text-alignment: at-right; " +
	        " text-size: 1px; " + 
	        "}"+
	        "node.oxc_optical {" +
	        " size: 10px, 10px; " + 
	        " fill-color: white; " +
	        " stroke-mode: plain; " +
	    	" stroke-color: black; " +
	        " text-alignment: under; " +
	        " text-size: 1px; " + 
	        "}"+
	        "node.oxc_digital {" +
	        " size: 10px, 10px; " + 
	        " fill-color: black; " +
	        " text-alignment: above; " +
	        " text-size: 1px; " + 
	        "}"+
	        "edge {"+
			"	size: 2px;" +
			"arrow-shape: arrow; arrow-size: 10px, 7px; "
			+ " fill-color:#D3D3D3; " +
	        "}" +
	        "edge.switch {"+
			"	size: 1px;" +
			" arrow-shape: arrow; arrow-size: 10px, 7px; "
			+ " fill-color: #D3D3D3; " +
	        "}"
	        ;
	
	
}
