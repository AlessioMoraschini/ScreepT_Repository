package JUnit;

import java.io.File;

import org.junit.Test;

import conversion.ImageToTextExtractor;
import net.sourceforge.tess4j.TesseractException;
import testUtils.Printer;

public class ImageToTextReaderTest {
	
	public static final String SOURCE_IMAGE_PATH = "mediaFileTest/img.png";
	
	 @Test
	 public void testExtractor() throws TesseractException {
		 Printer.printSeparator(80);
		 Printer.print("\n\n");
		 ImageToTextExtractor extractor = new ImageToTextExtractor();
		 Printer.printl(extractor.readTextFromImage(new File(SOURCE_IMAGE_PATH)));
	 }
}
