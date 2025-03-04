package junit.GUI;

import static org.junit.Assert.assertTrue;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import test.util.configuration.RepeatTest;
import test.util.configuration.RepeatedTestRule;
import test.util.utils.Generator;
import test.util.utils.TestUtils;
import various.common.light.utility.manipulation.ConversionUtils;
import various.common.light.utility.manipulation.EntrophyzerUtils;
import various.common.light.utility.time.TimeMeter;

public class HashMapArrayReorderTest {

	@Rule
	public RepeatedTestRule repeatRule = new RepeatedTestRule();

	private static final int SRC_BYTES = 40000000;  // number of source bytes to generate
	private static final int KEY_LENGTH = 256; // length of key image pixels values to generate
	
	private boolean printOutput = false;
	private TimeMeter timerManager;
	
	@Before
	public void initTest(){
		timerManager = new TimeMeter();
		timerManager.updateStart();
	}
	
	
	@Test
	@Ignore
	public void testRandArray(){
		TestUtils.printSeparator(100);
		TestUtils.print("Test One");
		TestUtils.printSeparator(100);
		
		timerManager.updateStart();
		
		int[] arr = Generator.randomIntArray(10000);
		
		timerManager.updateEnd();
		TestUtils.print("\n"+Arrays.toString(arr));
		
		TestUtils.printSeparator(100);
		TestUtils.print("\nElapsed: "+timerManager.getElapsed(true)+" ms");
	}
	
	@Test
//	@Ignore
	@RepeatTest(times = 3)
	public void testBytesSortMethod(){
		TestUtils.printSeparator(100);
		TestUtils.print("\nTest Bytes Sort with method - using key integer array (PX values from key image)");
		TestUtils.printSeparator(100);
		
		// array that represents key array values of N px
		int[] keyArray = Generator.randomIntArray(KEY_LENGTH); 
		int[] wrongKey = Generator.randomIntArray(KEY_LENGTH);
		
		// init array that represents source bytes to sort
		byte[] bytesToCode = Generator.getRandomBytes(SRC_BYTES);
		
		timerManager.updateStart();
		byte[] codedBytes = EntrophyzerUtils.orderArrayByKey(bytesToCode, keyArray);
		timerManager.updateEnd();
		long codeTime = timerManager.getElapsed(true);
		
		timerManager.updateStart();
		byte[] originalBytesFromCoded = EntrophyzerUtils.reorderBackArrayByKey(codedBytes, keyArray);
		timerManager.updateEnd();
		long decodeTime = timerManager.getElapsed(true);
		
		byte[] wrongKeyDecodedBytes = EntrophyzerUtils.reorderBackArrayByKey(codedBytes, wrongKey);
		
		if (printOutput) {
			TestUtils.print("\n => Source bytes:\n");
			printArray(bytesToCode);
			TestUtils.print("\n => Source keys:\n");
			printIntArray(keyArray);
			TestUtils.printSeparator(100);
			TestUtils.print("\n => Coded bytes:\n");
			printArray(codedBytes);
			TestUtils.printSeparator(100);
			TestUtils.print("\n => De-Coded bytes:\n");
			printArray(originalBytesFromCoded);
			TestUtils.printSeparator(100);
		}
		
		TestUtils.print("\nCoding time: " + codeTime + " ms => speed = "+ConversionUtils.coolFileSize((long)SRC_BYTES*1000/codeTime)+"/sec");
		TestUtils.print("\nDecoding time: " + decodeTime + " ms=> speed = "+ConversionUtils.coolFileSize((long)SRC_BYTES*1000/decodeTime)+"/sec");
		
		assertTrue(Arrays.equals(bytesToCode, originalBytesFromCoded));
		assertTrue(!Arrays.equals(bytesToCode, wrongKeyDecodedBytes));
	}
	
	@Test
	@Ignore
	public void testMapSort(){
		TestUtils.printSeparator(100);
		TestUtils.print("\nTest Two");
		TestUtils.printSeparator(100);
		
		// array that represents key array values of N px
		int[] keyArray = Generator.randomIntArray(KEY_LENGTH); 
		// init array that represents source bytes to sort
		byte[] bytesToCode = Generator.getRandomBytes(SRC_BYTES);
		
		Map<Integer, Integer> keysMap = EntrophyzerUtils.arrayToIndexedMap(keyArray);
//		Map<Integer, Integer> map = getRandMap(256);
//		Map<Integer, Integer> keySortedMap = new HashMap<Integer, Integer>();
		Map<Integer, Integer> valSortedMap = new HashMap<Integer, Integer>();

		timerManager.updateStart();
		
		//keySortedMap = sortMapByKey(keysMap);
		
		valSortedMap = EntrophyzerUtils.sortMapByValue(keysMap);
		
		Integer[] indexSortedArray = new Integer[KEY_LENGTH];
		indexSortedArray = valSortedMap.keySet().toArray(indexSortedArray);
		
		byte[] sortedBytes = orderFromIndexes(bytesToCode, indexSortedArray);
		byte[] reorderedBytes = reorderBackFromIndexes(sortedBytes, indexSortedArray);
		
		timerManager.updateEnd();
		
		if (printOutput) {
//			TestUtils.print("\n\n => Sorted MAP (BY KEY):\n");
//			printMap(keySortedMap);
			TestUtils.print("\n => Source MAP:\n");
			TestUtils.printMap(keysMap);
			TestUtils.print("\n\n => Sorted MAP (BY VALUE):\n");
			TestUtils.printMap(valSortedMap);
			TestUtils.print("\n => Source bytes:\n");
			printArray(bytesToCode);
			TestUtils.print("\n => Ordered bytes:\n");
			printArray(sortedBytes);
			TestUtils.print("\n => Back to Source bytes:\n");
			printArray(reorderedBytes);
			TestUtils.printSeparator(100);
			TestUtils.print("\n # Values:" + Arrays.toString(keysMap.values().toArray().clone()));
			TestUtils.print("\n\n # Keys:" + Arrays.toString(keysMap.keySet().toArray().clone()));
			TestUtils.printSeparator(100);
			TestUtils.print("\n # Values:" + Arrays.toString(valSortedMap.values().toArray().clone()));
			TestUtils.print("\n\n # Keys:" + Arrays.toString(valSortedMap.keySet().toArray().clone()));
		}
		TestUtils.printSeparator(100);
		TestUtils.print("\nElapsed: "+timerManager.getElapsed(true)+" ms");

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
    
    
}
