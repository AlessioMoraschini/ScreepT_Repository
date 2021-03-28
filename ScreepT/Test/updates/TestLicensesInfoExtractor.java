package updates;

import java.io.File;

import org.junit.Test;

import licenses.info.extractor.LicensesInfoExtractor;
import various.common.light.files.CustomFileFilters;
import various.common.light.files.FileVarious;
import various.common.light.files.FileWorker;

public class TestLicensesInfoExtractor {

	private static String sourceFolder = "./licenses_builds/";
	private static String sourcePluginsFolder = sourceFolder + "plugins/";

	private static String outFolder = sourceFolder + "generated/";
	private static String outPluginsFolder = outFolder + "plugins/";

	private static boolean downloadLicenses = false;

	@Test
	public void testExtraction() throws Exception {

		new File(sourcePluginsFolder).mkdirs();
		new File(outPluginsFolder).mkdirs();
		FileWorker.deleteDirContentRecursive(new File(outFolder));

		File screeptSource = new File(sourceFolder + "license_list_screept.xml");
		exctractHtml(screeptSource.getAbsolutePath(), outFolder + "license_list_screept.html", downloadLicenses);

		File pluginsDefinitionsFolder = new File(sourcePluginsFolder);
		File[] definitions = pluginsDefinitionsFolder.listFiles(CustomFileFilters.customTypeFilter(CustomFileFilters.FILE_ONLY));
		for(File definition : definitions) {
			File renamedOut = FileVarious.changeExtensionForceDot(definition, "html");
			String outputFile = outPluginsFolder + renamedOut.getName();
			exctractHtml(definition.getAbsolutePath(), outputFile, downloadLicenses);
		}
	}

	private void exctractHtml(String sourceFile, String outFile, boolean downloadLicenses) throws Exception {
		System.out.print(
				LicensesInfoExtractor.writeLicensesInfo(
						new File(sourceFile), new File(outFile), downloadLicenses));

	}
}
