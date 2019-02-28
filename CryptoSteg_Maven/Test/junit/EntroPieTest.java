package junit;

import static org.junit.Assert.assertTrue;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;

import junit.configuration.RepeatTest;
import junit.configuration.RepeatedTestRule;
import uTILS.local.ByteArrayShuffler;

public class EntroPieTest {

	@Rule
	public RepeatedTestRule repeatRule = new RepeatedTestRule();
	
	// TEST FIELDS
	int N = 256000000;
	byte[] sourceArray = new byte[N];
	byte[] entropyzedArray;
	
	// START TESTS
	
	@Test
	@RepeatTest(times = 1)
	public void entroPieForwardTest() throws IndexOutOfBoundsException{
		
		long startTime = System.nanoTime();
		
		initSrc();
		//printLineDest(16, sourceArray);
		
		//System.out.println("\n\n######################## Starting Sorting ###############################\n\n");

		entropyzedArray = ByteArrayShuffler.entroPieForward(sourceArray);
		//printLineDest(16, entropyzedArray);
		
		//System.out.println("\n\n######################## Turning Back ###############################\n\n");
		byte[] backToSource = ByteArrayShuffler.entroPieBack(entropyzedArray);
		//printLineDest(16, backToSource);
		
		long endTime = System.nanoTime();
		int elapsedMilliSeconds = (int)((endTime-startTime)/1000000);
		
		print("Elapsed: "+elapsedMilliSeconds+" ms");

		assertTrue(Arrays.equals(sourceArray, backToSource));
	}
	
	
	// UTILS LOCAL METHODS FOR TEST ONLY
	private void initSrc() {
		for(int i = 0; i<sourceArray.length; i++) {
			sourceArray[i] = (byte)i;
		}
	}
	
	private void printLineDest(int nForLine, byte[] array) {
		int progr = 0;
		while(progr+nForLine < array.length) {
			System.out.println(Arrays.toString(Arrays.copyOfRange(array, progr, progr+nForLine)));
			progr += nForLine;
		}
		System.out.println(Arrays.toString(Arrays.copyOfRange(array, progr, array.length-1)));
	}
	
	private boolean printEnabled(boolean DEBUG) {
        if (!DEBUG) {
            System.setOut(new PrintStream(new OutputStream() {
                public void close() {}
                public void flush() {}
                public void write(byte[] b) {}
                public void write(byte[] b, int off, int len) {}
                public void write(int b) {
                }
            }));
        }
        return DEBUG;
	}
	
	// lazy printer
	private void print(Object src) {
		System.out.println(src);
	}
	
	private void separatorPrint() {
		print("\n   ###################################################\n");
	}
}
