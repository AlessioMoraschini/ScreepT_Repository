package updates;

import java.io.File;

import org.junit.Test;

import licenses.info.extractor.LicensesInfoExtractor;
import various.common.light.files.CustomFileFilters;
import various.common.light.files.FileWorker;

public class TestRawLicensesInfoExtractor {

	private static String completeXml = "./UTILS_BUILD/complete_license_list.xml";
	private static String rawXmlFolderSrc = "./licenses_builds/raw_licenses_list/";
	private static String rawXmlFolderOut = rawXmlFolderSrc + "generated/";

	@Test
	public void testExtraction() throws Exception {

		File outDir = new File(rawXmlFolderOut);
		outDir.mkdirs();
		FileWorker.deleteDirContentRecursive(outDir);

		File rawXmlFolder = new File(rawXmlFolderSrc);
		File[] definitions = rawXmlFolder.listFiles(CustomFileFilters.customTypeFilter(CustomFileFilters.FILE_ONLY));
		for(File definition : definitions) {
			String outputFile = rawXmlFolderOut + definition.getName();
			extractFromCompleteList(definition.getAbsolutePath(), outputFile, completeXml);
		}
	}

	private void extractFromCompleteList(String sourceFile, String outFile, String completeListFile) throws Exception {
		System.out.print(
				LicensesInfoExtractor.syncFromCompleteList(
						new File(sourceFile), new File(outFile), new File(completeListFile)));

	}
}
