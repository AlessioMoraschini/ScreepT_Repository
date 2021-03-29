package updates;

import org.junit.Test;

import screept.license.tools.extractor.LicenseExtractor;

public class LicenseExtractorTest {

	@Test
	public void testGenerateHtml() throws Exception {
		String[] args = new String[]{"-generateHtml", "true"};
		LicenseExtractor.main(args);
	}

//	@Test
	public void testGenerateRawXml() throws Exception {
		String[] args = new String[]{"-raw", "true"};
		LicenseExtractor.main(args);
	}
}
