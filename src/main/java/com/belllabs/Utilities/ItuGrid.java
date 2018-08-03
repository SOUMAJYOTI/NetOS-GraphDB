package main.java.com.belllabs.Utilities;

import java.util.ArrayList;
import java.util.List;

import javafx.util.Pair;

public class ItuGrid {
/**
 * This class stores the map of the channels and the frequencies for DWDM - 100 GHz spacing
 * @param spacing - the frequency spacing between channels 
 */
	public static ArrayList<Pair<Integer, Integer>> channelFreq(int spacing){
		int channelStart = 1;
		int freq  = 190100;
		
		ArrayList<Pair<Integer, Integer>> cf = new ArrayList<Pair<Integer, Integer>>();
		for(int i=channelStart; i<=72; i += 1, freq += ((i-1)* spacing)){  // Fixed to 72 channels
			cf.add(new Pair<Integer, Integer>(i, freq));
		}
		return cf;
	}
}
