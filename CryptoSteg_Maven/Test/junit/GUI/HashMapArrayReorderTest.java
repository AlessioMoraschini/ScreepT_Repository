package junit.GUI;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import junit.configuration.RepeatTest;
import junit.configuration.RepeatedTestRule;
import uTILS.local.EntophyzerUtils;
import uTILS.local.FileStuff;

public class HashMapArrayReorderTest {

	@Rule
	public RepeatedTestRule repeatRule = new RepeatedTestRule();

	private long startTime=0;
	private long endTime=0;
	private static final int SRC_BYTES = 40000000;  // number of source bytes to generate
	private static final int KEY_LENGTH = 256; // length of key image pixels values to generate
	
	private boolean printOutput = false;
	
	@Test
	@Ignore
	public void testRandArray(){
		printSeparator(100);
		print("Test One");
		printSeparator(100);
		
		updateStart();
		
		int[] arr = randomIntArray(10000);
		
		updateEnd();
		print("\n"+Arrays.toString(arr));
		
		printSeparator(100);
		print("\nElapsed: "+getElapsed()+" ms");
	}
	
	@Test
//	@Ignore
	@RepeatTest(times = 3)
	public void testBytesSortMethod(){
		printSeparator(100);
		print("\nTest Bytes Sort with method - using key integer array (PX values from key image)");
		printSeparator(100);
		
		// array that represents key array values of N px
		int[] keyArray = randomIntArray(KEY_LENGTH); 
		int[] wrongKey = randomIntArray(KEY_LENGTH);
		
		// init array that represents source bytes to sort
		byte[] bytesToCode = initSrcBytes(SRC_BYTES);
		
		updateStart();
		byte[] codedBytes = EntophyzerUtils.orderArrayByKey(bytesToCode, keyArray);
		updateEnd();
		int codeTime = getElapsed();
		
		updateStart();
		byte[] originalBytesFromCoded = EntophyzerUtils.reorderBackArrayByKey(codedBytes, keyArray);
		updateEnd();
		int decodeTime = getElapsed();
		
		byte[] wrongKeyDecodedBytes = EntophyzerUtils.reorderBackArrayByKey(codedBytes, wrongKey);
		
		if (printOutput) {
			print("\n => Source bytes:\n");
			printArray(bytesToCode);
			print("\n => Source keys:\n");
			printIntArray(keyArray);
			printSeparator(100);
			print("\n => Coded bytes:\n");
			printArray(codedBytes);
			printSeparator(100);
			print("\n => De-Coded bytes:\n");
			printArray(originalBytesFromCoded);
			printSeparator(100);
		}
		
		print("\nCoding time: " + codeTime + " ms => speed = "+FileStuff.coolFileSize((long)SRC_BYTES*1000/codeTime)+"/sec");
		print("\nDecoding time: " + decodeTime + " ms=> speed = "+FileStuff.coolFileSize((long)SRC_BYTES*1000/decodeTime)+"/sec");
		
		assertTrue(Arrays.equals(bytesToCode, originalBytesFromCoded));
		assertTrue(!Arrays.equals(bytesToCode, wrongKeyDecodedBytes));
	}
	
	@Test
	@Ignore
	public void testMapSort(){
		printSeparator(100);
		print("\nTest Two");
		printSeparator(100);
		
		// array that represents key array values of N px
		int[] keyArray = randomIntArray(KEY_LENGTH); 
		// init array that represents source bytes to sort
		byte[] bytesToCode = initSrcBytes(SRC_BYTES);
		
		Map<Integer, Integer> keysMap = arrayToMap(keyArray);
//		Map<Integer, Integer> map = getRandMap(256);
		Map<Integer, Integer> keySortedMap = new HashMap<Integer, Integer>();
		Map<Integer, Integer> valSortedMap = new HashMap<Integer, Integer>();

		updateStart();
		
		//keySortedMap = sortMapByKey(keysMap);
		
		valSortedMap = sortMapByValue(keysMap);
		
		Integer[] indexSortedArray = new Integer[KEY_LENGTH];
		indexSortedArray = valSortedMap.keySet().toArray(indexSortedArray);
		
		byte[] sortedBytes = orderFromIndexes(bytesToCode, indexSortedArray);
		byte[] reorderedBytes = reorderBackFromIndexes(sortedBytes, indexSortedArray);
		
		updateEnd();
		
		if (printOutput) {
//			print("\n\n => Sorted MAP (BY KEY):\n");
//			printMap(keySortedMap);
			print("\n => Source MAP:\n");
			printMap(keysMap);
			print("\n\n => Sorted MAP (BY VALUE):\n");
			printMap(valSortedMap);
			print("\n => Source bytes:\n");
			printArray(bytesToCode);
			print("\n => Ordered bytes:\n");
			printArray(sortedBytes);
			print("\n => Back to Source bytes:\n");
			printArray(reorderedBytes);
			printSeparator(100);
			print("\n # Values:" + Arrays.toString(keysMap.values().toArray().clone()));
			print("\n\n # Keys:" + Arrays.toString(keysMap.keySet().toArray().clone()));
			printSeparator(100);
			print("\n # Values:" + Arrays.toString(valSortedMap.values().toArray().clone()));
			print("\n\n # Keys:" + Arrays.toString(valSortedMap.keySet().toArray().clone()));
		}
		printSeparator(100);
		print("\nElapsed: "+getElapsed()+" ms");

	}
	
	
	// local lazy printer
	private void print(String S){
		System.out.print(S);
	}
	
