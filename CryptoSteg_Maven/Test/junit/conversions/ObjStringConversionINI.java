package junit.conversions;

import java.awt.Color;
import java.awt.Font;

import org.junit.Test;

import enums.ScalingAlgorithm;
import testUtils.Printer;
import variousutils.HandleString;

public class ObjStringConversionINI {

	@Test
	public void testColor() {
		Color black = Color.black;
		Color dynColor = new Color(50,120,200);
		
		String colorY = HandleString.getRgbString(black);
		Printer.printl("Stringed: "+colorY);
		
		Color parsed = HandleString.getRgbColorFromString(colorY);
		Printer.printl("Parsed: "+parsed);
		
		Printer.printSeparator(40);
	}
	
	@Test
	public void testFont() {
		Font testFont = new Font("Segoe UI", Font.PLAIN, 20);
		
		String fontString = HandleString.getFontString(testFont);
		Printer.printl("\nString font: "+fontString);
		
		Font parsed = HandleString.getFontFromString(fontString);
		Printer.printl("Parsed: "+parsed);
		
		Printer.printSeparator(40);
	}
	
	@Test
	public void floatTest() {
		Printer.printl("Float parsed: " + Float.parseFloat("0.8"));
		Printer.printSeparator(50);
	}
	
	@Test
	public void enumAlgoTest() {
		ScalingAlgorithm algo = ScalingAlgorithm.valueOf("REPLICATE");
		Printer.print("\n"+algo.toString()+algo.getDescription());
	}
	
}
