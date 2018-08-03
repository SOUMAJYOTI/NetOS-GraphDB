package main.java.com.belllabs.Utilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Colors {
	
	/**
	 * This function computes colors based on an input  to color different ports 
	 * 
	 * @param type - the distinct id(channel) for which a color has to be sent - can be both strings and integers
	 * @param colors - the RGB map of the color allocated to the ID
	 * @return
	 */
	public static List<Integer> color(Object type, Map<Object, List<Integer>> c) throws IOException {
        return (c.computeIfAbsent(type, k -> {
        	
        	int rgb = 0;
        	if(type.getClass() == Integer.class){
        		rgb = (int) type;
        	}
        	else if(type.getClass() == String.class){
        		// The hash code for a String object is computed as −
            	// s[0]*31^(n - 1) + s[1]*31^(n - 2) + ... + s[n - 1]
            	
                rgb = type.hashCode();
                
        	}
        	
        	Random rand = new Random();
        	int n = rand.nextInt(50) + 1;
            Integer r = (int) (((rgb >> 16 & 0xFF) * n )%255.0f);
        	n = rand.nextInt(50) + 1;
            Integer g = (int) (((rgb >> 8  & 0xFF) * n)%255.0f);
        	n = rand.nextInt(50) + 1;
            Integer b = (int) (((rgb  >> 0    & 0xFF) * n)%255.0f);
            
            List<Integer> rgbList = Arrays.asList(r, g, b);
            return rgbList;
        }));
    }
	
	public static List<List<Integer>> fixedColors(){
		List<List<Integer>> colorList = new ArrayList<List<Integer>>();
		List<Integer> rgbList = Arrays.asList(255, 0, 0);
		colorList.add(rgbList);
		rgbList = Arrays.asList(50,205,50);
		colorList.add(rgbList);
		rgbList = Arrays.asList(139,0,139);
		colorList.add(rgbList);
		rgbList = Arrays.asList(154,205,50);
		colorList.add(rgbList);
		rgbList = Arrays.asList(218,165,32);
		colorList.add(rgbList);
		
		return colorList;
	}
}