	private void printSeparator(int n){
		int curr = 0;
		print("\n");
		while (curr < n) {
			print("#");
			curr++;
		}
	}
	
    //pretty print a map
    public static <K, V> void printMap(Map<K, V> map) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            System.out.println("Key : " + entry.getKey() 
				+ " <=> Value : " + entry.getValue());
        }
    }
    
    // printer for array
    public static void printArray(byte[] array) {
    	int i=0;
        for (byte curr : array) {
            System.out.println("Val["+i+"] : " + curr);
            i++;
        }
    }
    
    // printer for array
    public static void printIntArray(int[] array) {
    	int i=0;
        for (int curr : array) {
            System.out.println("Val["+i+"] : " + curr);
            i++;
        }
    }
    
    private byte[] orderFromIndexes(byte[] source, Integer[] indexes) {
    	if(source.length!=indexes.length) {
    		return null;
    	}
    	
    	byte[] result = new byte[indexes.length];

    	for(int i=0; i<indexes.length; i++) {
    		result[indexes[i]] = source[i];
    	}
    	
    	return result;
    }
    
    private byte[] reorderBackFromIndexes(byte[] ordered, Integer[] indexes) {
    	if(ordered.length!=indexes.length) {
    		return null;
    	}
    	
    	byte[] result = new byte[indexes.length];

    	for(int i=0; i<indexes.length; i++) {
    		result[i] = ordered[indexes[i]];
    	}
    	
    	return result;
    }
    
    
    // byte array source seq generator
    private byte[] initSrcBytes(int dim) {
		byte[] srcBytes = new byte[dim];
    	for(int i = 0; i<srcBytes.length; i++) {
    		srcBytes[i] = (byte)i;
		}
    	return srcBytes;
	}
    
	// static array generator
	private int[] initSrc(int dim) {
		int[] sourceArray = new int[dim];
		for(int i = 0; i<dim; i++) {
			sourceArray[i] = i;
		}
		return sourceArray;
	}
	
	// random int array generator
	private int[] randomIntArray (int dim){
		Random rand = new Random();
		int[]res = new int[dim];
		for(int i=0 ; i<res.length; i++){
			res[i] = rand.nextInt(Integer.MAX_VALUE)-rand.nextInt(Integer.MAX_VALUE) ;
		}
		return res; 
	}
	
	// HASHMAP GENERATOR
	private Map<Integer, Integer> getRandMap(int dim){
		Map<Integer, Integer> map = new LinkedHashMap<Integer, Integer>();
		Random rand = new Random();
		
		map.put(0, 105);
		map.put(1, 105);
		
		for(int i=dim-1 ; i>=2; i--){
			Integer val = rand.nextInt(Integer.MAX_VALUE)-rand.nextInt(Integer.MAX_VALUE) ;
			map.put(i, val);
		}
		
		return map;
	}
	
	// array hasmap mapper
	private Map<Integer, Integer> arrayToMap(int[] keyArray){
		Map<Integer, Integer> map = new LinkedHashMap<Integer, Integer>();
		
		for(int i=0 ; i<keyArray.length; i++){
			map.put(i, keyArray[i]);
		}
		
		return map;
	}
	
	// MAP SORTER BY KEY
	private TreeMap<Integer, Integer> sortMapByKey(Map<Integer, Integer> map){
        TreeMap<Integer, Integer> treeMap = new TreeMap<Integer, Integer>(map);

		return treeMap;
	}
	
	// MAP SORTER BY VALUE
    public static <K, V extends Comparable<? super V>> Map<K, V> sortMapByValue(Map<K, V> map) {
        List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Entry.comparingByValue());

        Map<K, V> result = new LinkedHashMap<>();
        for (Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
	
	
	private void updateStart(){
		startTime = System.nanoTime();
	}
	
	private void updateEnd(){
		endTime = System.nanoTime();
	}
	
	private int getElapsed(){
		return (int)((endTime-startTime)/1000000);
	}
	
	
}
