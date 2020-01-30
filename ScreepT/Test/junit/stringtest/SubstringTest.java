package junit.stringtest;

import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;

import configuration.RepeatTest;
import configuration.RepeatedTestRule;
import testUtils.TestUtils;

public class SubstringTest {

		@Rule
		public RepeatedTestRule repeatRule = new RepeatedTestRule();
		
		// START TESTS
		String chinesePassordString = "这是什么这是什么这是什么这是什么";
		String testPasswordString = "abcdefghijklmnop";
		
		@Test
		@RepeatTest(times = 1)
		public void testSubstring() throws IndexOutOfBoundsException, UnsupportedEncodingException {
			SubstringTestGen(testPasswordString);
			SubstringTestGen(chinesePassordString);
		}
		
		
		private void SubstringTestGen(String source) throws IndexOutOfBoundsException, UnsupportedEncodingException{
			
			int sourceLength = source.length();
			byte[] extractedBytes = new byte[sourceLength];
			ArrayList<Byte> extractedList = new ArrayList<Byte>();
			
			// extract bytes from source string
			for(int i=0; i<sourceLength; i++) {
				String currentChar = source.substring(i, i+1);
				byte[] byteChar = currentChar.getBytes("UTF-8");
				
				for(byte b : byteChar) {
					extractedList.add(b);
				}
				
				String feedbackString = " => Current char ("+(i+1)+") = " + currentChar + " - NofBytes: " + byteChar.length;
				TestUtils.printl(feedbackString);
			}
			TestUtils.printl("Integral byte coded: " + extractedList.size());
			
			int i = 0;
			for(byte by : extractedList) {
				if(i >= sourceLength) {
					break;
				}
				extractedBytes[i] = by;
				i++;
			}
			
			TestUtils.printl("Extracted: "+String.valueOf(Arrays.toString(extractedBytes)));
			
			assertTrue(extractedBytes.length == sourceLength);

		}
}
