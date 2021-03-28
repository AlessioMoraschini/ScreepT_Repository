package screept.license.tools.extractor;

import java.io.File;
import java.util.Arrays;

import licenses.info.extractor.LicensesInfoExtractor;
import various.common.light.files.CustomFileFilters;
import various.common.light.files.FileVarious;
import various.common.light.files.FileWorker;

public class ScreepTLicensesInfoExtractor {

	private static String screeptExportFolder = "./ROOT_DIR_APP_FILES/Libraries_licenses/detailedReport";

	private static String sourceFolder = "./licenses_builds/";
	private static String sourcePluginsFolder = sourceFolder + "plugins/";

	private static String outFolder = sourceFolder + "generated/";
	private static String outPluginsFolder = outFolder + "plugins/";

	private static boolean downloadLicenses = false;

	public static void initPaths(File root) {
		screeptExportFolder = root.getAbsolutePath() + "/ROOT_DIR_APP_FILES/Libraries_licenses/detailedReport";
		sourceFolder = root.getAbsolutePath() + "/licenses_builds/";
		sourcePluginsFolder = sourceFolder + "plugins/";
		outFolder = sourceFolder + "generated/";
		outPluginsFolder = outFolder + "plugins/";

		System.out.println("Paths generated from root [" + root.getAbsolutePath() + "]:");
		System.out.println("screeptExportFolder: " + screeptExportFolder);
		System.out.println("sourceFolder: " + sourceFolder);
		System.out.println("sourcePluginsFolder: " + sourcePluginsFolder);
		System.out.println("outFolder: " + outFolder);
		System.out.println("outPluginsFolder: " + outPluginsFolder);
	}

	public static void main(String[] args) throws Exception {

		System.out.println("ScreepTRawLicensesInfoExtractor.main() -> ARGS: " + Arrays.toString(args));

		File rootPrj = args != null && args.length > 1 ? new File(args[1]) : null;
		if(rootPrj != null && rootPrj.exists() && rootPrj.isDirectory()) {
			initPaths(rootPrj);
		}

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

		if(Boolean.valueOf(args[0])) {
			// Now copy also to screept export directory if specified throught params
			FileWorker.copyDirectoryContents(new File(outFolder), new File(screeptExportFolder));
			System.out.println(outFolder + " -> content copied into -> " + screeptExportFolder);
		}
	}

	private static void exctractHtml(String sourceFile, String outFile, boolean downloadLicenses) throws Exception {
		System.out.print(
				LicensesInfoExtractor.writeLicensesInfo(
						new File(sourceFile), new File(outFile), downloadLicenses));

	}
}
