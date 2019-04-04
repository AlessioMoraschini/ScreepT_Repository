package testUtils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class Generator {

	// SECURE INTEGER GENERATOR
	public static int secRand(boolean verySecure) throws NoSuchAlgorithmException{
		SecureRandom secureGen;
		secureGen = (verySecure)? SecureRandom.getInstanceStrong() : new SecureRandom();
		return secureGen.nextInt();
	}
	
	// INTEGER ARRAY GENERATOR
	public static int[] randomIntArray (int dim){
		Random rand = new Random();
		int[]res = new int[dim];
		for(int i=0 ; i<res.length-1; i++){
			res[i] = rand.nextInt(Integer.MAX_VALUE)-rand.nextInt(Integer.MAX_VALUE) ;
		}
		return res; 
	}
	
	// HASHMAP GENERATOR
	public static Map<Integer, Integer> getRandIntMap(int dim){
		Map<Integer, Integer> map = new LinkedHashMap<Integer, Integer>();
		Random rand = new Random();
		
		for(int i=dim-1 ; i>=0; i--){
			Integer val = rand.nextInt(Integer.MAX_VALUE)-rand.nextInt(Integer.MAX_VALUE) ;
			map.put(i, val);
		}
		
		return map;
	}
}
